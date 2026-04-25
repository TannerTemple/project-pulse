<template>
  <v-container>
    <v-btn class="mb-4" prepend-icon="mdi-arrow-left" :to="{ name: 'sections' }" variant="text">
      Back to Sections
    </v-btn>

    <v-progress-circular v-if="loading" class="d-block mx-auto mt-8" color="primary" indeterminate />

    <v-alert v-else-if="error" class="mb-4" type="error" variant="tonal">{{ error }}</v-alert>

    <template v-else-if="section">
      <!-- Header -->
      <div class="d-flex align-center mb-6 ga-3">
        <h1 class="text-h5 font-weight-bold flex-grow-1">{{ section.name }}</h1>
        <v-btn
          prepend-icon="mdi-calendar-clock"
          :to="{ name: 'section-weeks', params: { id: section.id } }"
          variant="tonal"
        >
          Active Weeks
        </v-btn>
        <v-btn
          color="primary"
          prepend-icon="mdi-pencil"
          :to="{ name: 'section-edit', params: { id: section.id } }"
          variant="flat"
        >
          Edit
        </v-btn>
      </div>

      <!-- Section info -->
      <v-card class="mb-6" rounded="lg">
        <v-card-title class="pa-4 pb-2">Section Details</v-card-title>
        <v-card-text class="px-4 pt-0">
          <v-row>
            <v-col cols="12" md="4">
              <div class="text-caption text-medium-emphasis">Dates</div>
              <div>{{ section.startDate }} — {{ section.endDate }}</div>
            </v-col>
            <v-col cols="12" md="8">
              <div class="text-caption text-medium-emphasis">Rubric</div>
              <div v-if="section.rubric">{{ section.rubric.name }}</div>
              <v-chip v-else color="warning" size="small" variant="tonal">No rubric assigned</v-chip>
            </v-col>
          </v-row>
          <template v-if="section.rubric?.criteria?.length">
            <v-divider class="my-3" />
            <div class="text-caption text-medium-emphasis mb-2">Criteria</div>
            <v-chip
              v-for="c in section.rubric.criteria"
              :key="c.id"
              class="mr-2 mb-1"
              size="small"
              variant="outlined"
            >
              {{ c.name }} (max {{ c.maxScore }})
            </v-chip>
          </template>
        </v-card-text>
      </v-card>

      <!-- Teams -->
      <h2 class="text-h6 font-weight-bold mb-3">Teams ({{ teams.length }})</h2>
      <v-alert v-if="teams.length === 0" class="mb-6" type="info" variant="tonal">
        No teams in this section yet.
      </v-alert>
      <v-expansion-panels v-else class="mb-6" variant="accordion">
        <v-expansion-panel v-for="team in teams" :key="team.id">
          <v-expansion-panel-title>
            <span class="font-weight-medium">{{ team.name }}</span>
            <span class="ml-3 text-caption text-medium-emphasis">
              {{ team.students.length }} student{{ team.students.length !== 1 ? 's' : '' }},
              {{ team.instructors.length }} instructor{{ team.instructors.length !== 1 ? 's' : '' }}
            </span>
          </v-expansion-panel-title>
          <v-expansion-panel-text>
            <v-row>
              <v-col cols="12" md="6">
                <div class="text-caption text-medium-emphasis mb-2">Students</div>
                <div v-if="team.students.length === 0" class="text-body-2 text-medium-emphasis">None assigned</div>
                <v-chip
                  v-for="s in team.students"
                  :key="s.id"
                  class="mr-2 mb-1"
                  size="small"
                  :to="{ name: 'student-detail', params: { id: s.id } }"
                  variant="tonal"
                >
                  {{ s.firstName }} {{ s.lastName }}
                </v-chip>
              </v-col>
              <v-col cols="12" md="6">
                <div class="text-caption text-medium-emphasis mb-2">Instructors</div>
                <div v-if="team.instructors.length === 0" class="text-body-2 text-medium-emphasis">None assigned</div>
                <v-chip
                  v-for="i in team.instructors"
                  :key="i.id"
                  class="mr-2 mb-1"
                  color="primary"
                  size="small"
                  :to="{ name: 'instructor-detail', params: { id: i.id } }"
                  variant="tonal"
                >
                  {{ i.firstName }} {{ i.lastName }}
                </v-chip>
              </v-col>
            </v-row>
            <v-row v-if="team.description || team.websiteUrl" class="mt-1">
              <v-col v-if="team.description" cols="12" md="6">
                <div class="text-caption text-medium-emphasis">Description</div>
                <div class="text-body-2">{{ team.description }}</div>
              </v-col>
              <v-col v-if="team.websiteUrl" cols="12" md="6">
                <div class="text-caption text-medium-emphasis">Website</div>
                <a :href="team.websiteUrl" rel="noopener" target="_blank">{{ team.websiteUrl }}</a>
              </v-col>
            </v-row>
          </v-expansion-panel-text>
        </v-expansion-panel>
      </v-expansion-panels>

      <!-- Unassigned students -->
      <h2 class="text-h6 font-weight-bold mb-3">
        Unassigned Students ({{ unassignedStudents.length }})
      </h2>
      <v-card class="mb-6" rounded="lg">
        <v-card-text>
          <div v-if="unassignedStudents.length === 0" class="text-body-2 text-medium-emphasis">
            All students are assigned to a team.
          </div>
          <v-chip
            v-for="s in unassignedStudents"
            :key="s.id"
            class="mr-2 mb-1"
            size="small"
            :to="{ name: 'student-detail', params: { id: s.id } }"
            variant="tonal"
          >
            {{ s.firstName }} {{ s.lastName }}
          </v-chip>
        </v-card-text>
      </v-card>

      <!-- Unassigned instructors -->
      <h2 class="text-h6 font-weight-bold mb-3">
        Unassigned Instructors ({{ unassignedInstructors.length }})
      </h2>
      <v-card rounded="lg">
        <v-card-text>
          <div v-if="unassignedInstructors.length === 0" class="text-body-2 text-medium-emphasis">
            All active instructors are assigned to at least one team in this section.
          </div>
          <v-chip
            v-for="i in unassignedInstructors"
            :key="i.id"
            class="mr-2 mb-1"
            color="primary"
            size="small"
            :to="{ name: 'instructor-detail', params: { id: i.id } }"
            variant="tonal"
          >
            {{ i.firstName }} {{ i.lastName }}
          </v-chip>
        </v-card-text>
      </v-card>
    </template>
  </v-container>
