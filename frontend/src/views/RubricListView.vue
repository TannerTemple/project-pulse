<template>
  <v-container class="pa-6" fluid>
    <div class="d-flex align-center mb-6 ga-3">
      <h1 class="text-h5 font-weight-bold flex-grow-1">Rubrics</h1>
      <v-btn color="primary" prepend-icon="mdi-plus" :to="{ name: 'rubric-create' }">
        New Rubric
      </v-btn>
    </div>

    <v-alert v-if="error" type="error" variant="tonal" class="mb-4">{{ error }}</v-alert>

    <v-progress-circular v-if="loading" indeterminate color="primary" class="d-block mx-auto my-8" />

    <v-row v-else-if="rubrics.length > 0">
      <v-col v-for="rubric in rubrics" :key="rubric.id" cols="12" md="6">
        <v-card rounded="lg">
          <v-card-title class="pa-4 pb-2">{{ rubric.name }}</v-card-title>
          <v-card-text class="px-4 pt-0">
            <v-chip-group>
              <v-chip
                v-for="c in rubric.criteria"
                :key="c.id"
                size="small"
                variant="tonal"
              >
                {{ c.name }} ({{ c.maxScore }})
              </v-chip>
            </v-chip-group>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>

    <v-alert v-else type="info" variant="tonal">No rubrics yet. Create one to get started.</v-alert>
  </v-container>
</template>

<script lang="ts" setup>
import { ref, onMounted } from 'vue'
import { api, type Rubric } from '@/api'

const rubrics = ref<Rubric[]>([])
const loading = ref(false)
const error   = ref('')

async function load() {
  loading.value = true
  try {
    rubrics.value = await api.get<Rubric[]>('/rubrics')
  } catch (e: any) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>
