<template>
  <v-container>
    <v-btn class="mb-4" prepend-icon="mdi-arrow-left" :to="{ name: 'teams' }" variant="text">
      Back to Teams
    </v-btn>

    <v-progress-circular v-if="loading" class="d-block mx-auto mt-8" color="primary" indeterminate />

    <v-alert v-else-if="error" class="mb-4" type="error" variant="tonal">{{ error }}</v-alert>

    <template v-else-if="team">
      <!-- Header -->
      <div class="d-flex align-center mb-6 ga-3">
        <h1 class="text-h5 font-weight-bold flex-grow-1">{{ team.name }}</h1>
        <template v-if="auth.role === 'ADMIN'">
          <v-btn
            prepend-icon="mdi-pencil"
            :to="{ name: 'team-edit', params: { id: team.id } }"
            variant="tonal"
          >
            Edit
          </v-btn>
          <v-btn color="error" prepend-icon="mdi-delete" variant="tonal" @click="deleteDialog = true">
            Delete
          </v-btn>
        </template>
      </div>

      <!-- Team info card -->
      <v-card class="mb-6" rounded="lg">
        <v-card-title class="pa-4 pb-2">Team Details</v-card-title>
        <v-card-text class="px-4 pt-0">
          <v-row>
            <v-col cols="12" md="4">
              <div class="text-caption text-medium-emphasis">Section</div>
              <div>{{ team.sectionName }}</div>
            </v-col>
            <v-col v-if="team.websiteUrl" cols="12" md="4">
              <div class="text-caption text-medium-emphasis">Website</div>
              <a :href="team.websiteUrl" rel="noopener" target="_blank">{{ team.websiteUrl }}</a>
            </v-col>
          </v-row>
          <v-row v-if="team.description" class="mt-1">
            <v-col cols="12">
              <div class="text-caption text-medium-emphasis">Description</div>
              <div class="text-body-2">{{ team.description }}</div>
            </v-col>
          </v-row>
        </v-card-text>
      </v-card>

      <!-- Members -->
      <v-row>
        <!-- Students -->
        <v-col cols="12" md="6">
          <v-card height="100%" rounded="lg">
            <v-card-title class="pa-4 pb-2">
              Students ({{ team.students.length }})
            </v-card-title>
            <v-card-text class="px-4 pt-0">
              <div v-if="team.students.length === 0" class="text-body-2 text-medium-emphasis">
                No students assigned yet.
              </div>
              <v-list v-else density="compact" lines="two">
                <v-list-item
                  v-for="s in team.students"
                  :key="s.id"
                  :subtitle="s.email"
                  :title="`${s.firstName} ${s.lastName}`"
                  :to="{ name: 'student-detail', params: { id: s.id } }"
                >
                  <template #prepend>
                    <v-avatar color="primary" size="36">
                      <span class="text-caption text-white">{{ s.firstName[0] }}{{ s.lastName[0] }}</span>
                    </v-avatar>
                  </template>
                </v-list-item>
              </v-list>
            </v-card-text>
          </v-card>
        </v-col>

        <!-- Instructors -->
        <v-col cols="12" md="6">
          <v-card height="100%" rounded="lg">
            <v-card-title class="pa-4 pb-2">
              Instructors ({{ team.instructors.length }})
            </v-card-title>
            <v-card-text class="px-4 pt-0">
              <div v-if="team.instructors.length === 0" class="text-body-2 text-medium-emphasis">
                No instructors assigned yet.
              </div>
              <v-list v-else density="compact" lines="two">
                <v-list-item
                  v-for="i in team.instructors"
                  :key="i.id"
                  :subtitle="i.email"
                  :title="`${i.firstName} ${i.lastName}`"
                  :to="{ name: 'instructor-detail', params: { id: i.id } }"
                >
                  <template #prepend>
                    <v-avatar color="secondary" size="36">
                      <span class="text-caption text-white">{{ i.firstName[0] }}{{ i.lastName[0] }}</span>
                    </v-avatar>
                  </template>
                </v-list-item>
              </v-list>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>
    </template>

    <!-- Delete confirmation -->
    <v-dialog v-model="deleteDialog" max-width="400">
      <v-card rounded="lg">
        <v-card-title class="pa-4">Delete team?</v-card-title>
        <v-card-text class="px-4">
          Delete <strong>{{ team?.name }}</strong>? This cannot be undone. All associated WAR
          activities and peer evaluations will also be permanently deleted.
        </v-card-text>
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
  import { useRoute, useRouter } from 'vue-router'
  import { api, type Team } from '@/api'
  import { useAuthStore } from '@/stores/auth'

  const route = useRoute()
  const router = useRouter()
  const auth = useAuthStore()
  const teamId = Number(route.params.id)

  const team = ref<Team | null>(null)
  const loading = ref(true)
  const error = ref('')
  const deleteDialog = ref(false)
  const deleting = ref(false)

  onMounted(async () => {
    try {
      team.value = await api.get<Team>(`/teams/${teamId}`)
    } catch (error_: any) {
      error.value = error_.message ?? 'Failed to load team.'
    } finally {
      loading.value = false
    }
  })

  async function doDelete () {
    deleting.value = true
    try {
      await api.delete(`/teams/${teamId}`)
      router.push({ name: 'teams' })
    } catch (error_: any) {
      error.value = error_.message ?? 'Failed to delete team.'
      deleteDialog.value = false
    } finally {
      deleting.value = false
    }
  }
</script>