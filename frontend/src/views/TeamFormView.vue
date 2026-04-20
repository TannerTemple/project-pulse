<template>
  <v-container class="pa-6" style="max-width: 700px">
    <div class="d-flex align-center mb-6 ga-3">
      <v-btn icon="mdi-arrow-left" variant="text" :to="{ name: 'teams' }" />
      <h1 class="text-h5 font-weight-bold">{{ isEdit ? 'Edit Team' : 'New Team' }}</h1>
    </div>

    <v-alert v-if="error" type="error" variant="tonal" class="mb-4">{{ error }}</v-alert>

    <v-card rounded="lg">
      <v-card-text class="pa-6">
        <v-form ref="formRef" @submit.prevent="handleSubmit">
          <v-text-field
            v-model="form.name"
            label="Team name"
            variant="outlined"
            density="comfortable"
            :rules="[rules.required]"
            class="mb-3"
          />

          <v-textarea
            v-model="form.description"
            label="Description (optional)"
            variant="outlined"
            density="comfortable"
            rows="3"
            class="mb-3"
          />

          <v-text-field
            v-model="form.websiteUrl"
            label="Website URL (optional)"
            variant="outlined"
            density="comfortable"
            class="mb-3"
          />

          <v-select
            v-model="form.sectionId"
            label="Section"
            :items="sections"
            item-title="name"
            item-value="id"
            variant="outlined"
            density="comfortable"
            :rules="[rules.required]"
            class="mb-3"
          />

          <!-- Student assignment -->
          <v-divider class="my-4" />
          <p class="text-subtitle-2 mb-3">Assign Students</p>
          <v-autocomplete
            v-model="form.studentIds"
            label="Students"
            :items="eligibleStudents"
            :item-title="(u: any) => `${u.firstName} ${u.lastName} (${u.email})`"
            item-value="id"
            variant="outlined"
            density="comfortable"
            multiple
            chips
            closable-chips
            class="mb-3"
          />

          <!-- Instructor assignment -->
          <p class="text-subtitle-2 mb-3">Assign Instructors</p>
          <v-autocomplete
            v-model="form.instructorIds"
            label="Instructors"
            :items="instructors"
            :item-title="(u: any) => `${u.firstName} ${u.lastName}`"
            item-value="id"
            variant="outlined"
            density="comfortable"
            multiple
            chips
            closable-chips
            class="mb-3"
          />

          <div class="d-flex justify-end ga-2 mt-4">
            <v-btn variant="text" :to="{ name: 'teams' }">Cancel</v-btn>
            <v-btn type="submit" color="primary" :loading="saving">
              {{ isEdit ? 'Save Changes' : 'Create Team' }}
            </v-btn>
          </div>
        </v-form>
      </v-card-text>
    </v-card>
  </v-container>
</template>

<script lang="ts" setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { api } from '@/api'

const router = useRouter()
const route  = useRoute()
const isEdit = computed(() => !!route.params.id)

const formRef = ref()
const saving  = ref(false)
const error   = ref('')

interface Section { id: number; name: string }
interface User    { id: number; firstName: string; lastName: string; email: string; sectionId?: number }
interface Team    { id: number; name: string; sectionId: number; students: any[]; instructors: any[] }

const sections         = ref<Section[]>([])
const allStudents      = ref<User[]>([])
const instructors      = ref<User[]>([])

const form = ref({
  name: '', description: '', websiteUrl: '',
  sectionId: null as number | null,
  studentIds: [] as number[],
  instructorIds: [] as number[],
})

// Only show students belonging to the selected section
const eligibleStudents = computed(() =>
  form.value.sectionId
    ? allStudents.value.filter(s => s.sectionId === form.value.sectionId)
    : allStudents.value
)

const rules = { required: (v: any) => (v !== null && v !== '' && v !== undefined) || 'Required' }

// Clear student selection when section changes
watch(() => form.value.sectionId, () => { form.value.studentIds = [] })

async function load() {
  sections.value    = await api.get<Section[]>('/sections').catch(() => [])
  allStudents.value = await api.get<User[]>('/students').catch(() => [])
  instructors.value = await api.get<User[]>('/instructors').catch(() => [])

  if (isEdit.value) {
    const t = await api.get<Team>(`/teams/${route.params.id}`)
    form.value = {
      name:          t.name,
      description:   (t as any).description ?? '',
      websiteUrl:    (t as any).websiteUrl ?? '',
      sectionId:     t.sectionId,
      studentIds:    t.students.map((s: any) => s.id),
      instructorIds: t.instructors.map((i: any) => i.id),
    }
  }
}

async function handleSubmit() {
  const { valid } = await formRef.value.validate()
  if (!valid) return

  saving.value = true
  error.value  = ''
  try {
    const payload = {
      name: form.value.name,
      description: form.value.description,
      websiteUrl: form.value.websiteUrl,
      sectionId: form.value.sectionId,
    }
    let teamId: number
    if (isEdit.value) {
      const t = await api.put<any>(`/teams/${route.params.id}`, payload)
      teamId = t.id
    } else {
      const t = await api.post<any>('/teams', payload)
      teamId = t.id
    }

    // Assign students (only if any selected)
    if (form.value.studentIds.length > 0) {
      await api.post(`/teams/${teamId}/students`, { userIds: form.value.studentIds })
    }
    // Assign instructors (only if any selected)
    if (form.value.instructorIds.length > 0) {
      await api.post(`/teams/${teamId}/instructors`, { userIds: form.value.instructorIds })
    }

    router.push({ name: 'teams' })
  } catch (e: any) {
    error.value = e.message
  } finally {
    saving.value = false
  }
}

onMounted(load)
</script>
