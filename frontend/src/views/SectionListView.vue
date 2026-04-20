<template>
  <v-container class="pa-6" fluid>
    <div class="d-flex align-center mb-6 ga-3">
      <h1 class="text-h5 font-weight-bold flex-grow-1">Sections</h1>
      <v-btn color="primary" prepend-icon="mdi-plus" :to="{ name: 'section-create' }">
        New Section
      </v-btn>
    </div>

    <!-- Search -->
    <v-text-field
      v-model="search"
      label="Search by name"
      prepend-inner-icon="mdi-magnify"
      variant="outlined"
      density="comfortable"
      clearable
      class="mb-4"
      style="max-width: 400px"
      @update:model-value="load"
    />

    <v-alert v-if="error" type="error" variant="tonal" class="mb-4">{{ error }}</v-alert>

    <v-row v-if="loading">
      <v-col cols="12" class="text-center py-8">
        <v-progress-circular indeterminate color="primary" />
      </v-col>
    </v-row>

    <v-row v-else-if="sections.length === 0">
      <v-col cols="12">
        <v-alert type="info" variant="tonal">
          No sections found. Create your first section to get started.
        </v-alert>
      </v-col>
    </v-row>

    <v-row v-else>
      <v-col
        v-for="section in sections"
        :key="section.id"
        cols="12" md="6" lg="4"
      >
        <v-card rounded="lg" hover>
          <v-card-title class="pa-4 pb-2">
            {{ section.name }}
          </v-card-title>
          <v-card-subtitle class="px-4 pb-2">
            {{ section.startDate }} — {{ section.endDate }}
          </v-card-subtitle>
          <v-card-text class="px-4 pt-0">
            <div class="text-caption text-medium-emphasis mb-1">
              Rubric: {{ section.rubric?.name ?? 'None' }}
            </div>
            <div class="text-caption text-medium-emphasis">
              Teams: {{ section.teamNames.join(', ') || 'None yet' }}
            </div>
          </v-card-text>
          <v-card-actions class="px-4 pb-3">
            <v-btn
              variant="tonal"
              size="small"
              :to="{ name: 'section-edit', params: { id: section.id } }"
            >
              Edit
            </v-btn>
          </v-card-actions>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
</template>

<script lang="ts" setup>
import { ref, onMounted } from 'vue'
import { api, type Section } from '@/api'

const sections = ref<Section[]>([])
const loading  = ref(false)
const error    = ref('')
const search   = ref('')

async function load() {
  loading.value = true
  error.value   = ''
  try {
    const q = search.value ? `?name=${encodeURIComponent(search.value)}` : ''
    sections.value = await api.get<Section[]>(`/sections${q}`)
  } catch (e: any) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>
