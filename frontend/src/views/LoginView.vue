<template>
  <v-container class="fill-height" fluid>
    <v-row align="center" justify="center">
      <v-col cols="12" lg="4" md="5" sm="8">

        <!-- Logo / heading -->
        <div class="text-center mb-6">
          <v-icon color="primary" size="56">mdi-pulse</v-icon>
          <h1 class="text-h4 font-weight-bold mt-2">Project Pulse</h1>
          <p class="text-medium-emphasis text-body-2 mt-1">
            Peer Evaluation &amp; Activity Tracking
          </p>
        </div>

        <v-card elevation="4" rounded="lg">
          <v-card-title class="text-h6 pa-6 pb-2">Sign in</v-card-title>

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

            <v-form ref="formRef" @submit.prevent="handleLogin">
              <v-text-field
                v-model="email"
                autofocus
                class="mb-3"
                density="comfortable"
                label="Email address"
                prepend-inner-icon="mdi-email-outline"
                :rules="[rules.required, rules.email]"
                type="email"
                variant="outlined"
              />
              <v-text-field
                v-model="password"
                :append-inner-icon="showPassword ? 'mdi-eye-off' : 'mdi-eye'"
                density="comfortable"
                label="Password"
                prepend-inner-icon="mdi-lock-outline"
                :rules="[rules.required]"
                :type="showPassword ? 'text' : 'password'"
                variant="outlined"
                @click:append-inner="showPassword = !showPassword"
              />

              <v-btn
                block
                class="mt-4"
                color="primary"
                :loading="loading"
                size="large"
                type="submit"
              >
                Sign in
              </v-btn>
            </v-form>
          </v-card-text>
        </v-card>

        <p class="text-center text-caption text-medium-emphasis mt-4">
          New here? Check your email for a registration link.
        </p>

      </v-col>
    </v-row>
  </v-container>
</template>

<script lang="ts" setup>
  import { ref } from 'vue'
  import { useRouter } from 'vue-router'
  import { useAuthStore } from '@/stores/auth'

  const auth = useAuthStore()
  const router = useRouter()
  const formRef = ref()

  const email = ref('')
  const password = ref('')
  const showPassword = ref(false)
  const loading = ref(false)
  const errorMsg = ref('')

  const rules = {
    required: (v: string) => !!v || 'Required',
    email: (v: string) => /.+@.+\..+/.test(v) || 'Enter a valid email',
  }

  async function handleLogin () {
    const { valid } = await formRef.value.validate()
    if (!valid) return

    loading.value = true
    errorMsg.value = ''

    try {
      await auth.login(email.value, password.value)
      router.push({ name: 'dashboard' })
    } catch (error: any) {
      errorMsg.value = error.message ?? 'Login failed. Please try again.'
    } finally {
      loading.value = false
    }
  }
</script>
