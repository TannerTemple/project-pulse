<template>
  <v-container class="fill-height" fluid>
    <v-row align="center" justify="center">
      <v-col cols="12" lg="4" md="5" sm="8">

        <div class="text-center mb-6">
          <v-icon color="primary" size="56">mdi-pulse</v-icon>
          <h1 class="text-h4 font-weight-bold mt-2">Project Pulse</h1>
          <p class="text-medium-emphasis text-body-2 mt-1">Complete your registration</p>
        </div>

        <v-card elevation="4" rounded="lg">
          <v-card-title class="text-h6 pa-6 pb-2">Create your account</v-card-title>

          <v-card-text class="pa-6 pt-4">
            <v-alert
              v-if="errorMsg"
              class="mb-4"
              closable
              type="error"
              variant="tonal"
              @click:close="errorMsg = ''"
            >
              {{ errorMsg }}
            </v-alert>
            <v-alert v-if="!token" class="mb-4" type="warning" variant="tonal">
              No invitation token found. Please use the link from your invitation email.
            </v-alert>

            <v-form ref="formRef" @submit.prevent="handleRegister">
              <v-row dense>
                <v-col cols="12" sm="6">
                  <v-text-field
                    v-model="firstName"
                    density="comfortable"
                    label="First name"
                    :rules="[rules.required]"
                    variant="outlined"
                  />
                </v-col>
                <v-col cols="12" sm="6">
                  <v-text-field
                    v-model="lastName"
                    density="comfortable"
                    label="Last name"
                    :rules="[rules.required]"
                    variant="outlined"
                  />
                </v-col>
              </v-row>

              <v-text-field
                v-model="password"
                :append-inner-icon="showPw ? 'mdi-eye-off' : 'mdi-eye'"
                class="mb-3"
                density="comfortable"
                label="Password"
                prepend-inner-icon="mdi-lock-outline"
                :rules="[rules.required, rules.minLen]"
                :type="showPw ? 'text' : 'password'"
                variant="outlined"
                @click:append-inner="showPw = !showPw"
              />

              <v-text-field
                v-model="confirmPassword"
                density="comfortable"
                label="Confirm password"
                prepend-inner-icon="mdi-lock-check-outline"
                :rules="[rules.required, rules.match]"
                type="password"
                variant="outlined"
              />

              <v-btn
                block
                class="mt-4"
                color="primary"
                :disabled="!token"
                :loading="loading"
                size="large"
                type="submit"
              >
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
  import { onMounted, ref } from 'vue'
  import { useRoute, useRouter } from 'vue-router'
  import { useAuthStore } from '@/stores/auth'

  const auth = useAuthStore()
  const route = useRoute()
  const router = useRouter()
  const formRef = ref()

  const token = ref('')
  const firstName = ref('')
  const lastName = ref('')
  const password = ref('')
  const confirmPassword = ref('')
  const showPw = ref(false)
  const loading = ref(false)
  const errorMsg = ref('')

  onMounted(() => {
    token.value = (route.query.token as string) ?? ''
  })

  const rules = {
    required: (v: string) => !!v || 'Required',
    minLen: (v: string) => v.length >= 8 || 'Minimum 8 characters',
    match: (v: string) => v === password.value || 'Passwords do not match',
  }

  async function handleRegister () {
    const { valid } = await formRef.value.validate()
    if (!valid) return

    loading.value = true
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
    } catch (error: any) {
      errorMsg.value = error.message ?? 'Registration failed. Please try again.'
    } finally {
      loading.value = false
    }
  }
</script>
