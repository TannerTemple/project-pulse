<template>
  <v-container class="pa-6" style="max-width: 600px">
    <h1 class="text-h5 font-weight-bold mb-6">Account Settings</h1>

    <v-alert v-if="error"   type="error"   variant="tonal" class="mb-4" closable @click:close="error = ''">{{ error }}</v-alert>
    <v-alert v-if="success" type="success" variant="tonal" class="mb-4" closable @click:close="success = ''">{{ success }}</v-alert>

    <v-card rounded="lg">
      <v-card-text class="pa-6">
        <v-form ref="formRef" @submit.prevent="handleSubmit">
          <v-row>
            <v-col cols="12" sm="6">
              <v-text-field
                v-model="form.firstName"
                label="First name"
                variant="outlined"
                density="comfortable"
                :rules="[rules.required]"
              />
            </v-col>
            <v-col cols="12" sm="6">
              <v-text-field
                v-model="form.lastName"
                label="Last name"
                variant="outlined"
                density="comfortable"
                :rules="[rules.required]"
              />
            </v-col>
          </v-row>

          <v-text-field
            v-if="auth.role === 'INSTRUCTOR'"
            v-model="form.middleInitial"
            label="Middle initial (optional)"
            variant="outlined"
            density="comfortable"
            class="mb-3"
            maxlength="1"
          />

          <v-divider class="my-4" />
          <p class="text-subtitle-2 mb-3">Change Password (leave blank to keep current)</p>

          <v-text-field
            v-model="form.newPassword"
            label="New password"
            :type="showPw ? 'text' : 'password'"
            :append-inner-icon="showPw ? 'mdi-eye-off' : 'mdi-eye'"
            @click:append-inner="showPw = !showPw"
            variant="outlined"
            density="comfortable"
            :rules="form.newPassword ? [rules.minLength] : []"
            class="mb-3"
          />
          <v-text-field
            v-model="form.confirmPassword"
            label="Confirm new password"
            :type="showPw ? 'text' : 'password'"
            variant="outlined"
            density="comfortable"
            :rules="form.newPassword ? [rules.match] : []"
          />

          <div class="d-flex justify-end mt-4">
            <v-btn type="submit" color="primary" :loading="saving">Save Changes</v-btn>
          </div>
        </v-form>
      </v-card-text>
    </v-card>
  </v-container>
</template>

<script lang="ts" setup>
import { ref, onMounted } from 'vue'
import { api, type User } from '@/api'
import { useAuthStore } from '@/stores/auth'

const auth    = useAuthStore()
const formRef = ref()
const saving  = ref(false)
const error   = ref('')
const success = ref('')
const showPw  = ref(false)

const form = ref({
  firstName: '',
  lastName: '',
  middleInitial: '',
  newPassword: '',
  confirmPassword: '',
})

const rules = {
  required:  (v: string) => !!v || 'Required',
  minLength: (v: string) => (v.length >= 8) || 'Minimum 8 characters',
  match:     (v: string) => v === form.value.newPassword || 'Passwords do not match',
}

async function load() {
  const me = await api.get<User>('/users/me')
  form.value.firstName     = me.firstName
  form.value.lastName      = me.lastName
  form.value.middleInitial = me.middleInitial ?? ''
}

async function handleSubmit() {
  const { valid } = await formRef.value.validate()
  if (!valid) return

  saving.value = true
  error.value  = ''
  success.value = ''
  try {
    const payload: any = {
      firstName:      form.value.firstName,
      lastName:       form.value.lastName,
      middleInitial:  form.value.middleInitial || undefined,
    }
    if (form.value.newPassword) {
      payload.newPassword     = form.value.newPassword
      payload.confirmPassword = form.value.confirmPassword
    }
    await api.patch('/users/me', payload)
    success.value = 'Account updated successfully.'
    form.value.newPassword     = ''
    form.value.confirmPassword = ''
  } catch (e: any) {
    error.value = e.message
  } finally {
    saving.value = false
  }
}

onMounted(load)
</script>
