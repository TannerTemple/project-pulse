<template>
  <v-container>
    <!-- Back button -->
    <v-btn
      class="mb-4"
      prepend-icon="mdi-arrow-left"
      :to="{ name: 'students' }"
      variant="text"
    >
      Back to Students
    </v-btn>

    <v-progress-circular v-if="loadingStudent" class="d-block mx-auto mt-8" indeterminate />

    <template v-if="student">
      <!-- Profile card -->
      <v-card class="mb-6" rounded="lg">
        <v-card-text>
          <v-row align="center">
            <v-col cols="auto">
              <v-avatar color="primary" size="64">
                <span class="text-h6 text-white">
                  {{ student.firstName[0] }}{{ student.lastName[0] }}
                </span>
              </v-avatar>
            </v-col>
            <v-col>
              <h2 class="text-h6 font-weight-bold">
                {{ student.firstName }} {{ student.middleInitial ? student.middleInitial + '. ' : '' }}{{ student.lastName }}
              </h2>
              <div class="text-body-2 text-medium-emphasis">{{ student.email }}</div>
              <div class="mt-1">
                <v-chip class="mr-2" size="small" variant="tonal">
                  {{ student.sectionName ?? 'No section' }}
                </v-chip>
                <v-chip color="primary" size="small" variant="tonal">
                  {{ student.teamName ?? 'No team' }}
                </v-chip>
                <v-chip
                  class="ml-2"
                  :color="student.registrationComplete ? 'success' : 'warning'"
                  size="small"
                  variant="tonal"
                >
                  {{ student.registrationComplete ? 'Registered' : 'Pending' }}
                </v-chip>
              </div>
            </v-col>
          </v-row>
        </v-card-text>
      </v-card>

      <!-- Report tabs -->
      <v-tabs v-model="tab" class="mb-4">
        <v-tab value="peer">Peer Evaluation History</v-tab>
        <v-tab value="war">WAR Activity History</v-tab>
      </v-tabs>

      <!-- Date range selector (shared) -->
      <v-row class="mb-4">
        <v-col cols="6" md="3">
          <v-text-field
            v-model="startDate"
            density="comfortable"
            label="Start date"
            type="date"
            variant="outlined"
          />
        </v-col>
        <v-col cols="6" md="3">
          <v-text-field
            v-model="endDate"
            density="comfortable"
            label="End date"
            type="date"
            variant="outlined"
          />
        </v-col>
        <v-col class="d-flex align-center" cols="auto">
          <v-btn
            color="primary"
            :loading="loadingReport"
            prepend-icon="mdi-magnify"
            variant="flat"
            @click="loadReport"
          >
            Load
          </v-btn>
        </v-col>
      </v-row>

      <v-alert v-if="reportError" class="mb-4" type="error" variant="tonal">
        {{ reportError }}
      </v-alert>

      <v-tabs-window v-model="tab">
        <!-- ── Peer eval tab ──────────────────────────────────────────────── -->
        <v-tabs-window-item value="peer">
          <v-progress-circular v-if="loadingReport && tab === 'peer'" class="d-block mx-auto mt-4" indeterminate />

          <template v-if="peerReport && tab === 'peer'">
            <div v-if="peerReport.weeks.length === 0" class="text-medium-emphasis text-center py-8">
              No peer evaluation data in this date range.
            </div>

            <v-expansion-panels v-else variant="accordion">
              <v-expansion-panel
                v-for="week in peerReport.weeks"
                :key="week.weekId"
              >
                <v-expansion-panel-title>
                  <v-row align="center" no-gutters>
                    <v-col class="font-weight-medium" cols="4">
                      Week of {{ formatDate(week.weekStart) }}
                    </v-col>
                    <v-col cols="4">
                      Grade: <strong>{{ week.grade }}</strong>
                    </v-col>
                    <v-col class="text-right text-caption text-medium-emphasis" cols="4">
                      {{ week.evaluations.length }} evaluator(s)
                    </v-col>
                  </v-row>
                </v-expansion-panel-title>
                <v-expansion-panel-text>
                  <v-table density="compact">
                    <thead>
                      <tr>
                        <th>Evaluator</th>
                        <th
                          v-for="score in week.evaluations[0]?.scores"
                          :key="score.criterionName"
                        >
                          {{ score.criterionName }}
                        </th>
                        <th>Public</th>
                        <th>Private</th>
                      </tr>
                    </thead>
                    <tbody>
                      <tr v-for="ev in week.evaluations" :key="ev.evaluatorId">
                        <td>{{ ev.evaluatorName }}</td>
                        <td v-for="score in ev.scores" :key="score.criterionName">
                          {{ score.score }}
                        </td>
                        <td>{{ ev.publicComments ?? '—' }}</td>
                        <td>{{ ev.privateComments ?? '—' }}</td>
                      </tr>
                    </tbody>
                  </v-table>
                </v-expansion-panel-text>
              </v-expansion-panel>
            </v-expansion-panels>
          </template>
        </v-tabs-window-item>

        <!-- ── WAR tab ────────────────────────────────────────────────────── -->
        <v-tabs-window-item value="war">
          <v-progress-circular v-if="loadingReport && tab === 'war'" class="d-block mx-auto mt-4" indeterminate />

          <template v-if="warReport && tab === 'war'">
            <div v-if="warReport.weeks.length === 0" class="text-medium-emphasis text-center py-8">
              No WAR activity data in this date range.
            </div>

            <div v-for="week in warReport.weeks" v-else :key="week.weekId" class="mb-6">
              <h3 class="text-subtitle-1 font-weight-medium mb-2">
                Week of {{ formatDate(week.weekStart) }}
              </h3>
              <v-table density="compact">
                <thead>
                  <tr>
                    <th>Category</th>
                    <th>Activity</th>
                    <th>Description</th>
                    <th>Planned h</th>
                    <th>Actual h</th>
                    <th>Status</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="act in week.activities" :key="act.id">
                    <td>
                      <v-chip color="primary" size="x-small" variant="tonal">
                        {{ act.category }}
                      </v-chip>
                    </td>
                    <td>{{ act.activity }}</td>
                    <td class="text-medium-emphasis">{{ act.description ?? '—' }}</td>
                    <td>{{ act.plannedHours }}</td>
                    <td>{{ act.actualHours ?? '—' }}</td>
                    <td>
                      <v-chip :color="statusColor(act.status)" size="x-small" variant="tonal">
                        {{ act.status }}
                      </v-chip>
                    </td>
                  </tr>
                </tbody>
              </v-table>
              <v-divider class="mt-4" />
            </div>
          </template>
        </v-tabs-window-item>
      </v-tabs-window>
    </template>
  </v-container>
