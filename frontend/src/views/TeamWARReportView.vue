<template>
  <v-container fluid>
    <h1 class="text-h5 font-weight-bold mb-4">Team WAR Report</h1>

    <!-- Filters -->
    <v-row class="mb-4">
      <v-col cols="12" md="4">
        <v-select
          v-model="selectedTeamId"
          density="comfortable"
          item-title="name"
          item-value="id"
          :items="teams"
          label="Team"
          :loading="loadingTeams"
          variant="outlined"
          @update:model-value="onTeamChange"
        />
      </v-col>
      <v-col cols="12" md="4">
        <v-select
          v-model="selectedWeekId"
          density="comfortable"
          :disabled="!selectedTeamId"
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
      <!-- Summary -->
      <v-row class="mb-4">
        <v-col cols="auto">
          <v-chip color="primary" variant="tonal">{{ report.entries.length }} students</v-chip>
        </v-col>
        <v-col cols="auto">
          <v-chip color="success" variant="tonal">
            {{ report.entries.filter(e => e.activities.length > 0).length }} submitted
          </v-chip>
        </v-col>
        <v-col v-if="report.nonSubmitters.length > 0" cols="auto">
          <v-chip color="error" variant="tonal">{{ report.nonSubmitters.length }} missing</v-chip>
        </v-col>
      </v-row>

      <v-alert
        v-if="report.nonSubmitters.length > 0"
        class="mb-4"
        type="warning"
        variant="tonal"
      >
        <strong>Did not submit a WAR this week:</strong> {{ report.nonSubmitters.join(', ') }}
      </v-alert>

      <!-- Per-student sections -->
      <div v-for="entry in report.entries" :key="entry.studentId" class="mb-6">
        <div class="d-flex align-center mb-2 ga-2">
          <v-icon color="primary" size="small">mdi-account</v-icon>
          <span class="font-weight-medium">{{ entry.studentName }}</span>
          <v-chip
            :color="entry.activities.length > 0 ? 'success' : 'warning'"
            size="x-small"
            variant="tonal"
          >
            {{ entry.activities.length > 0 ? `${entry.activities.length} activities` : 'No submission' }}
          </v-chip>
        </div>

        <v-table v-if="entry.activities.length > 0" density="compact">
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
            <tr v-for="act in entry.activities" :key="act.id">
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
                <v-chip
                  :color="statusColor(act.status)"
                  size="x-small"
                  variant="tonal"
                >
                  {{ act.status }}
                </v-chip>
              </td>
            </tr>
          </tbody>
        </v-table>

        <p v-else class="text-medium-emphasis text-body-2 ml-6">
          No activities submitted for this week.
        </p>
        <v-divider class="mt-4" />
      </div>
    </template>

    <v-alert
      v-else-if="!loadingReport && !report && selectedWeekId"
      type="info"
      variant="tonal"
    >
      No WAR data found for this team and week.
    </v-alert>
  </v-container>
</template>

<script lang="ts" setup>
  import { onMounted, ref } from 'vue'
  import { api, type Team, type TeamWARReport } from '@/api'
  import { useAuthStore } from '@/stores/auth'

  interface WeekOption {
    id: number
    label: string
  }

  const auth = useAuthStore()
  const teams = ref<Team[]>([])
  const weeks = ref<WeekOption[]>([])
  const report = ref<TeamWARReport | null>(null)

  const selectedTeamId = ref<number | null>(null)
  const selectedWeekId = ref<number | null>(null)

  const loadingTeams = ref(false)
  const loadingWeeks = ref(false)
  const loadingReport = ref(false)
  const error = ref('')

  onMounted(async () => {
    loadingTeams.value = true
    try {
      let myUserId: number | null = null
      if (auth.role === 'INSTRUCTOR') {
        const me = await api.get<any>('/users/me').catch(() => null)
        if (me) myUserId = me.id
      }
      const all = await api.get<Team[]>('/teams')
      teams.value = myUserId
        ? all.filter(t => t.instructors?.some((i: any) => i.id === myUserId))
        : all
    } catch (error_: any) {
      error.value = error_.message
    } finally {
      loadingTeams.value = false
    }
  })

  async function onTeamChange () {
    selectedWeekId.value = null
    report.value = null
    weeks.value = []
    if (!selectedTeamId.value) return

    // Find which section this team belongs to, then load its weeks
    const team = teams.value.find(t => t.id === selectedTeamId.value)
    if (!team?.sectionId) return

    loadingWeeks.value = true
    try {
      const raw = await api.get<{ id: number, weekStart: string, active: boolean }[]>(
        `/sections/${team.sectionId}/weeks`,
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
    if (!selectedTeamId.value || !selectedWeekId.value) return
    loadingReport.value = true
    error.value = ''
    report.value = null
    try {
      report.value = await api.get<TeamWARReport>(
        `/teams/${selectedTeamId.value}/war-report?weekId=${selectedWeekId.value}`,
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

  function statusColor (status: string) {
    const map: Record<string, string> = {
      DONE: 'success',
      IN_PROGRESS: 'info',
      UNDER_TESTING: 'warning',
    }
    return map[status] ?? 'default'
  }
</script>
