<template>
  <v-container class="pa-6" style="max-width: 800px">
    <div class="d-flex align-center mb-6 ga-3">
      <v-btn icon="mdi-arrow-left" variant="text" :to="{ name: 'rubrics' }" />
      <h1 class="text-h5 font-weight-bold">New Rubric</h1>
    </div>

    <v-alert v-if="error" type="error" variant="tonal" class="mb-4">{{ error }}</v-alert>

    <v-card rounded="lg">
      <v-card-text class="pa-6">
        <v-form ref="formRef" @submit.prevent="handleSubmit">
          <v-text-field
            v-model="form.name"
            label="Rubric name"
            variant="outlined"
            density="comfortable"
            :rules="[rules.required]"
            class="mb-4"
            placeholder="e.g. Peer Eval Rubric v1"
          />

          <div class="d-flex align-center mb-3">
            <p class="text-subtitle-1 font-weight-medium flex-grow-1">Criteria</p>
            <v-btn variant="tonal" size="small" prepend-icon="mdi-plus" @click="addCriterion">
              Add Criterion
            </v-btn>
          </div>

          <v-card
            v-for="(criterion, i) in form.criteria"
            :key="i"
            variant="outlined"
            rounded="lg"
            class="mb-3 pa-3"
          >
            <div class="d-flex align-center ga-2 mb-2">
              <span class="text-caption text-medium-emphasis">Criterion {{ i + 1 }}</span>
              <v-spacer />
              <v-btn
                icon="mdi-delete"
                variant="text"
                size="x-small"
                color="error"
                @click="removeCriterion(i)"
              />
            </div>
            <v-row>
              <v-col cols="12" sm="7">
                <v-text-field
                  v-model="criterion.name"
                  label="Name"
                  variant="outlined"
                  density="comfortable"
                  :rules="[rules.required]"
                />
              </v-col>
              <v-col cols="12" sm="5">
                <v-text-field
                  v-model.number="criterion.maxScore"
                  label="Max score"
                  type="number"
                  variant="outlined"
                  density="comfortable"
                  min="1"
                  :rules="[rules.required, rules.positive]"
                />
              </v-col>
              <v-col cols="12">
                <v-text-field
                  v-model="criterion.description"
                  label="Description (optional)"
                  variant="outlined"
                  density="comfortable"
                />
              </v-col>
            </v-row>
          </v-card>

          <div class="d-flex justify-end ga-2 mt-4">
            <v-btn variant="text" :to="{ name: 'rubrics' }">Cancel</v-btn>
            <v-btn type="submit" color="primary" :loading="saving">Create Rubric</v-btn>
          </div>
        </v-form>
      </v-card-text>
    </v-card>
  </v-container>
</template>

<script lang="ts" setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { api } from '@/api'

const router  = useRouter()
const formRef = ref()
const saving  = ref(false)
const error   = ref('')

interface CriterionForm { name: string; description: string; maxScore: number; orderIndex: number }

const DEFAULT_CRITERIA: CriterionForm[] = [
  { name: 'Quality of Work',       description: '', maxScore: 10, orderIndex: 0 },
  { name: 'Productivity',          description: '', maxScore: 10, orderIndex: 1 },
  { name: 'Initiative',            description: '', maxScore: 10, orderIndex: 2 },
  { name: 'Courtesy',              description: '', maxScore: 10, orderIndex: 3 },
  { name: 'Open-mindedness',       description: '', maxScore: 10, orderIndex: 4 },
  { name: 'Engagement in Meetings',description: '', maxScore: 10, orderIndex: 5 },
]

const form = ref({
  name: '',
  criteria: DEFAULT_CRITERIA.map(c => ({ ...c })),
})

const rules = {
  required: (v: any) => (v !== null && v !== '' && v !== undefined) || 'Required',
  positive:  (v: number) => (v > 0) || 'Must be > 0',
}

function addCriterion() {
  form.value.criteria.push({ name: '', description: '', maxScore: 10, orderIndex: form.value.criteria.length })
}

function removeCriterion(i: number) {
  form.value.criteria.splice(i, 1)
  form.value.criteria.forEach((c, idx) => { c.orderIndex = idx })
}

async function handleSubmit() {
  const { valid } = await formRef.value.validate()
  if (!valid) return
  if (form.value.criteria.length === 0) {
    error.value = 'At least one criterion is required.'
    return
  }

  saving.value = true
  error.value  = ''
  try {
    await api.post('/rubrics', form.value)
    router.push({ name: 'rubrics' })
  } catch (e: any) {
    error.value = e.message
  } finally {
    saving.value = false
  }
}
</script>