</template>

<script lang="ts" setup>
  import { onMounted, ref } from 'vue'
  import { useRoute } from 'vue-router'
  import { api, type StudentPeerRangeReport, type StudentWARRangeReport, type User } from '@/api'

  const route = useRoute()
  const studentId = Number(route.params.id)

  const student = ref<User | null>(null)
  const peerReport = ref<StudentPeerRangeReport | null>(null)
  const warReport = ref<StudentWARRangeReport | null>(null)

  const tab = ref('peer')
  const loadingStudent = ref(false)
  const loadingReport = ref(false)
  const reportError = ref('')

  // Default date range: last 16 weeks
  const today = new Date()
  const sixteenWeeksAgo = new Date(today)
  sixteenWeeksAgo.setDate(today.getDate() - 112)
  const startDate = ref(sixteenWeeksAgo.toISOString().slice(0, 10))
  const endDate = ref(today.toISOString().slice(0, 10))

  onMounted(async () => {
    loadingStudent.value = true
    try {
      student.value = await api.get<User>(`/students/${studentId}`)
      await loadReport()
    } catch (error: any) {
      reportError.value = error.message
    } finally {
      loadingStudent.value = false
    }
  })

  async function loadReport () {
    loadingReport.value = true
    reportError.value = ''
    peerReport.value = null
    warReport.value = null
    try {
      const [peer, war] = await Promise.all([
        api.get<StudentPeerRangeReport>(
          `/students/${studentId}/peer-evaluation-report?start=${startDate.value}&end=${endDate.value}`,
        ),
        api.get<StudentWARRangeReport>(
          `/students/${studentId}/war-report?start=${startDate.value}&end=${endDate.value}`,
        ),
      ])
      peerReport.value = peer
      warReport.value = war
    } catch (error: any) {
      reportError.value = error.message
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

  function statusColor (status: string) {
    const map: Record<string, string> = {
      DONE: 'success',
      IN_PROGRESS: 'info',
      UNDER_TESTING: 'warning',
    }
    return map[status] ?? 'default'
  }
</script>
