<template>
  <v-container class="pa-6" fluid>
    <div class="d-flex align-center mb-6 ga-3">
      <h1 class="text-h5 font-weight-bold flex-grow-1">Teams</h1>
      <v-btn
        v-if="auth.role === 'ADMIN'"
        color="primary"
        prepend-icon="mdi-plus"
        :to="{ name: 'team-create' }"
      >
        New Team
      </v-btn>
    </div>

    <!-- Filters -->
    <v-row class="mb-4">
      <v-col cols="12" sm="6" md="4">
        <v-text-field
          v-model="searchName"
          label="Team name"
          prepend-inner-icon="mdi-magnify"
          variant="outlined"
          density="comfortable"
          clearable
          @update:model-value="load"
        />
      </v-col>
      <v-col v-if="auth.role === 'ADMIN'" cols="12" sm="6" md="4">
        <v-select
          v-model="sectionId"
          label="Filter by section"
          :items="sections"
          item-title="name"
          item-value="id"
          variant="outlined"
          density="comfortable"
          clearable
          @update:model-value="load"
        />
      </v-col>
    </v-row>

    <v-alert v-if="error" type="error" variant="tonal" class="mb-4">{{ error }}</v-alert>

    <v-progress-circular v-if="loading" indeterminate color="primary" class="d-block mx-auto my-8" />

    <v-row v-else-if="teams.length === 0">
      <v-col>
        <v-alert type="info" variant="tonal">No teams found.</v-alert>
      </v-col>
    </v-row>

    <v-row v-else>
      <v-col v-for="team in teams" :key="team.id" cols="12" md="6" lg="4">
        <v-card rounded="lg" hover>
          <v-card-title class="pa-4 pb-1">{{ team.name }}</v-card-title>
          <v-card-subtitle class="px-4 pb-2">{{ team.sectionName }}</v-card-subtitle>
          <v-card-text class="px-4 pt-0">
            <div class="text-caption text-medium-emphasis mb-1">
              Students: {{ team.students?.map((s: any) => `${s.firstName} ${s.lastName}`).join(', ') || 'None' }}
            </div>
            <div class="text-caption text-medium-emphasis">
              Instructors: {{ team.instructors?.map((i: any) => `${i.firstName} ${i.lastName}`).join(', ') || 'None' }}
            </div>
          </v-card-text>
          <v-card-actions v-if="auth.role === 'ADMIN'" class="px-4 pb-3">
            <v-btn variant="tonal" size="small" :to="{ name: 'team-edit', params: { id: team.id } }">
              Edit
            </v-btn>
            <v-btn variant="text" size="small" color="error" @click="confirmDelete(team)">
              Delete
            </v-btn>
          </v-card-actions>
        </v-card>
      </v-col>
    </v-row>

    <!-- Delete confirmation -->
    <v-dialog v-model="deleteDialog" max-width="400">
      <v-card rounded="lg">
        <v-card-title class="pa-4">Delete team?</v-card-title>
        <v-card-text class="px-4">
          Delete <strong>{{ teamToDelete?.name }}</strong>? This cannot be undone.
        </v-card-text>
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
import { ref, onMounted } from 'vue'
import { api } from '@/api'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()

interface Team {
  id: number; name: string; sectionId: number; sectionName: string
  students: any[]; instructors: any[]
}
interface Section { id: number; name: string }

const teams       = ref<Team[]>([])
const sections    = ref<Section[]>([])
const searchName  = ref('')
const sectionId   = ref<number | null>(null)
const loading     = ref(false)
const error       = ref('')
const deleteDialog = ref(false)
const teamToDelete = ref<Team | null>(null)
const deleting    = ref(false)

async function load() {
  loading.value = true
  error.value   = ''
  try {
    const params = new URLSearchParams()
    if (searchName.value) params.set('name', searchName.value)
    if (sectionId.value)  params.set('sectionId', String(sectionId.value))
    const q = params.toString() ? `?${params}` : ''
    teams.value = await api.get<Team[]>(`/teams${q}`)
  } catch (e: any) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}

function confirmDelete(team: Team) {
  teamToDelete.value = team
  deleteDialog.value = true
}

async function doDelete() {
  if (!teamToDelete.value) return
  deleting.value = true
  try {
    await api.delete(`/teams/${teamToDelete.value.id}`)
    deleteDialog.value = false
    await load()
  } catch (e: any) {
    error.value = e.message
  } finally {
    deleting.value = false
  }
}

onMounted(async () => {
  if (auth.role === 'ADMIN') {
    sections.value = await api.get<Section[]>('/sections').catch(() => [])
  }
  await load()
})
</script>
