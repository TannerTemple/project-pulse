<template>
  <v-container class="pa-6" fluid>
    <div class="d-flex align-center mb-6 ga-3">
      <h1 class="text-h5 font-weight-bold flex-grow-1">Students</h1>
      <v-btn
        v-if="auth.role === 'ADMIN'"
        color="primary"
        prepend-icon="mdi-email-plus"
        @click="inviteDialog = true"
      >
        Invite Students
      </v-btn>
    </div>

    <!-- Filters -->
    <v-row class="mb-2">
      <v-col cols="12" sm="4">
        <v-text-field
          v-model="filters.firstName"
          clearable
          density="comfortable"
          label="First name"
          variant="outlined"
          @update:model-value="load"
        />
      </v-col>
      <v-col cols="12" sm="4">
        <v-text-field
          v-model="filters.lastName"
          clearable
          density="comfortable"
          label="Last name"
          variant="outlined"
          @update:model-value="load"
        />
      </v-col>
      <v-col v-if="auth.role === 'ADMIN'" cols="12" sm="4">
        <v-select
          v-model="filters.sectionId"
          clearable
          density="comfortable"
          item-title="name"
          item-value="id"
          :items="sections"
          label="Section"
          variant="outlined"
          @update:model-value="load"
        />
      </v-col>
    </v-row>

    <v-alert v-if="error" class="mb-4" type="error" variant="tonal">{{ error }}</v-alert>

    <v-progress-circular v-if="loading" class="d-block mx-auto my-8" color="primary" indeterminate />

    <v-table v-else-if="students.length > 0" hover>
      <thead>
        <tr>
          <th>Name</th>
          <th>Email</th>
          <th>Section</th>
          <th>Team</th>
          <th>Status</th>
          <th />
        </tr>
      </thead>
      <tbody>
        <tr v-for="s in students" :key="s.id">
          <td>{{ s.firstName }} {{ s.lastName }}</td>
          <td>{{ s.email }}</td>
          <td>{{ s.sectionName ?? '—' }}</td>
          <td>{{ s.teamName ?? '—' }}</td>
          <td>
            <v-chip
              :color="s.registrationComplete ? 'success' : 'warning'"
              size="small"
              variant="tonal"
            >
              {{ s.registrationComplete ? 'Active' : 'Pending' }}
            </v-chip>
          </td>
          <td>
            <v-btn
              icon="mdi-eye"
              size="small"
              :to="{ name: 'student-detail', params: { id: s.id } }"
              variant="text"
            />
            <v-btn
              v-if="auth.role === 'ADMIN'"
              color="error"
              icon="mdi-delete"
              size="small"
              variant="text"
              @click="confirmDelete(s)"
            />
          </td>
        </tr>
      </tbody>
    </v-table>

    <v-alert v-else type="info" variant="tonal">No students found.</v-alert>

    <!-- Invite dialog -->
    <v-dialog v-model="inviteDialog" max-width="500">
      <v-card rounded="lg">
        <v-card-title class="pa-4">Invite Students</v-card-title>
        <v-card-text class="px-4">
          <v-alert v-if="inviteError" class="mb-3" type="error" variant="tonal">{{ inviteError }}</v-alert>
          <v-alert v-if="inviteSuccess" class="mb-3" type="success" variant="tonal">{{ inviteSuccess }}</v-alert>

          <v-select
            v-model="invite.sectionId"
            class="mb-3"
            density="comfortable"
            item-title="name"
            item-value="id"
            :items="sections"
            label="Section"
            :rules="[r => !!r || 'Required']"
            variant="outlined"
          />
          <v-textarea
            v-model="invite.emails"
            class="mb-3"
            density="comfortable"
            label="Email addresses (semicolon-separated)"
            placeholder="alice@tcu.edu; bob@tcu.edu"
            rows="3"
            variant="outlined"
          />
          <v-textarea
            v-model="invite.customMessage"
            density="comfortable"
            label="Custom message (optional)"
            rows="2"
            variant="outlined"
          />
        </v-card-text>
        <v-card-actions class="px-4 pb-4">
          <v-spacer />
          <v-btn variant="text" @click="inviteDialog = false">Cancel</v-btn>
          <v-btn color="primary" :loading="inviting" @click="sendInvites">Send Invitations</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Delete confirmation -->
    <v-dialog v-model="deleteDialog" max-width="400">
      <v-card rounded="lg">
        <v-card-title class="pa-4">Delete student?</v-card-title>
        <v-card-text class="px-4">Delete <strong>{{ studentToDelete?.firstName }} {{ studentToDelete?.lastName }}</strong>?</v-card-text>
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
  import { onMounted, ref } from 'vue'
  import { api, type User } from '@/api'
  import { useAuthStore } from '@/stores/auth'

  const auth = useAuthStore()

  const students = ref<User[]>([])
  const sections = ref<any[]>([])
  const loading = ref(false)
  const error = ref('')
  const filters = ref({ firstName: '', lastName: '', sectionId: null as number | null })

  const inviteDialog = ref(false)
  const inviting = ref(false)
  const inviteError = ref('')
  const inviteSuccess = ref('')
  const invite = ref({ sectionId: null as number | null, emails: '', customMessage: '' })

  const deleteDialog = ref(false)
  const deleting = ref(false)
  const studentToDelete = ref<User | null>(null)

  async function load () {
    loading.value = true
    error.value = ''
    try {
      const p = new URLSearchParams()
      if (filters.value.firstName) p.set('firstName', filters.value.firstName)
      if (filters.value.lastName) p.set('lastName', filters.value.lastName)
      if (filters.value.sectionId) p.set('sectionId', String(filters.value.sectionId))
      const q = p.toString() ? `?${p}` : ''
      students.value = await api.get<User[]>(`/students${q}`)
    } catch (error_: any) {
      error.value = error_.message
    } finally {
      loading.value = false
    }
  }

  async function sendInvites () {
    inviteError.value = ''
    inviteSuccess.value = ''
    if (!invite.value.sectionId || !invite.value.emails.trim()) {
      inviteError.value = 'Section and at least one email are required.'
      return
    }
    inviting.value = true
    try {
      const res = await api.post<any>(`/sections/${invite.value.sectionId}/invitations/students`, {
        emails: invite.value.emails,
        customMessage: invite.value.customMessage,
      })
      inviteSuccess.value = `${res.emailsSent} invitation(s) sent.`
      invite.value = { sectionId: null, emails: '', customMessage: '' }
    } catch (error_: any) {
      inviteError.value = error_.message
    } finally {
      inviting.value = false
    }
  }

  function confirmDelete (student: User) {
    studentToDelete.value = student
    deleteDialog.value = true
  }

  async function doDelete () {
    if (!studentToDelete.value) return
    deleting.value = true
    try {
      await api.delete(`/students/${studentToDelete.value.id}`)
      deleteDialog.value = false
      await load()
    } catch (error_: any) {
      error.value = error_.message
    } finally {
      deleting.value = false
    }
  }

  onMounted(async () => {
    sections.value = await api.get<any[]>('/sections').catch(() => [])
    await load()
  })
</script>
