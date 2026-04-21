<template>
  <v-container>
    <v-row class="mb-4">
      <v-col>
        <h1 class="text-h5 font-weight-bold">Active Week Setup</h1>
        <p class="text-medium-emphasis">
          Configure which weeks students may submit peer evaluations (UC-6).
          All weeks are active by default — toggle any to mark as inactive (e.g., holiday break).
        </p>
      </v-col>
    </v-row>

    <!-- Section selector -->
    <v-row>
      <v-col cols="12" md="5">
        <v-select
          v-model="selectedSectionId"
          density="comfortable"
          item-title="name"
          item-value="id"
          :items="sections"
          label="Select a section"
          :loading="loadingSections"
          variant="outlined"
          @update:model-value="loadWeeks"
        />
      </v-col>
    </v-row>

    <v-alert
      v-if="error"
      class="mb-4"
      closable
      type="error"
      @click:close="error = ''"
    >
      {{ error }}
    </v-alert>
    <v-alert
      v-if="saved"
      class="mb-4"
      closable
      type="success"
      @click:close="saved = false"
    >
      Active week configuration saved.
    </v-alert>

    <!-- No weeks yet — generate them -->
    <template v-if="selectedSectionId && weeks.length === 0 && !loadingWeeks">
      <v-alert class="mb-4" type="info">
        No weeks configured for this section yet. Click "Generate Weeks" to create
        Monday-aligned weeks from the section's start to end date.
      </v-alert>
      <v-btn
        color="primary"
        :loading="saving"
        prepend-icon="mdi-calendar-plus"
        @click="generateWeeks"
      >
        Generate Weeks
      </v-btn>
    </template>

    <!-- Week list -->
    <template v-if="weeks.length > 0">
      <v-row align="center" class="mb-2">
        <v-col>
          <span class="text-body-2 text-medium-emphasis">
            {{ weeks.length }} weeks · {{ activeCount }} active · {{ inactiveCount }} inactive
          </span>
        </v-col>
        <v-col cols="auto">
          <v-btn
            color="primary"
            :loading="saving"
            prepend-icon="mdi-content-save"
            variant="flat"
            @click="save"
          >
            Save Configuration
          </v-btn>
        </v-col>
      </v-row>

      <v-card rounded="lg" variant="outlined">
        <v-list lines="one">
          <template v-for="(week, idx) in weeks" :key="week.id ?? idx">
            <v-divider v-if="idx > 0" />
            <v-list-item>
              <template #prepend>
                <v-icon :color="week.active ? 'success' : 'error'">
                  {{ week.active ? 'mdi-calendar-check' : 'mdi-calendar-remove' }}
                </v-icon>
              </template>
              <v-list-item-title>
                Week of {{ formatDate(week.weekStart) }}
              </v-list-item-title>
              <v-list-item-subtitle>
                <v-chip
                  :color="week.active ? 'success' : 'error'"
                  size="small"
                  variant="tonal"
                >
                  {{ week.active ? 'Active' : 'Inactive' }}
                </v-chip>
              </v-list-item-subtitle>
              <template #append>
                <v-switch
                  v-model="week.active"
                  color="success"
                  density="compact"
                  hide-details
                  inset
                />
              </template>
            </v-list-item>
          </template>
        </v-list>
      </v-card>

      <v-row class="mt-4">
        <v-col cols="auto">
          <v-btn
            color="primary"
            :loading="saving"
            prepend-icon="mdi-content-save"
            variant="flat"
            @click="save"
          >
            Save Configuration
          </v-btn>
        </v-col>
        <v-col cols="auto">
          <v-btn
            prepend-icon="mdi-check-all"
            variant="text"
            @click="setAll(true)"
          >
            All Active
          </v-btn>
        </v-col>
        <v-col cols="auto">
          <v-btn
            prepend-icon="mdi-close-circle-multiple"
            variant="text"
            @click="setAll(false)"
          >
            All Inactive
          </v-btn>
        </v-col>
      </v-row>
    </template>

    <v-progress-circular v-if="loadingWeeks" class="mt-8 d-block mx-auto" indeterminate />
  </v-container>
</template>

<script lang="ts" setup>
  import { computed, onMounted, ref } from 'vue'
  import { useRoute } from 'vue-router'
  import { api, type Section } from '@/api'

  interface WeekRow {
    id: number | null
    weekStart: string
    active: boolean
  }

  const route = useRoute()

  const sections = ref<Section[]>([])
  const selectedSectionId = ref<number | null>(null)
  const weeks = ref<WeekRow[]>([])
  const loadingSections = ref(false)
  const loadingWeeks = ref(false)
  const saving = ref(false)
  const error = ref('')
  const saved = ref(false)

  const activeCount = computed(() => weeks.value.filter(w => w.active).length)
  const inactiveCount = computed(() => weeks.value.filter(w => !w.active).length)

  onMounted(async () => {
    loadingSections.value = true
    try {
      sections.value = await api.get<Section[]>('/sections')
      // If navigated directly with a section id in the path, pre-select it
      const idParam = route.params.id
      if (idParam) {
        selectedSectionId.value = Number(idParam)
        await loadWeeks()
      }
    } catch (error_: any) {
      error.value = error_.message
    } finally {
      loadingSections.value = false
    }
  })

  async function loadWeeks () {
    if (!selectedSectionId.value) return
    loadingWeeks.value = true
    saved.value = false
    error.value = ''
    try {
      const raw = await api.get<{ id: number, weekStart: string, active: boolean }[]>(
        `/sections/${selectedSectionId.value}/weeks`,
      )
      weeks.value = raw.map(w => ({ id: w.id, weekStart: w.weekStart, active: w.active }))
    } catch (error_: any) {
      error.value = error_.message
    } finally {
      loadingWeeks.value = false
    }
  }

  async function generateWeeks () {
    // POST with empty inactive set → backend generates all weeks as active
    saving.value = true
    error.value = ''
    try {
      const raw = await api.post<{ id: number, weekStart: string, active: boolean }[]>(
        `/sections/${selectedSectionId.value}/weeks`,
        { inactiveWeekStarts: [] },
      )
      weeks.value = raw.map(w => ({ id: w.id, weekStart: w.weekStart, active: w.active }))
      saved.value = true
    } catch (error_: any) {
      error.value = error_.message
    } finally {
      saving.value = false
    }
  }

  async function save () {
    saving.value = true
    saved.value = false
    error.value = ''
    try {
      const inactiveWeekStarts = weeks.value
        .filter(w => !w.active)
        .map(w => w.weekStart)
      const raw = await api.post<{ id: number, weekStart: string, active: boolean }[]>(
        `/sections/${selectedSectionId.value}/weeks`,
        { inactiveWeekStarts },
      )
      weeks.value = raw.map(w => ({ id: w.id, weekStart: w.weekStart, active: w.active }))
      saved.value = true
    } catch (error_: any) {
      error.value = error_.message
    } finally {
      saving.value = false
    }
  }

  function setAll (active: boolean) {
    for (const w of weeks.value) w.active = active
  }

  function formatDate (iso: string) {
    return new Date(iso + 'T12:00:00').toLocaleDateString('en-US', {
      weekday: 'short',
      month: 'short',
      day: 'numeric',
      year: 'numeric',
    })
  }
</script>
