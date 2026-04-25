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
      class="mb-4"
      clearable
      density="comfortable"
      label="Search by name"
      prepend-inner-icon="mdi-magnify"
      style="max-width: 400px"
      variant="outlined"
      @update:model-value="load"
    />

    <v-alert v-if="error" class="mb-4" type="error" variant="tonal">{{ error }}</v-alert>

    <v-row v-if="loading">
      <v-col class="text-center py-8" cols="12">
        <v-progress-circular color="primary" indeterminate />
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
        cols="12"
        lg="4"
        md="6"
      >
        <v-card hover rounded="lg">
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
              size="small"
              :to="{ name: 'section-detail', params: { id: section.id } }"
              variant="tonal"
            >
              View
            </v-btn>
            <v-btn
              size="small"
              :to="{ name: 'section-edit', params: { id: section.id } }"
              variant="tonal"
            >
              Edit
            </v-btn>
            <v-btn
              prepend-icon="mdi-calendar-clock"
              size="small"
              :to="{ name: 'section-weeks', params: { id: section.id } }"
              variant="tonal"
            >
              Weeks
            </v-btn>
          </v-card-actions>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
</template>

<script lang="ts" setup>
  import { onMounted, ref } from 'vue'
  import { api, type Section } from '@/api'

  const sections = ref<Section[]>([])
  const loading = ref(false)
  const error = ref('')
  const search = ref('')

  async function load () {
    loading.value = true
    error.value = ''
    try {
      const q = search.value ? `?name=${encodeURIComponent(search.value)}` : ''
      sections.value = await api.get<Section[]>(`/sections${q}`)
    } catch (error_: any) {
      error.value = error_.message
    } finally {
      loading.value = false
    }
  }

  onMounted(load)
</script>
