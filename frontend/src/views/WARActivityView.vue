<template>
  <v-container class="pa-6" fluid>
    <h1 class="text-h5 font-weight-bold mb-2">Weekly Activity Report</h1>
    <p class="text-medium-emphasis text-body-2 mb-6">Log your Work, Accomplishments, and Results for the selected week.</p>

    <!-- Week selector -->
    <v-select
      v-model="selectedWeekId"
      class="mb-4"
      density="comfortable"
      :item-title="(w: any) => formatWeekLabel(w)"
      item-value="id"
      :items="weeks"
      label="Select week"
      style="max-width: 320px"
      variant="outlined"
      @update:model-value="loadActivities"
    />

    <v-alert
      v-if="error"
      class="mb-4"
      closable
      type="error"
      variant="tonal"
      @click:close="error = ''"
    >{{ error }}</v-alert>

    <div v-if="selectedWeekId">
      <!-- Activity list -->
      <div class="d-flex align-center mb-3 ga-2">
        <h2 class="text-subtitle-1 font-weight-medium flex-grow-1">
          Activities ({{ activities.length }})
        </h2>
        <v-btn color="primary" prepend-icon="mdi-plus" size="small" @click="openForm()">
          Add Activity
        </v-btn>
      </div>

      <v-progress-circular v-if="loading" class="d-block mx-auto my-8" color="primary" indeterminate />

      <v-alert v-else-if="activities.length === 0" type="info" variant="tonal">
        No activities logged for this week yet.
      </v-alert>

      <v-table v-else hover>
        <thead>
          <tr>
            <th>Category</th>
            <th>Activity</th>
            <th>Planned h</th>
            <th>Actual h</th>
            <th>Status</th>
            <th />
          </tr>
        </thead>
        <tbody>
          <tr v-for="a in activities" :key="a.id">
            <td>
              <v-chip :color="categoryColor(a.category)" size="small" variant="tonal">
                {{ a.category }}
              </v-chip>
            </td>
            <td>
              <div class="font-weight-medium">{{ a.activity }}</div>
              <div v-if="a.description" class="text-caption text-medium-emphasis">{{ a.description }}</div>
            </td>
            <td>{{ a.plannedHours }}</td>
            <td>{{ a.actualHours ?? '—' }}</td>
            <td>
              <v-chip :color="statusColor(a.status)" size="small" variant="tonal">{{ a.status }}</v-chip>
            </td>
            <td>
              <v-btn icon="mdi-pencil" size="small" variant="text" @click="openForm(a)" />
              <v-btn
                color="error"
                icon="mdi-delete"
                size="small"
                variant="text"
                @click="confirmDelete(a)"
              />
            </td>
          </tr>
        </tbody>
      </v-table>

      <!-- Summary -->
      <v-card v-if="activities.length > 0" class="mt-4 pa-4" rounded="lg" variant="tonal">
        <v-row>
          <v-col class="text-center" cols="6" sm="3">
            <div class="text-h6">{{ totalPlanned.toFixed(1) }}</div>
            <div class="text-caption">Planned hours</div>
          </v-col>
          <v-col class="text-center" cols="6" sm="3">
            <div class="text-h6">{{ totalActual.toFixed(1) }}</div>
            <div class="text-caption">Actual hours</div>
          </v-col>
          <v-col class="text-center" cols="6" sm="3">
            <div class="text-h6">{{ activities.filter(a => a.status === 'COMPLETED').length }}</div>
            <div class="text-caption">Completed</div>
          </v-col>
          <v-col class="text-center" cols="6" sm="3">
            <div class="text-h6">{{ activities.filter(a => a.status === 'IN_PROGRESS').length }}</div>
            <div class="text-caption">In progress</div>
          </v-col>
        </v-row>
      </v-card>
    </div>

    <!-- Add/Edit dialog -->
    <v-dialog v-model="formDialog" max-width="600">
      <v-card rounded="lg">
        <v-card-title class="pa-4">{{ editing ? 'Edit Activity' : 'Add Activity' }}</v-card-title>
        <v-card-text class="px-4">
          <v-alert v-if="formError" class="mb-3" type="error" variant="tonal">{{ formError }}</v-alert>
          <v-form ref="formRef">
            <v-select
              v-model="form.category"
              class="mb-3"
              density="comfortable"
              :items="categories"
              label="Category"
              :rules="[r => !!r || 'Required']"
              variant="outlined"
            />
            <v-text-field
              v-model="form.activity"
              class="mb-3"
              density="comfortable"
              label="Activity title"
              :rules="[r => !!r || 'Required']"
              variant="outlined"
            />
            <v-textarea
              v-model="form.description"
              class="mb-3"
              density="comfortable"
              label="Description (optional)"
              rows="2"
              variant="outlined"
            />
            <v-row>
              <v-col cols="6">
                <v-text-field
                  v-model.number="form.plannedHours"
                  density="comfortable"
                  label="Planned hours"
                  min="0"
                  :rules="[r => r >= 0 || 'Must be ≥ 0']"
                  step="0.5"
                  type="number"
                  variant="outlined"
                />
              </v-col>
              <v-col cols="6">
                <v-text-field
                  v-model.number="form.actualHours"
                  density="comfortable"
                  label="Actual hours"
                  min="0"
                  placeholder="Leave blank if not done"
                  step="0.5"
                  type="number"
                  variant="outlined"
                />
              </v-col>
            </v-row>
            <v-select
              v-model="form.status"
              density="comfortable"
              :items="statuses"
              label="Status"
              :rules="[r => !!r || 'Required']"
              variant="outlined"
            />
          </v-form>
        </v-card-text>
        <v-card-actions class="px-4 pb-4">
          <v-spacer />
          <v-btn variant="text" @click="formDialog = false">Cancel</v-btn>
          <v-btn color="primary" :loading="saving" @click="save">Save</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Delete confirmation -->
    <v-dialog v-model="deleteDialog" max-width="400">
      <v-card rounded="lg">
        <v-card-title class="pa-4">Delete activity?</v-card-title>
        <v-card-text class="px-4">Delete <strong>{{ toDelete?.activity }}</strong>?</v-card-text>
        <v-card-actions class="px-4 pb-4">
          <v-spacer />
          <v-btn variant="text" @click="deleteDialog = false">Cancel</v-btn>
          <v-btn color="error" :loading="deleting" @click="doDelete">Delete</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </v-container>
