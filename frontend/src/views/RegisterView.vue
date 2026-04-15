<template>
  <v-container class="fill-height" fluid>
    <v-row align="center" justify="center">
      <v-col cols="12" sm="8" md="5" lg="4">

        <div class="text-center mb-6">
          <v-icon color="primary" size="56">mdi-pulse</v-icon>
          <h1 class="text-h4 font-weight-bold mt-2">Project Pulse</h1>
          <p class="text-medium-emphasis text-body-2 mt-1">Complete your registration</p>
        </div>

        <v-card rounded="lg" elevation="4">
          <v-card-title class="text-h6 pa-6 pb-2">Create your account</v-card-title>

          <v-card-text class="pa-6 pt-4">
            <v-alert v-if="errorMsg" type="error" variant="tonal" class="mb-4" closable @click:close="errorMsg = ''">
              {{ errorMsg }}
            </v-alert>
            <v-alert v-if="!token" type="warning" variant="tonal" class="mb-4">
              No invitation token found. Please use the link from your invitation email.
            </v-alert>

            <v-form ref="formRef" @submit.prevent="handleRegister">
              <v-row dense>
                <v-col cols="12" sm="6">
                  <v-text-field v-model="firstName" label="First name" variant="outlined"
                    density="comfortable" :rules="[rules.required]" />
                </v-col>
                <v-col cols="12" sm="6">
                  <v-text-field v-model="lastName" label="Last name" variant="outlined"
                    density="comfortable" :rules="[rules.required]" />
                </v-col>
              </v-row>

              <v-text-field v-model="password" label="Password"
                :type="showPw ? 'text' : 'password'"
                prepend-inner-icon="mdi-lock-outline"
                :append-inner-icon="showPw ? 'mdi-eye-off' : 'mdi-eye'"
                @click:append-inner="showPw = !showPw"
                variant="outlined" density="comfortable"
                :rules="[rules.required, rules.minLen]" class="mb-3" />

              <v-text-field v-model="confirmPassword" label="Confirm password"
                type="password" prepend-inner-icon="mdi-lock-check-outline"
                variant="outlined" density="comfortable"
                :rules="[rules.required, rules.match]" />

              <v-btn type="submit" color="primary" size="large" block
                :loading="loading" :disabled="!token" class="mt-4">
                Create account
              </v-btn>
            </v-form>
          </v-card-text>
        </v-card>

      </v-col>
    </v-row>
  </v-container>
</template>

<script lang="ts" setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const auth     = useAuthStore()
const route    = useRoute()
const router   = useRouter()
const formRef  = ref()

const token           = ref('')
const firstName       = ref('')
const lastName        = ref('')
const password        = ref('')
const confirmPassword = ref('')
const showPw          = ref(false)
const loading         = ref(false)
const errorMsg        = ref('')

onMounted(() => {
  token.value = (route.query.token as string) ?? ''
})

const rules = {
  required: (v: string) => !!v || 'Required',
  minLen:   (v: string) => v.length >= 8 || 'Minimum 8 characters',
  match:    (v: string) => v === password.value || 'Passwords do not match',
}

async function handleRegister() {
  const { valid } = await formRef.value.validate()
  if (!valid) return

  loading.value  = true
  errorMsg.value = ''

  try {
    await auth.register({
      token: token.value,
      firstName: firstName.value,
      lastName: lastName.value,
      password: password.value,
      confirmPassword: confirmPassword.value,
    })
    router.push({ name: 'dashboard' })
  } catch (err: any) {
    errorMsg.value = err.message ?? 'Registration failed. Please try again.'
  } finally {
    loading.value = false
  }
}
</script>