</template>

<script lang="ts" setup>
  import { computed, onMounted, ref } from 'vue'
  import { useRoute } from 'vue-router'
  import { api, type Section, type Team, type User } from '@/api'

  const route = useRoute()
  const sectionId = Number(route.params.id)

  const section = ref<Section | null>(null)
  const teams = ref<Team[]>([])
  const allStudents = ref<User[]>([])
  const allInstructors = ref<User[]>([])
  const loading = ref(true)
  const error = ref('')

  const unassignedStudents = computed(() =>
    allStudents.value.filter(s => !s.teamId)
  )

  const unassignedInstructors = computed(() => {
    const assignedIds = new Set(teams.value.flatMap(t => t.instructors.map(i => i.id)))
    return allInstructors.value.filter(i => !assignedIds.has(i.id))
  })

  onMounted(async () => {
    try {
      const [sec, tms, students, instructors] = await Promise.all([
        api.get<Section>(`/sections/${sectionId}`),
        api.get<Team[]>(`/teams?sectionId=${sectionId}`),
        api.get<User[]>(`/students?sectionId=${sectionId}`),
        api.get<User[]>('/instructors?active=true'),
      ])
      section.value = sec
      teams.value = tms
      allStudents.value = students
      allInstructors.value = instructors
    } catch (e: any) {
      error.value = e.message ?? 'Failed to load section.'
    } finally {
      loading.value = false
    }
  })
</script>