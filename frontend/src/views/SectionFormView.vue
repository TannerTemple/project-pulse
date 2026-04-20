<template>
  <v-container class="pa-6" style="max-width: 700px">
    <div class="d-flex align-center mb-6 ga-3">
      <v-btn icon="mdi-arrow-left" :to="{ name: 'sections' }" variant="text" />
      <h1 class="text-h5 font-weight-bold">{{ isEdit ? 'Edit Section' : 'New Section' }}</h1>
    </div>

    <v-alert v-if="error" class="mb-4" type="error" variant="tonal">{{ error }}</v-alert>

    <v-card rounded="lg">
      <v-card-text class="pa-6">
        <v-form ref="formRef" @submit.prevent="handleSubmit">
          <v-text-field
            v-model="form.name"
            class="mb-3"
            density="comfortable"
            label="Section name"
            placeholder="e.g. 2023-2024"
            :rules="[rules.required]"
            variant="outlined"
          />

          <v-row>
            <v-col cols="12" sm="6">
              <v-text-field
                v-model="form.startDate"
                density="comfortable"
                label="Start date"
                :rules="[rules.required]"
                type="date"
                variant="outlined"
              />
            </v-col>
            <v-col cols="12" sm="6">
              <v-text-field
                v-model="form.endDate"
                density="comfortable"
                label="End date"
                :rules="[rules.required]"
                type="date"
                variant="outlined"
              />
            </v-col>
          </v-row>

          <v-select
            v-model="form.rubricId"
            class="mb-3"
            density="comfortable"
            item-title="name"
            item-value="id"
            :items="rubrics"
            label="Rubric"
            :rules="[rules.required]"
            variant="outlined"
          />

          <div class="d-flex justify-end ga-2 mt-4">
            <v-btn :to="{ name: 'sections' }" variant="text">Cancel</v-btn>
            <v-btn color="primary" :loading="saving" type="submit">
              {{ isEdit ? 'Save Changes' : 'Create Section' }}
            </v-btn>
          </div>
        </v-form>
      </v-card-text>
    </v-card>
  </v-container>
</template>

<script lang="ts" setup>
  import { computed, onMounted, ref } from 'vue'
  import { useRoute, useRouter } from 'vue-router'
  import { api, type Rubric, type Section } from '@/api'

  const router = useRouter()
  const route = useRoute()

  const isEdit = computed(() => !!route.params.id)
  const formRef = ref()
  const saving = ref(false)
  const error = ref('')
  const rubrics = ref<Rubric[]>([])

  const form = ref({ name: '', startDate: '', endDate: '', rubricId: null as number | null })

  const rules = {
    required: (v: any) => (v !== null && v !== '' && v !== undefined) || 'Required',
  }

  async function load () {
    rubrics.value = await api.get<Rubric[]>('/rubrics')
    if (isEdit.value) {
      const s = await api.get<Section>(`/sections/${route.params.id}`)
      form.value = {
        name: s.name,
        startDate: s.startDate,
        endDate: s.endDate,
        rubricId: s.rubric?.id ?? null,
      }
    }
  }

  async function handleSubmit () {
    const { valid } = await formRef.value.validate()
    if (!valid) return

    saving.value = true
    error.value = ''
    try {
      await (isEdit.value ? api.put(`/sections/${route.params.id}`, form.value) : api.post('/sections', form.value))
      router.push({ name: 'sections' })
    } catch (error_: any) {
      error.value = error_.message
    } finally {
      saving.value = false
    }
  }

  onMounted(load)
</script>
