<template>
  <v-container class="pa-6" style="max-width: 700px">
    <div class="d-flex align-center mb-6 ga-3">
      <v-btn icon="mdi-arrow-left" :to="{ name: 'teams' }" variant="text" />
      <h1 class="text-h5 font-weight-bold">{{ isEdit ? 'Edit Team' : 'New Team' }}</h1>
    </div>

    <v-alert v-if="error" class="mb-4" type="error" variant="tonal">{{ error }}</v-alert>

    <v-card rounded="lg">
      <v-card-text class="pa-6">
        <v-form ref="formRef" @submit.prevent="handleSubmit">
          <v-text-field
            v-model="form.name"
            class="mb-3"
            density="comfortable"
            label="Team name"
            :rules="[rules.required]"
            variant="outlined"
          />

          <v-textarea
            v-model="form.description"
            class="mb-3"
            density="comfortable"
            label="Description (optional)"
            rows="3"
            variant="outlined"
          />

          <v-text-field
            v-model="form.websiteUrl"
            class="mb-3"
            density="comfortable"
            label="Website URL (optional)"
            variant="outlined"
          />

          <v-select
            v-model="form.sectionId"
            class="mb-3"
            density="comfortable"
            item-title="name"
            item-value="id"
            :items="sections"
            label="Section"
            :rules="[rules.required]"
            variant="outlined"
          />

          <!-- Student assignment -->
          <v-divider class="my-4" />
          <p class="text-subtitle-2 mb-3">Assign Students</p>
          <v-autocomplete
            v-model="form.studentIds"
            chips
            class="mb-3"
            closable-chips
            density="comfortable"
            :item-title="(u: any) => `${u.firstName} ${u.lastName} (${u.email})`"
            item-value="id"
            :items="eligibleStudents"
            label="Students"
            multiple
            variant="outlined"
          />

          <!-- Instructor assignment -->
          <p class="text-subtitle-2 mb-3">Assign Instructors</p>
          <v-autocomplete
            v-model="form.instructorIds"
            chips
            class="mb-3"
            closable-chips
            density="comfortable"
            :item-title="(u: any) => `${u.firstName} ${u.lastName}`"
            item-value="id"
            :items="instructors"
            label="Instructors"
            multiple
            variant="outlined"
          />

          <div class="d-flex justify-end ga-2 mt-4">
            <v-btn :to="{ name: 'teams' }" variant="text">Cancel</v-btn>
            <v-btn color="primary" :loading="saving" type="submit">
              {{ isEdit ? 'Save Changes' : 'Create Team' }}
            </v-btn>
          </div>
        </v-form>
      </v-card-text>
    </v-card>
  </v-container>
</template>

<script lang="ts" setup>
  import { computed, onMounted, ref, watch } from 'vue'
  import { useRoute, useRouter } from 'vue-router'
  import { api } from '@/api'

  const router = useRouter()
  const route = useRoute()
  const isEdit = computed(() => !!route.params.id)

  const formRef = ref()
  const saving = ref(false)
  const error = ref('')

  interface Section { id: number, name: string }
  interface User { id: number, firstName: string, lastName: string, email: string, sectionId?: number }
  interface Team { id: number, name: string, sectionId: number, students: any[], instructors: any[] }

  const sections = ref<Section[]>([])
  const allStudents = ref<User[]>([])
  const instructors = ref<User[]>([])

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
      : allStudents.value,
  )

  const rules = { required: (v: any) => (v !== null && v !== '' && v !== undefined) || 'Required' }

  // Clear student selection when section changes
  watch(() => form.value.sectionId, () => {
    form.value.studentIds = []
  })

  async function load () {
    sections.value = await api.get<Section[]>('/sections').catch(() => [])
    allStudents.value = await api.get<User[]>('/students').catch(() => [])
    instructors.value = await api.get<User[]>('/instructors').catch(() => [])

    if (isEdit.value) {
      const t = await api.get<Team>(`/teams/${route.params.id}`)
      form.value = {
        name: t.name,
        description: (t as any).description ?? '',
        websiteUrl: (t as any).websiteUrl ?? '',
        sectionId: t.sectionId,
        studentIds: t.students.map((s: any) => s.id),
        instructorIds: t.instructors.map((i: any) => i.id),
      }
    }
  }

  async function handleSubmit () {
    const { valid } = await formRef.value.validate()
    if (!valid) return

    saving.value = true
    error.value = ''
    try {
      const rawUrl = form.value.websiteUrl?.trim()
      const websiteUrl = rawUrl && !rawUrl.startsWith('http://') && !rawUrl.startsWith('https://')
        ? 'https://' + rawUrl
        : rawUrl || null
      const payload = {
        name: form.value.name,
        description: form.value.description,
        websiteUrl,
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
    } catch (error_: any) {
      error.value = error_.message
    } finally {
      saving.value = false
    }
  }

  onMounted(load)
</script>
