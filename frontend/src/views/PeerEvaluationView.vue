<template>
  <v-container class="pa-6" style="max-width: 800px">
    <h1 class="text-h5 font-weight-bold mb-2">Peer Evaluation</h1>
    <p class="text-medium-emphasis text-body-2 mb-6">
      Evaluate each of your teammates for the selected week. Evaluations cannot be edited once submitted.
    </p>

    <!-- Week selector -->
    <v-select
      v-model="selectedWeekId"
      label="Select week to evaluate"
      :items="eligibleWeeks"
      :item-title="(w: any) => `Week of ${w.weekStart}`"
      item-value="id"
      variant="outlined"
      density="comfortable"
      style="max-width: 320px"
      class="mb-6"
      @update:model-value="onWeekChange"
    />

    <v-alert v-if="error" type="error" variant="tonal" class="mb-4" closable @click:close="error = ''">{{ error }}</v-alert>

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
        rounded="lg"
        class="mb-4"
      >
        <v-card-title class="pa-4 pb-2">
          {{ tm.firstName }} {{ tm.lastName }}
          <v-chip v-if="submitted[tm.id]" color="success" size="small" variant="tonal" class="ml-2">
            Submitted
          </v-chip>
        </v-card-title>
        <v-card-text class="px-4">
          <v-form :ref="el => (formRefs[i] = el)">
            <!-- Scores per criterion -->
            <v-row v-for="criterion in rubricCriteria" :key="criterion.id" class="mb-1" align="center">
              <v-col cols="12" sm="6">
                <div class="text-body-2 font-weight-medium">{{ criterion.name }}</div>
                <div class="text-caption text-medium-emphasis">Max: {{ criterion.maxScore }}</div>
              </v-col>
              <v-col cols="12" sm="6">
                <v-text-field
                  v-model.number="scores[tm.id][criterion.id]"
                  type="number"
                  min="0"
                  :max="criterion.maxScore"
                  variant="outlined"
                  density="comfortable"
                  :rules="[r => r >= 0 || 'Min 0', r => r <= criterion.maxScore || `Max ${criterion.maxScore}`]"
                  :disabled="submitted[tm.id]"
                />
              </v-col>
            </v-row>

            <v-textarea
              v-model="comments[tm.id].public"
              label="Public comments (shared with teammate)"
              variant="outlined"
              density="comfortable"
              rows="2"
              class="mt-2"
              :disabled="submitted[tm.id]"
            />
            <v-textarea
              v-model="comments[tm.id].private"
              label="Private comments (instructor only)"
              variant="outlined"
              density="comfortable"
              rows="2"
              :disabled="submitted[tm.id]"
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
import { ref, onMounted } from 'vue'
import { api, type ActiveWeek } from '@/api'

interface User { id: number; firstName: string; lastName: string }
interface Criterion { id: number; name: string; maxScore: number; orderIndex: number }

const eligibleWeeks   = ref<ActiveWeek[]>([])
const selectedWeekId  = ref<number | null>(null)
const teammates       = ref<User[]>([])
const rubricCriteria  = ref<Criterion[]>([])
const error           = ref('')

// Per-teammate state
const scores:    Record<number, Record<number, number>> = {}
const comments:  Record<number, { public: string; private: string }> = {}
const submitted: Record<number, boolean> = {}
const submitting: Record<number, boolean> = {}
const formRefs:  any[] = []

function initTeammate(tm: User) {
  if (!scores[tm.id]) {
    scores[tm.id]    = {}
    rubricCriteria.value.forEach(c => { scores[tm.id][c.id] = 0 })
    comments[tm.id]  = { public: '', private: '' }
    submitted[tm.id] = false
    submitting[tm.id] = false
  }
}

async function onWeekChange() {
  if (!selectedWeekId.value) return
  // teammates already loaded
}

async function submitEval(tm: User, i: number) {
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
      evaluateeId:     tm.id,
      weekId:          selectedWeekId.value,
      publicComments:  comments[tm.id].public,
      privateComments: comments[tm.id].private,
      scores:          scoreList,
    })
    submitted[tm.id] = true
  } catch (e: any) {
    error.value = e.message
  } finally {
    submitting[tm.id] = false
  }
}

onMounted(async () => {
  try {
    const me = await api.get<any>('/users/me')
    if (!me.sectionId) return

    // Load past active weeks only (eligible for evaluation)
    const allWeeks  = await api.get<ActiveWeek[]>(`/sections/${me.sectionId}/weeks`)
    const today     = new Date().toISOString().split('T')[0]
    // Active weeks that have already started (previous weeks)
    eligibleWeeks.value = allWeeks.filter(w => w.active && w.weekStart < today)

    // Load team roster
    if (me.teamId) {
      const team = await api.get<any>(`/teams/${me.teamId}`)
      teammates.value = team.students.filter((s: any) => s.id !== me.id)

      // Load rubric criteria from section's rubric
      const section = await api.get<any>(`/sections/${me.sectionId}`)
      if (section.rubric?.criteria) {
        rubricCriteria.value = section.rubric.criteria.sort((a: Criterion, b: Criterion) => a.orderIndex - b.orderIndex)
      }

      teammates.value.forEach(initTeammate)
    }
  } catch (e: any) {
    error.value = e.message
  }
})
</script>
