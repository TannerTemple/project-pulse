<template>
  <v-container class="pa-6" style="max-width: 600px">
    <h1 class="text-h5 font-weight-bold mb-6">Account Settings</h1>

    <v-alert
      v-if="error"
      class="mb-4"
      closable
      type="error"
      variant="tonal"
      @click:close="error = ''"
    >{{ error }}</v-alert>
    <v-alert
      v-if="success"
      class="mb-4"
      closable
      type="success"
      variant="tonal"
      @click:close="success = ''"
    >{{ success }}</v-alert>

    <v-card rounded="lg">
      <v-card-text class="pa-6">
        <v-form ref="formRef" @submit.prevent="handleSubmit">
          <v-row>
            <v-col cols="12" sm="6">
              <v-text-field
                v-model="form.firstName"
                density="comfortable"
                label="First name"
                :rules="[rules.required]"
                variant="outlined"
              />
            </v-col>
            <v-col cols="12" sm="6">
              <v-text-field
                v-model="form.lastName"
                density="comfortable"
                label="Last name"
                :rules="[rules.required]"
                variant="outlined"
              />
            </v-col>
          </v-row>

          <v-text-field
            v-if="auth.role === 'INSTRUCTOR'"
            v-model="form.middleInitial"
            class="mb-3"
            density="comfortable"
            label="Middle initial (optional)"
            maxlength="1"
            variant="outlined"
          />

          <v-divider class="my-4" />
          <p class="text-subtitle-2 mb-3">Change Password (leave blank to keep current)</p>

          <v-text-field
            v-model="form.newPassword"
            :append-inner-icon="showPw ? 'mdi-eye-off' : 'mdi-eye'"
            class="mb-3"
            density="comfortable"
            label="New password"
            :rules="form.newPassword ? [rules.minLength] : []"
            :type="showPw ? 'text' : 'password'"
            variant="outlined"
            @click:append-inner="showPw = !showPw"
          />
          <v-text-field
            v-model="form.confirmPassword"
            density="comfortable"
            label="Confirm new password"
            :rules="form.newPassword ? [rules.match] : []"
            :type="showPw ? 'text' : 'password'"
            variant="outlined"
          />

          <div class="d-flex justify-end mt-4">
            <v-btn color="primary" :loading="saving" type="submit">Save Changes</v-btn>
          </div>
        </v-form>
      </v-card-text>
    </v-card>
  </v-container>
</template>

<script lang="ts" setup>
  import { onMounted, ref } from 'vue'
  import { api, type User } from '@/api'
  import { useAuthStore } from '@/stores/auth'

  const auth = useAuthStore()
  const formRef = ref()
  const saving = ref(false)
  const error = ref('')
  const success = ref('')
  const showPw = ref(false)

  const form = ref({
    firstName: '',
    lastName: '',
    middleInitial: '',
    newPassword: '',
    confirmPassword: '',
  })

  const rules = {
    required: (v: string) => !!v || 'Required',
    minLength: (v: string) => (v.length >= 8) || 'Minimum 8 characters',
    match: (v: string) => v === form.value.newPassword || 'Passwords do not match',
  }

  async function load () {
    const me = await api.get<User>('/users/me')
    form.value.firstName = me.firstName
    form.value.lastName = me.lastName
    form.value.middleInitial = me.middleInitial ?? ''
  }

  async function handleSubmit () {
    const { valid } = await formRef.value.validate()
    if (!valid) return

    saving.value = true
    error.value = ''
    success.value = ''
    try {
      const payload: any = {
        firstName: form.value.firstName,
        lastName: form.value.lastName,
        middleInitial: form.value.middleInitial || undefined,
      }
      if (form.value.newPassword) {
        payload.newPassword = form.value.newPassword
        payload.confirmPassword = form.value.confirmPassword
      }
      await api.patch('/users/me', payload)
      success.value = 'Account updated successfully.'
      form.value.newPassword = ''
      form.value.confirmPassword = ''
    } catch (error_: any) {
      error.value = error_.message
    } finally {
      saving.value = false
    }
  }

  onMounted(load)
</script>
