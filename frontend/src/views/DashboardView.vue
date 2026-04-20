<template>
  <v-container class="pa-6" fluid>
    <v-row>
      <v-col cols="12">
        <h1 class="text-h5 font-weight-bold mb-1">
          Welcome back, {{ auth.user?.firstName }} 👋
        </h1>
        <p class="text-medium-emphasis text-body-2">
          {{ roleLabel }} · Project Pulse
        </p>
      </v-col>
    </v-row>

    <!-- Quick-action cards (Admin) -->
    <v-row v-if="auth.role === 'ADMIN'" class="mt-2">
      <v-col
        v-for="card in adminCards"
        :key="card.title"
        cols="12"
        lg="3"
        md="4"
        sm="6"
      >
        <v-card hover rounded="lg" :to="card.to">
          <v-card-text class="d-flex align-center ga-4 pa-5">
            <v-avatar :color="card.color" rounded="lg" size="48">
              <v-icon color="white" :icon="card.icon" />
            </v-avatar>
            <div>
              <div class="text-subtitle-1 font-weight-medium">{{ card.title }}</div>
              <div class="text-caption text-medium-emphasis">{{ card.subtitle }}</div>
            </div>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>

    <!-- Instructor view -->
    <v-row v-else-if="auth.role === 'INSTRUCTOR'" class="mt-2">
      <v-col
        v-for="card in instructorCards"
        :key="card.title"
        cols="12"
        md="4"
        sm="6"
      >
        <v-card hover rounded="lg" :to="card.to">
          <v-card-text class="d-flex align-center ga-4 pa-5">
            <v-avatar :color="card.color" rounded="lg" size="48">
              <v-icon color="white" :icon="card.icon" />
            </v-avatar>
            <div>
              <div class="text-subtitle-1 font-weight-medium">{{ card.title }}</div>
              <div class="text-caption text-medium-emphasis">{{ card.subtitle }}</div>
            </div>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>

    <!-- Student view -->
    <v-row v-else class="mt-2">
      <v-col
        v-for="card in studentCards"
        :key="card.title"
        cols="12"
        md="4"
        sm="6"
      >
        <v-card hover rounded="lg" :to="card.to">
          <v-card-text class="d-flex align-center ga-4 pa-5">
            <v-avatar :color="card.color" rounded="lg" size="48">
              <v-icon color="white" :icon="card.icon" />
            </v-avatar>
            <div>
              <div class="text-subtitle-1 font-weight-medium">{{ card.title }}</div>
              <div class="text-caption text-medium-emphasis">{{ card.subtitle }}</div>
            </div>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
</template>

<script lang="ts" setup>
  import { computed } from 'vue'
  import { useAuthStore } from '@/stores/auth'

  const auth = useAuthStore()

  const roleLabel = computed(() => {
    const map: Record<string, string> = {
      ADMIN: 'Administrator',
      INSTRUCTOR: 'Instructor',
      STUDENT: 'Student',
    }
    return map[auth.role ?? ''] ?? ''
  })

  const adminCards = [
    { title: 'Sections', subtitle: 'Manage senior design sections', icon: 'mdi-school', color: 'primary', to: '/sections' },
    { title: 'Teams', subtitle: 'View and manage teams', icon: 'mdi-account-group', color: 'secondary', to: '/teams' },
    { title: 'Students', subtitle: 'Invite and manage students', icon: 'mdi-account-multiple', color: 'success', to: '/students' },
    { title: 'Instructors', subtitle: 'Invite and manage instructors', icon: 'mdi-teach', color: 'info', to: '/instructors' },
    { title: 'Rubrics', subtitle: 'Create evaluation rubrics', icon: 'mdi-format-list-checks', color: 'warning', to: '/rubrics' },
  ]

  const instructorCards = [
    { title: 'My Teams', subtitle: 'View your assigned teams', icon: 'mdi-account-group', color: 'primary', to: '/teams' },
    { title: 'WAR Reports', subtitle: 'View weekly activity reports', icon: 'mdi-chart-bar', color: 'secondary', to: '/reports/war' },
    { title: 'Peer Evaluations', subtitle: 'View peer evaluation reports', icon: 'mdi-star-outline', color: 'success', to: '/reports/peer' },
  ]

  const studentCards = [
    { title: 'Weekly Activity', subtitle: 'Submit your WAR for this week', icon: 'mdi-calendar-edit', color: 'primary', to: '/war' },
    { title: 'Peer Evaluation', subtitle: 'Evaluate your teammates', icon: 'mdi-star-outline', color: 'secondary', to: '/peer-eval' },
    { title: 'My Report', subtitle: 'See how your team rates you', icon: 'mdi-chart-line', color: 'success', to: '/my-report' },
  ]
</script>
