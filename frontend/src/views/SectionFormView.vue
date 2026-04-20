<template>
  <v-container class="pa-6" style="max-width: 700px">
    <div class="d-flex align-center mb-6 ga-3">
      <v-btn icon="mdi-arrow-left" variant="text" :to="{ name: 'sections' }" />
      <h1 class="text-h5 font-weight-bold">{{ isEdit ? 'Edit Section' : 'New Section' }}</h1>
    </div>

    <v-alert v-if="error" type="error" variant="tonal" class="mb-4">{{ error }}</v-alert>

    <v-card rounded="lg">
      <v-card-text class="pa-6">
        <v-form ref="formRef" @submit.prevent="handleSubmit">
          <v-text-field
            v-model="form.name"
            label="Section name"
            variant="outlined"
            density="comfortable"
            :rules="[rules.required]"
            class="mb-3"
            placeholder="e.g. 2023-2024"
          />

          <v-row>
            <v-col cols="12" sm="6">
              <v-text-field
                v-model="form.startDate"
                label="Start date"
                type="date"
                variant="outlined"
                density="comfortable"
                :rules="[rules.required]"
              />
            </v-col>
            <v-col cols="12" sm="6">
              <v-text-field
                v-model="form.endDate"
                label="End date"
                type="date"
                variant="outlined"
                density="comfortable"
                :rules="[rules.required]"
              />
            </v-col>
          </v-row>

          <v-select
            v-model="form.rubricId"
            label="Rubric"
            :items="rubrics"
            item-title="name"
            item-value="id"
            variant="outlined"
            density="comfortable"
            :rules="[rules.required]"
            class="mb-3"
          />

          <div class="d-flex justify-end ga-2 mt-4">
            <v-btn variant="text" :to="{ name: 'sections' }">Cancel</v-btn>
            <v-btn type="submit" color="primary" :loading="saving">
              {{ isEdit ? 'Save Changes' : 'Create Section' }}
            </v-btn>
          </div>
        </v-form>
      </v-card-text>
    </v-card>
  </v-container>
</template>

<script lang="ts" setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { api, type Rubric, type Section } from '@/api'

const router = useRouter()
const route  = useRoute()

const isEdit  = computed(() => !!route.params.id)
const formRef = ref()
const saving  = ref(false)
const error   = ref('')
const rubrics = ref<Rubric[]>([])

const form = ref({ name: '', startDate: '', endDate: '', rubricId: null as number | null })

const rules = {
  required: (v: any) => (v !== null && v !== '' && v !== undefined) || 'Required',
}

async function load() {
  rubrics.value = await api.get<Rubric[]>('/rubrics')
  if (isEdit.value) {
    const s = await api.get<Section>(`/sections/${route.params.id}`)
    form.value = {
      name:      s.name,
      startDate: s.startDate,
      endDate:   s.endDate,
      rubricId:  s.rubric?.id ?? null,
    }
  }
}

async function handleSubmit() {
  const { valid } = await formRef.value.validate()
  if (!valid) return

  saving.value = true
  error.value  = ''
  try {
    if (isEdit.value) {
      await api.put(`/sections/${route.params.id}`, form.value)
    } else {
      await api.post('/sections', form.value)
    }
    router.push({ name: 'sections' })
  } catch (e: any) {
    error.value = e.message
  } finally {
    saving.value = false
  }
}

onMounted(load)
</script>
