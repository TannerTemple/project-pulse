<template>
  <v-container class="pa-6" style="max-width: 800px">
    <h1 class="text-h5 font-weight-bold mb-2">Peer Evaluation</h1>
    <p class="text-medium-emphasis text-body-2 mb-6">
      Evaluate each of your teammates for the selected week. Evaluations cannot be edited once submitted.
    </p>

    <!-- Week selector -->
    <v-select
      v-model="selectedWeekId"
      class="mb-6"
      density="comfortable"
      :item-title="(w: any) => `Week of ${w.weekStart}`"
      item-value="id"
      :items="eligibleWeeks"
      label="Select week to evaluate"
      style="max-width: 320px"
      variant="outlined"
      @update:model-value="onWeekChange"
    />

    <v-alert
      v-if="error"
      class="mb-4"
      closable
      type="error"
      variant="tonal"
      @click:close="error = ''"
    >{{ error }}</v-alert>

    <v-alert v-if="!selectedWeekId" type="info" variant="tonal">
      Select a week above. You can only evaluate the previous week within 1 week of it ending.
    </v-alert>

    <template v-else-if="teammates.length === 0">
      <v-alert type="warning" variant="tonal">
        You are not assigned to a team, or your team has no other members.
      </v-alert>
    </template>

    <template v-else>
      <!-- One card per teammate -->
      <v-card
        v-for="(tm, i) in teammates"
        :key="tm.id"
        class="mb-4"
        rounded="lg"
      >
        <v-card-title class="pa-4 pb-2">
          {{ tm.firstName }} {{ tm.lastName }}
          <v-chip
            v-if="submitted[tm.id]"
            class="ml-2"
            color="success"
            size="small"
            variant="tonal"
          >
            Submitted
          </v-chip>
        </v-card-title>
        <v-card-text class="px-4">
          <v-form :ref="el => (formRefs[i] = el)">
            <!-- Scores per criterion -->
            <v-row v-for="criterion in rubricCriteria" :key="criterion.id" align="center" class="mb-1">
              <v-col cols="12" sm="6">
                <div class="text-body-2 font-weight-medium">{{ criterion.name }}</div>
                <div class="text-caption text-medium-emphasis">Max: {{ criterion.maxScore }}</div>
              </v-col>
              <v-col cols="12" sm="6">
                <v-text-field
                  v-model.number="scores[tm.id][criterion.id]"
                  density="comfortable"
                  :disabled="submitted[tm.id]"
                  :max="criterion.maxScore"
                  min="0"
                  :rules="[r => r >= 0 || 'Min 0', r => r <= criterion.maxScore || `Max ${criterion.maxScore}`]"
                  type="number"
                  variant="outlined"
                />
              </v-col>
            </v-row>

            <v-textarea
              v-model="comments[tm.id].public"
              class="mt-2"
              density="comfortable"
              :disabled="submitted[tm.id]"
              label="Public comments (shared with teammate)"
              rows="2"
              variant="outlined"
            />
            <v-textarea
              v-model="comments[tm.id].private"
              density="comfortable"
              :disabled="submitted[tm.id]"
              label="Private comments (instructor only)"
              rows="2"
              variant="outlined"
            />
          </v-form>
        </v-card-text>
        <v-card-actions v-if="!submitted[tm.id]" class="px-4 pb-4">
          <v-spacer />
          <v-btn
            color="primary"
            :loading="submitting[tm.id]"
            @click="submitEval(tm, i)"
          >
            Submit Evaluation
          </v-btn>
        </v-card-actions>
      </v-card>
    </template>
  </v-container>
</template>

<script lang="ts" setup>
  import { onMounted, ref } from 'vue'
  import { type ActiveWeek, api } from '@/api'

  interface User { id: number, firstName: string, lastName: string }
  interface Criterion { id: number, name: string, maxScore: number, orderIndex: number }

  const eligibleWeeks = ref<ActiveWeek[]>([])
  const selectedWeekId = ref<number | null>(null)
  const teammates = ref<User[]>([])
  const rubricCriteria = ref<Criterion[]>([])
  const error = ref('')

  // Per-teammate state
  const scores: Record<number, Record<number, number>> = {}
  const comments: Record<number, { public: string, private: string }> = {}
  const submitted: Record<number, boolean> = {}
  const submitting: Record<number, boolean> = {}
  const formRefs: any[] = []

  function initTeammate (tm: User) {
    if (!scores[tm.id]) {
      scores[tm.id] = {}
      for (const c of rubricCriteria.value) {
        scores[tm.id][c.id] = 0
      }
      comments[tm.id] = { public: '', private: '' }
      submitted[tm.id] = false
      submitting[tm.id] = false
    }
  }

  async function onWeekChange () {
    if (!selectedWeekId.value) return
  // teammates already loaded
  }

  async function submitEval (tm: User, i: number) {
    const formEl = formRefs[i]
    if (formEl) {
      const { valid } = await formEl.validate()
      if (!valid) return
    }

    submitting[tm.id] = true
    error.value = ''
    try {
      const scoreList = rubricCriteria.value.map(c => ({
        criterionId: c.id,
        score: scores[tm.id][c.id] ?? 0,
      }))
      await api.post('/peer-evaluations', {
        evaluateeId: tm.id,
        weekId: selectedWeekId.value,
        publicComments: comments[tm.id].public,
        privateComments: comments[tm.id].private,
        scores: scoreList,
      })
      submitted[tm.id] = true
    } catch (error_: any) {
      error.value = error_.message
    } finally {
      submitting[tm.id] = false
    }
  }

  onMounted(async () => {
    try {
      const me = await api.get<any>('/users/me')
      if (!me.sectionId) return

      // Load past active weeks only (eligible for evaluation)
      const allWeeks = await api.get<ActiveWeek[]>(`/sections/${me.sectionId}/weeks`)
      const today = new Date().toISOString().split('T')[0]
      // Active weeks that have already started (previous weeks)
      eligibleWeeks.value = allWeeks.filter(w => w.active && w.weekStart < today)

      // Load team roster
      if (me.teamId) {
        const team = await api.get<any>(`/teams/${me.teamId}`)
        teammates.value = team.students.filter((s: any) => s.id !== me.id)

        // Load rubric criteria from section's rubric
        const section = await api.get<any>(`/sections/${me.sectionId}`)
        if (section.rubric?.criteria) {
          rubricCriteria.value = section.rubric.criteria.toSorted((a: Criterion, b: Criterion) => a.orderIndex - b.orderIndex)
        }

        for (const teammate of teammates.value) initTeammate(teammate)
      }
    } catch (error_: any) {
      error.value = error_.message
    }
  })
</script>
