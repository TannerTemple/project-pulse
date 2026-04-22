<template>
  <v-container class="pa-6" fluid>
    <div class="d-flex align-center mb-6 ga-3">
      <h1 class="text-h5 font-weight-bold flex-grow-1">Instructors</h1>
      <v-btn color="primary" prepend-icon="mdi-email-plus" @click="inviteDialog = true">
        Invite Instructors
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
      <v-col cols="12" sm="4">
        <v-select
          v-model="filters.active"
          density="comfortable"
          item-title="title"
          item-value="value"
          :items="[{ title: 'All', value: null }, { title: 'Active', value: true }, { title: 'Inactive', value: false }]"
          label="Status"
          variant="outlined"
          @update:model-value="load"
        />
      </v-col>
    </v-row>

    <v-alert v-if="error" class="mb-4" type="error" variant="tonal">{{ error }}</v-alert>

    <v-progress-circular v-if="loading" class="d-block mx-auto my-8" color="primary" indeterminate />

    <v-table v-else-if="instructors.length > 0" hover>
      <thead>
        <tr>
          <th>Name</th>
          <th>Email</th>
          <th>Status</th>
          <th>Registration</th>
          <th />
        </tr>
      </thead>
      <tbody>
        <tr v-for="inst in instructors" :key="inst.id">
          <td>{{ inst.firstName }} {{ inst.middleInitial ? inst.middleInitial + '. ' : '' }}{{ inst.lastName }}</td>
          <td>{{ inst.email }}</td>
          <td>
            <v-chip :color="inst.active ? 'success' : 'error'" size="small" variant="tonal">
              {{ inst.active ? 'Active' : 'Inactive' }}
            </v-chip>
          </td>
          <td>
            <v-chip :color="inst.registrationComplete ? 'success' : 'warning'" size="small" variant="tonal">
              {{ inst.registrationComplete ? 'Complete' : 'Pending' }}
            </v-chip>
          </td>
          <td>
            <v-btn
              icon="mdi-eye"
              size="small"
              :to="{ name: 'instructor-detail', params: { id: inst.id } }"
              variant="text"
            />
            <v-btn
              v-if="inst.active"
              color="warning"
              :loading="actionId === inst.id"
              size="small"
              variant="text"
              @click="deactivate(inst)"
            >
              Deactivate
            </v-btn>
            <v-btn
              v-else
              color="success"
              :loading="actionId === inst.id"
              size="small"
              variant="text"
              @click="reactivate(inst)"
            >
              Reactivate
            </v-btn>
          </td>
        </tr>
      </tbody>
    </v-table>

    <v-alert v-else type="info" variant="tonal">No instructors found.</v-alert>

    <!-- Invite dialog -->
    <v-dialog v-model="inviteDialog" max-width="500">
      <v-card rounded="lg">
        <v-card-title class="pa-4">Invite Instructors</v-card-title>
        <v-card-text class="px-4">
          <v-alert v-if="inviteError" class="mb-3" type="error" variant="tonal">{{ inviteError }}</v-alert>
          <v-alert v-if="inviteSuccess" class="mb-3" type="success" variant="tonal">{{ inviteSuccess }}</v-alert>
          <v-textarea
            v-model="invite.emails"
            class="mb-3"
            density="comfortable"
            label="Email addresses (semicolon-separated)"
            placeholder="prof@tcu.edu; prof2@tcu.edu"
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
  </v-container>
</template>

<script lang="ts" setup>
  import { onMounted, ref } from 'vue'
  import { api, type User } from '@/api'

  const instructors = ref<User[]>([])
  const loading = ref(false)
  const error = ref('')
  const actionId = ref<number | null>(null)
  const filters = ref({ firstName: '', lastName: '', active: null as boolean | null })

  const inviteDialog = ref(false)
  const inviting = ref(false)
  const inviteError = ref('')
  const inviteSuccess = ref('')
  const invite = ref({ emails: '', customMessage: '' })

  async function load () {
    loading.value = true
    error.value = ''
    try {
      const p = new URLSearchParams()
      if (filters.value.firstName) p.set('firstName', filters.value.firstName)
      if (filters.value.lastName) p.set('lastName', filters.value.lastName)
      if (filters.value.active !== null) p.set('active', String(filters.value.active))
      const q = p.toString() ? `?${p}` : ''
      instructors.value = await api.get<User[]>(`/instructors${q}`)
    } catch (error_: any) {
      error.value = error_.message
    } finally {
      loading.value = false
    }
  }

  async function deactivate (inst: User) {
    actionId.value = inst.id
    try {
      await api.patch(`/instructors/${inst.id}/deactivate`)
      await load()
    } catch (error_: any) {
      error.value = error_.message
    } finally {
      actionId.value = null
    }
  }

  async function reactivate (inst: User) {
    actionId.value = inst.id
    try {
      await api.patch(`/instructors/${inst.id}/reactivate`)
      await load()
    } catch (error_: any) {
      error.value = error_.message
    } finally {
      actionId.value = null
    }
  }

  async function sendInvites () {
    inviteError.value = ''
    inviteSuccess.value = ''
    if (!invite.value.emails.trim()) {
      inviteError.value = 'At least one email is required.'
      return
    }
    inviting.value = true
    try {
      const res = await api.post<any>('/invitations/instructors', invite.value)
      inviteSuccess.value = `${res.emailsSent} invitation(s) sent.`
      invite.value = { emails: '', customMessage: '' }
    } catch (error_: any) {
      inviteError.value = error_.message
    } finally {
      inviting.value = false
    }
  }

  onMounted(load)
</script>
