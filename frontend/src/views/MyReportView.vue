<template>
  <v-container class="pa-6" style="max-width: 800px">
    <h1 class="text-h5 font-weight-bold mb-2">My Peer Evaluation Report</h1>
    <p class="text-medium-emphasis text-body-2 mb-6">
      View how your teammates evaluated you. Evaluator identities are anonymous.
    </p>

    <!-- Week selector -->
    <v-select
      v-model="selectedWeekId"
      label="Select week"
      :items="weeks"
      :item-title="(w: any) => `Week of ${w.weekStart}`"
      item-value="id"
      variant="outlined"
      density="comfortable"
      style="max-width: 320px"
      class="mb-6"
      @update:model-value="loadReport"
    />

    <v-alert v-if="error" type="error" variant="tonal" class="mb-4">{{ error }}</v-alert>

    <v-progress-circular v-if="loading" indeterminate color="primary" class="d-block mx-auto my-8" />

    <template v-else-if="report">
      <!-- Grade card -->
      <v-card rounded="lg" color="primary" variant="tonal" class="mb-4">
        <v-card-text class="pa-6 text-center">
          <div class="text-h3 font-weight-bold">{{ report.overallGrade }}</div>
          <div class="text-body-1 mt-1">Overall Grade</div>
          <div class="text-caption mt-1">Based on {{ report.evaluatorCount }} evaluation(s)</div>
        </v-card-text>
      </v-card>

      <!-- No evaluations message -->
      <v-alert v-if="report.evaluatorCount === 0" type="info" variant="tonal" class="mb-4">
        No evaluations have been submitted for this week yet.
      </v-alert>

      <template v-else>
        <!-- Criterion breakdown -->
        <v-card rounded="lg" class="mb-4">
          <v-card-title class="pa-4 pb-2">Score by Criterion</v-card-title>
          <v-card-text class="px-4">
            <div
              v-for="c in report.criterionAverages"
              :key="c.criterionId"
              class="mb-3"
            >
              <div class="d-flex justify-space-between text-body-2 mb-1">
                <span>{{ c.criterionName }}</span>
                <span class="font-weight-medium">{{ c.averageScore }} / {{ c.maxScore }}</span>
              </div>
              <v-progress-linear
                :model-value="(c.averageScore / c.maxScore) * 100"
                color="primary"
                rounded
                height="8"
              />
            </div>
          </v-card-text>
        </v-card>

        <!-- Public comments -->
        <v-card v-if="report.publicComments.length > 0" rounded="lg">
          <v-card-title class="pa-4 pb-2">Feedback from Teammates</v-card-title>
          <v-card-text class="px-4">
            <v-list>
              <v-list-item
                v-for="(comment, i) in report.publicComments"
                :key="i"
                prepend-icon="mdi-comment-outline"
              >
                <v-list-item-title class="text-wrap">{{ comment }}</v-list-item-title>
              </v-list-item>
            </v-list>
          </v-card-text>
        </v-card>
        <v-alert v-else type="info" variant="tonal">No written feedback provided this week.</v-alert>
      </template>
    </template>

    <v-alert v-else-if="selectedWeekId && !loading" type="info" variant="tonal">
      Select a week to see your report.
    </v-alert>
  </v-container>
</template>

<script lang="ts" setup>
import { ref, onMounted } from 'vue'
import { api, type ActiveWeek, type PeerEvaluationReport } from '@/api'

const weeks          = ref<ActiveWeek[]>([])
const selectedWeekId = ref<number | null>(null)
const report         = ref<PeerEvaluationReport | null>(null)
const loading        = ref(false)
const error          = ref('')

async function loadReport() {
  if (!selectedWeekId.value) return
  loading.value = true
  error.value   = ''
  report.value  = null
  try {
    report.value = await api.get<PeerEvaluationReport>(
      `/peer-evaluations/me/report?weekId=${selectedWeekId.value}`
    )
  } catch (e: any) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  try {
    const me = await api.get<any>('/users/me')
    if (me.sectionId) {
      const allWeeks = await api.get<ActiveWeek[]>(`/sections/${me.sectionId}/weeks`)
      const today    = new Date().toISOString().split('T')[0]
      weeks.value    = allWeeks.filter(w => w.active && w.weekStart < today)
    }
  } catch (e: any) {
    error.value = e.message
  }
})
</script>
