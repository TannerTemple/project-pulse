<template>
  <v-container fluid>
    <h1 class="text-h5 font-weight-bold mb-4">Section Peer Evaluation Report</h1>

    <!-- Filters -->
    <v-row class="mb-4">
      <v-col cols="12" md="4">
        <v-select
          v-model="selectedSectionId"
          density="comfortable"
          item-title="name"
          item-value="id"
          :items="sections"
          label="Section"
          :loading="loadingSections"
          variant="outlined"
          @update:model-value="onSectionChange"
        />
      </v-col>
      <v-col cols="12" md="4">
        <v-select
          v-model="selectedWeekId"
          density="comfortable"
          :disabled="!selectedSectionId"
          item-title="label"
          item-value="id"
          :items="weeks"
          label="Week"
          :loading="loadingWeeks"
          variant="outlined"
          @update:model-value="loadReport"
        />
      </v-col>
    </v-row>

    <v-alert v-if="error" class="mb-4" type="error" variant="tonal">{{ error }}</v-alert>

    <v-progress-circular v-if="loadingReport" class="d-block mx-auto mt-8" indeterminate />

    <template v-if="report && !loadingReport">
      <!-- Summary chips -->
      <v-row class="mb-4">
        <v-col cols="auto">
          <v-chip color="primary" variant="tonal">
            {{ report.students.length }} students
          </v-chip>
        </v-col>
        <v-col cols="auto">
          <v-chip color="success" variant="tonal">
            {{ report.students.filter(s => s.submitted).length }} submitted
          </v-chip>
        </v-col>
        <v-col v-if="report.nonSubmitters.length > 0" cols="auto">
          <v-chip color="error" variant="tonal">
            {{ report.nonSubmitters.length }} missing
          </v-chip>
        </v-col>
      </v-row>

      <!-- Non-submitters warning -->
      <v-alert
        v-if="report.nonSubmitters.length > 0"
        class="mb-4"
        type="warning"
        variant="tonal"
      >
        <strong>Did not submit:</strong> {{ report.nonSubmitters.join(', ') }}
      </v-alert>

      <!-- Student rows -->
      <v-expansion-panels variant="accordion">
        <v-expansion-panel
          v-for="student in report.students"
          :key="student.studentId"
        >
          <v-expansion-panel-title>
            <v-row align="center" no-gutters>
              <v-col class="font-weight-medium" cols="4">
                {{ student.studentName }}
              </v-col>
              <v-col cols="3">
                <v-chip
                  :color="student.submitted ? 'success' : 'warning'"
                  size="small"
                  variant="tonal"
                >
                  {{ student.submitted ? 'Received evals' : 'No evals' }}
                </v-chip>
              </v-col>
              <v-col cols="3">
                <span v-if="student.submitted" class="text-body-2">
                  Grade: <strong>{{ student.grade }}</strong>
                </span>
              </v-col>
              <v-col class="text-right text-caption text-medium-emphasis" cols="2">
                {{ student.evaluations.length }} evaluator(s)
              </v-col>
            </v-row>
          </v-expansion-panel-title>

          <v-expansion-panel-text>
            <div v-if="student.evaluations.length === 0" class="text-medium-emphasis py-2">
              No evaluations received this week.
            </div>

            <v-table v-else density="compact">
              <thead>
                <tr>
                  <th>Evaluator</th>
                  <th
                    v-for="score in student.evaluations[0]?.scores"
                    :key="score.criterionName"
                  >
                    {{ score.criterionName }}
                  </th>
                  <th>Public Comment</th>
                  <th v-if="showPrivateComments">Private Comment</th>
                </tr>
              </thead>
              <tbody>
                <tr
                  v-for="ev in student.evaluations"
                  :key="ev.evaluatorId"
                >
                  <td>{{ ev.evaluatorName }}</td>
                  <td v-for="score in ev.scores" :key="score.criterionName">
                    {{ score.score }}
                  </td>
                  <td>{{ ev.publicComments ?? '—' }}</td>
                  <td v-if="showPrivateComments">{{ ev.privateComments ?? '—' }}</td>
                </tr>
              </tbody>
            </v-table>
          </v-expansion-panel-text>
        </v-expansion-panel>
      </v-expansion-panels>
    </template>

    <v-alert
      v-else-if="!loadingReport && !report && selectedWeekId"
      type="info"
      variant="tonal"
    >
      No peer evaluation data found for this week.
    </v-alert>
  </v-container>
</template>

<script lang="ts" setup>
  import { computed, onMounted, ref } from 'vue'
  import { api, type Section, type SectionPeerReport } from '@/api'
  import { useAuthStore } from '@/stores/auth'

  interface WeekOption {
    id: number
    label: string
  }

  const sections = ref<Section[]>([])
  const weeks = ref<WeekOption[]>([])
  const report = ref<SectionPeerReport | null>(null)

  const selectedSectionId = ref<number | null>(null)
  const selectedWeekId = ref<number | null>(null)

  const loadingSections = ref(false)
  const loadingWeeks = ref(false)
  const loadingReport = ref(false)
  const error = ref('')
  const auth = useAuthStore()
  const showPrivateComments = computed(() => auth.role === 'INSTRUCTOR')

  onMounted(async () => {
    loadingSections.value = true
    try {
      if (auth.role === 'INSTRUCTOR') {
        const me = await api.get<any>('/users/me').catch(() => null)
        const myId = me?.id ?? null
        const allTeams = await api.get<any[]>('/teams')
        const myTeams = myId
          ? allTeams.filter(t => t.instructors?.some((i: any) => i.id === myId))
          : []
        const sectionIds = new Set(myTeams.map((t: any) => t.sectionId))
        const allSections = await api.get<Section[]>('/sections')
        sections.value = allSections.filter(s => sectionIds.has(s.id))
      } else {
        sections.value = await api.get<Section[]>('/sections')
      }
    } catch (error_: any) {
      error.value = error_.message
    } finally {
      loadingSections.value = false
    }
  })

  async function onSectionChange () {
    selectedWeekId.value = null
    report.value = null
    weeks.value = []
    if (!selectedSectionId.value) return
    loadingWeeks.value = true
    try {
      const raw = await api.get<{ id: number, weekStart: string, active: boolean }[]>(
        `/sections/${selectedSectionId.value}/weeks`,
      )
      weeks.value = raw.map(w => ({
        id: w.id,
        label: `Week of ${formatDate(w.weekStart)}${w.active ? '' : ' (inactive)'}`,
      }))
    } catch (error_: any) {
      error.value = error_.message
    } finally {
      loadingWeeks.value = false
    }
  }

  async function loadReport () {
    if (!selectedSectionId.value || !selectedWeekId.value) return
    loadingReport.value = true
    error.value = ''
    report.value = null
    try {
      report.value = await api.get<SectionPeerReport>(
        `/sections/${selectedSectionId.value}/peer-evaluation-report?weekId=${selectedWeekId.value}`,
      )
    } catch (error_: any) {
      error.value = error_.message
    } finally {
      loadingReport.value = false
    }
  }

  function formatDate (iso: string) {
    return new Date(iso + 'T12:00:00').toLocaleDateString('en-US', {
      month: 'short',
      day: 'numeric',
      year: 'numeric',
    })
  }
</script>
