<template>
  <v-container>
    <v-btn
      class="mb-4"
      prepend-icon="mdi-arrow-left"
      :to="{ name: 'instructors' }"
      variant="text"
    >
      Back to Instructors
    </v-btn>

    <v-progress-circular v-if="loading" class="d-block mx-auto mt-8" indeterminate />

    <template v-if="instructor">
      <!-- Profile card -->
      <v-card class="mb-6" rounded="lg">
        <v-card-text>
          <v-row align="center">
            <v-col cols="auto">
              <v-avatar color="secondary" size="64">
                <span class="text-h6 text-white">
                  {{ instructor.firstName[0] }}{{ instructor.lastName[0] }}
                </span>
              </v-avatar>
            </v-col>
            <v-col>
              <h2 class="text-h6 font-weight-bold">
                {{ instructor.firstName }}
                {{ instructor.middleInitial ? instructor.middleInitial + '. ' : '' }}
                {{ instructor.lastName }}
              </h2>
              <div class="text-body-2 text-medium-emphasis">{{ instructor.email }}</div>
              <div class="mt-2 d-flex ga-2 flex-wrap">
                <v-chip
                  :color="instructor.active ? 'success' : 'error'"
                  size="small"
                  variant="tonal"
                >
                  {{ instructor.active ? 'Active' : 'Inactive' }}
                </v-chip>
                <v-chip
                  :color="instructor.registrationComplete ? 'success' : 'warning'"
                  size="small"
                  variant="tonal"
                >
                  {{ instructor.registrationComplete ? 'Registered' : 'Pending' }}
                </v-chip>
              </div>
            </v-col>

            <!-- Admin actions -->
            <v-col v-if="auth.role === 'ADMIN'" cols="auto">
              <v-btn
                v-if="instructor.active"
                class="mr-2"
                color="warning"
                :loading="actioning"
                variant="tonal"
                @click="deactivate"
              >
                Deactivate
              </v-btn>
              <v-btn
                v-else
                color="success"
                :loading="actioning"
                variant="tonal"
                @click="reactivate"
              >
                Reactivate
              </v-btn>
            </v-col>
          </v-row>
        </v-card-text>
      </v-card>

      <v-alert v-if="actionError" class="mb-4" type="error" variant="tonal">
        {{ actionError }}
      </v-alert>

      <!-- Teams -->
      <h3 class="text-subtitle-1 font-weight-bold mb-3">Assigned Teams</h3>
      <v-alert v-if="teams.length === 0" type="info" variant="tonal">
        This instructor is not assigned to any teams.
      </v-alert>
      <v-row v-else>
        <v-col
          v-for="team in teams"
          :key="team.id"
          cols="12"
          md="4"
        >
          <v-card rounded="lg" variant="outlined">
            <v-card-title class="pa-4 pb-1 text-body-1 font-weight-medium">
              {{ team.name }}
            </v-card-title>
            <v-card-subtitle class="px-4 pb-2">{{ team.sectionName }}</v-card-subtitle>
            <v-card-text class="px-4 pt-0 text-caption text-medium-emphasis">
              {{ team.students.length }} student(s)
            </v-card-text>
            <v-card-actions class="px-4 pb-3">
              <v-btn
                size="small"
                :to="{ name: 'reports-war', query: { teamId: team.id } }"
                variant="text"
              >
                WAR Report
              </v-btn>
            </v-card-actions>
          </v-card>
        </v-col>
      </v-row>
    </template>
  </v-container>
</template>

<script lang="ts" setup>
  import { onMounted, ref } from 'vue'
  import { useRoute } from 'vue-router'
  import { api, type Team, type User } from '@/api'
  import { useAuthStore } from '@/stores/auth'

  const route = useRoute()
  const auth = useAuthStore()
  const instructorId = Number(route.params.id)

  const instructor = ref<User | null>(null)
  const teams = ref<Team[]>([])
  const loading = ref(false)
  const actioning = ref(false)
  const actionError = ref('')

  onMounted(async () => {
    loading.value = true
    try {
      const [user, allTeams] = await Promise.all([
        api.get<User>(`/instructors/${instructorId}`),
        api.get<Team[]>('/teams'),
      ])
      instructor.value = user
      teams.value = allTeams.filter(t =>
        t.instructors.some(i => i.id === instructorId),
      )
    } catch (error: any) {
      actionError.value = error.message
    } finally {
      loading.value = false
    }
  })

  async function deactivate () {
    actioning.value = true
    actionError.value = ''
    try {
      instructor.value = await api.patch<User>(`/instructors/${instructorId}/deactivate`)
    } catch (error: any) {
      actionError.value = error.message
    } finally {
      actioning.value = false
    }
  }

  async function reactivate () {
    actioning.value = true
    actionError.value = ''
    try {
      instructor.value = await api.patch<User>(`/instructors/${instructorId}/reactivate`)
    } catch (error: any) {
      actionError.value = error.message
    } finally {
      actioning.value = false
    }
  }
</script>