</template>

<script lang="ts" setup>
  import { computed, onMounted, ref } from 'vue'
  import { type ActiveWeek, api, type WARActivity } from '@/api'

  const weeks = ref<ActiveWeek[]>([])
  const activities = ref<WARActivity[]>([])
  const selectedWeekId = ref<number | null>(null)
  const loading = ref(false)
  const error = ref('')

  const formDialog = ref(false)
  const formRef = ref()
  const formError = ref('')
  const saving = ref(false)
  const editing = ref<WARActivity | null>(null)

  const deleteDialog = ref(false)
  const deleting = ref(false)
  const toDelete = ref<WARActivity | null>(null)

  function blankForm () {
    return {
      category: '',
      activity: '',
      description: '',
      plannedHours: 0,
      actualHours: null as number | null,
      status: 'IN_PROGRESS',
    }
  }
  const form = ref(blankForm())

  const categories = [
    'DEVELOPMENT', 'TESTING', 'BUGFIX', 'COMMUNICATION', 'DOCUMENTATION',
    'DESIGN', 'PLANNING', 'LEARNING', 'DEPLOYMENT', 'SUPPORT', 'MISCELLANEOUS',
  ]
  const statuses = ['IN_PROGRESS', 'UNDER_TESTING', 'DONE']

  const totalPlanned = computed(() => activities.value.reduce((s, a) => s + a.plannedHours, 0))
  const totalActual = computed(() => activities.value.reduce((s, a) => s + (a.actualHours ?? 0), 0))

  function formatWeekLabel (w: ActiveWeek) {
    return `Week of ${w.weekStart}${w.active ? '' : ' (inactive)'}`
  }

  function categoryColor (c: string): string {
    const map: Record<string, string> = {
      DEVELOPMENT: 'blue', TESTING: 'orange', BUGFIX: 'red', COMMUNICATION: 'purple',
      DOCUMENTATION: 'teal', DESIGN: 'pink', PLANNING: 'indigo', LEARNING: 'cyan',
      DEPLOYMENT: 'green', SUPPORT: 'amber', MISCELLANEOUS: 'grey',
    }
    return map[c] ?? 'grey'
  }

  function statusColor (s: string): string {
    const map: Record<string, string> = { IN_PROGRESS: 'blue', UNDER_TESTING: 'orange', DONE: 'success' }
    return map[s] ?? 'grey'
  }

  async function loadActivities () {
    if (!selectedWeekId.value) return
    loading.value = true
    error.value = ''
    try {
      activities.value = await api.get<WARActivity[]>(`/war-activities?weekId=${selectedWeekId.value}`)
    } catch (error_: any) {
      error.value = error_.message
    } finally {
      loading.value = false
    }
  }

  function openForm (activity?: WARActivity) {
    formError.value = ''
    editing.value = activity ?? null
    form.value = activity
      ? {
        category: activity.category,
        activity: activity.activity,
        description: activity.description ?? '',
        plannedHours: activity.plannedHours,
        actualHours: activity.actualHours ?? null,
        status: activity.status,
      }
      : blankForm()
    formDialog.value = true
  }

  async function save () {
    const { valid } = await formRef.value.validate()
    if (!valid) return
    saving.value = true
    formError.value = ''
    try {
      const payload = {
        ...form.value,
        weekId: selectedWeekId.value,
        actualHours: form.value.actualHours || null,
      }
      await (editing.value ? api.put(`/war-activities/${editing.value.id}`, payload) : api.post('/war-activities', payload))
      formDialog.value = false
      await loadActivities()
    } catch (error_: any) {
      formError.value = error_.message
    } finally {
      saving.value = false
    }
  }

  function confirmDelete (a: WARActivity) {
    toDelete.value = a
    deleteDialog.value = true
  }

  async function doDelete () {
    if (!toDelete.value) return
    deleting.value = true
    try {
      await api.delete(`/war-activities/${toDelete.value.id}`)
      deleteDialog.value = false
      await loadActivities()
    } catch (error_: any) {
      error.value = error_.message
    } finally {
      deleting.value = false
    }
  }

  onMounted(async () => {
    // Load weeks for this student's section — use the section's active weeks
    const me = await api.get<any>('/users/me').catch(() => null)
    if (me?.sectionId) {
      // get all weeks (not just active) since students can submit WAR for any past week
      const allWeeks = await api.get<ActiveWeek[]>(`/sections/${me.sectionId}/weeks`).catch(() => [])
      // filter to past or current weeks only
      const today = new Date().toISOString().split('T')[0]
      weeks.value = allWeeks.filter(w => w.weekStart <= today)
      if (weeks.value.length > 0) {
        selectedWeekId.value = weeks.value.at(-1)!.id
        await loadActivities()
      }
    }
  })
</script>
