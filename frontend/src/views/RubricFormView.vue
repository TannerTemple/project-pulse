<template>
  <v-container class="pa-6" style="max-width: 800px">
    <div class="d-flex align-center mb-6 ga-3">
      <v-btn icon="mdi-arrow-left" :to="{ name: 'rubrics' }" variant="text" />
      <h1 class="text-h5 font-weight-bold">{{ isEdit ? 'Edit Rubric' : 'New Rubric' }}</h1>
    </div>

    <v-progress-circular v-if="loading" class="d-block mx-auto my-8" color="primary" indeterminate />

    <template v-else>
      <v-alert v-if="error" class="mb-4" type="error" variant="tonal">{{ error }}</v-alert>

      <v-card rounded="lg">
        <v-card-text class="pa-6">
          <v-form ref="formRef" @submit.prevent="handleSubmit">
            <v-text-field
              v-model="form.name"
              class="mb-4"
              density="comfortable"
              label="Rubric name"
              placeholder="e.g. Peer Eval Rubric v1"
              :rules="[rules.required]"
              variant="outlined"
            />

            <div class="d-flex align-center mb-3">
              <p class="text-subtitle-1 font-weight-medium flex-grow-1">Criteria</p>
              <v-btn prepend-icon="mdi-plus" size="small" variant="tonal" @click="addCriterion">
                Add Criterion
              </v-btn>
            </div>

            <v-card
              v-for="(criterion, i) in form.criteria"
              :key="i"
              class="mb-3 pa-3"
              rounded="lg"
              variant="outlined"
            >
              <div class="d-flex align-center ga-2 mb-2">
                <span class="text-caption text-medium-emphasis">Criterion {{ i + 1 }}</span>
                <v-spacer />
                <v-btn
                  color="error"
                  icon="mdi-delete"
                  size="x-small"
                  variant="text"
                  @click="removeCriterion(i)"
                />
              </div>
              <v-row>
                <v-col cols="12" sm="7">
                  <v-text-field
                    v-model="criterion.name"
                    density="comfortable"
                    label="Name"
                    :rules="[rules.required]"
                    variant="outlined"
                  />
                </v-col>
                <v-col cols="12" sm="5">
                  <v-text-field
                    v-model.number="criterion.maxScore"
                    density="comfortable"
                    label="Max score"
                    min="1"
                    :rules="[rules.required, rules.positive]"
                    type="number"
                    variant="outlined"
                  />
                </v-col>
                <v-col cols="12">
                  <v-text-field
                    v-model="criterion.description"
                    density="comfortable"
                    label="Description (optional)"
                    variant="outlined"
                  />
                </v-col>
              </v-row>
            </v-card>

            <div class="d-flex justify-end ga-2 mt-4">
              <v-btn :to="{ name: 'rubrics' }" variant="text">Cancel</v-btn>
              <v-btn color="primary" :loading="saving" type="submit">
                {{ isEdit ? 'Save Changes' : 'Create Rubric' }}
              </v-btn>
            </div>
          </v-form>
        </v-card-text>
      </v-card>
    </template>
  </v-container>
</template>

<script lang="ts" setup>
  import { computed, onMounted, ref } from 'vue'
  import { useRoute, useRouter } from 'vue-router'
  import { api } from '@/api'

  const route = useRoute()
  const router = useRouter()
  const formRef = ref()
  const saving = ref(false)
  const loading = ref(false)
  const error = ref('')

  const isEdit = computed(() => !!route.params.id)

  interface CriterionForm { name: string, description: string, maxScore: number, orderIndex: number }

  const DEFAULT_CRITERIA: CriterionForm[] = [
    { name: 'Quality of Work', description: '', maxScore: 10, orderIndex: 0 },
    { name: 'Productivity', description: '', maxScore: 10, orderIndex: 1 },
    { name: 'Initiative', description: '', maxScore: 10, orderIndex: 2 },
    { name: 'Courtesy', description: '', maxScore: 10, orderIndex: 3 },
    { name: 'Open-mindedness', description: '', maxScore: 10, orderIndex: 4 },
    { name: 'Engagement in Meetings', description: '', maxScore: 10, orderIndex: 5 },
  ]

  const form = ref({
    name: '',
    criteria: DEFAULT_CRITERIA.map(c => ({ ...c })),
  })

  const rules = {
    required: (v: any) => (v !== null && v !== '' && v !== undefined) || 'Required',
    positive: (v: number) => (v > 0) || 'Must be > 0',
  }

  function addCriterion () {
    form.value.criteria.push({ name: '', description: '', maxScore: 10, orderIndex: form.value.criteria.length })
  }

  function removeCriterion (i: number) {
    form.value.criteria.splice(i, 1)
    for (const [idx, c] of form.value.criteria.entries()) {
      c.orderIndex = idx
    }
  }

  async function handleSubmit () {
    const { valid } = await formRef.value.validate()
    if (!valid) return
    if (form.value.criteria.length === 0) {
      error.value = 'At least one criterion is required.'
      return
    }

    saving.value = true
    error.value = ''
    try {
      const payload = {
        name: form.value.name,
        criteria: form.value.criteria.map((c, i) => ({ ...c, orderIndex: i })),
      }
      await (isEdit.value ? api.put(`/rubrics/${route.params.id}`, payload) : api.post('/rubrics', payload))
      router.push({ name: 'rubrics' })
    } catch (error_: any) {
      error.value = error_.message
    } finally {
      saving.value = false
    }
  }

  onMounted(async () => {
    if (!isEdit.value) return
    loading.value = true
    try {
      const rubric = await api.get<any>(`/rubrics/${route.params.id}`)
      form.value = {
        name: rubric.name,
        criteria: rubric.criteria.map((c: any) => ({
          name: c.name,
          description: c.description ?? '',
          maxScore: c.maxScore,
          orderIndex: c.orderIndex,
        })),
      }
    } catch (error_: any) {
      error.value = error_.message
    } finally {
      loading.value = false
    }
  })
</script>
