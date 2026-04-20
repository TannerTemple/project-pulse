<template>
  <v-app>
    <!-- ── Authenticated layout ─────────────────────────────────────────── -->
    <template v-if="auth.isAuthenticated">

      <v-navigation-drawer v-model="drawer" :rail="rail" permanent>
        <!-- Header -->
        <v-list-item
          prepend-icon="mdi-pulse"
          title="Project Pulse"
          nav
        >
          <template #append>
            <v-btn
              :icon="rail ? 'mdi-chevron-right' : 'mdi-chevron-left'"
              variant="text"
              @click="rail = !rail"
            />
          </template>
        </v-list-item>

        <v-divider />

        <!-- Nav items filtered by role -->
        <v-list density="compact" nav>
          <v-list-item
            v-for="item in navItems"
            :key="item.to"
            :prepend-icon="item.icon"
            :title="item.title"
            :to="item.to"
            rounded="lg"
          />
        </v-list>

        <!-- User info pinned to bottom -->
        <template #append>
          <v-divider />
          <v-list-item
            :prepend-icon="roleIcon"
            :title="auth.fullName"
            :subtitle="roleLabel"
            nav
          >
            <template #append>
              <v-btn icon="mdi-account-cog" variant="text" size="small" :to="{ name: 'account' }" />
              <v-btn icon="mdi-logout" variant="text" size="small" @click="logout" />
            </template>
          </v-list-item>
        </template>
      </v-navigation-drawer>

      <v-app-bar elevation="0" border="b">
        <v-app-bar-nav-icon @click="drawer = !drawer" class="d-sm-none" />
        <v-app-bar-title>{{ pageTitle }}</v-app-bar-title>
      </v-app-bar>

    </template>
    <!-- ── End authenticated layout ─────────────────────────────────────── -->

    <v-main>
      <router-view />
    </v-main>
  </v-app>
</template>

<script lang="ts" setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const auth   = useAuthStore()
const route  = useRoute()
const router = useRouter()

const drawer = ref(true)
const rail   = ref(false)

const roleLabel = computed(() => {
  const map: Record<string, string> = { ADMIN: 'Administrator', INSTRUCTOR: 'Instructor', STUDENT: 'Student' }
  return map[auth.role ?? ''] ?? ''
})

const roleIcon = computed(() => {
  const map: Record<string, string> = { ADMIN: 'mdi-shield-account', INSTRUCTOR: 'mdi-teach', STUDENT: 'mdi-account-school' }
  return map[auth.role ?? ''] ?? 'mdi-account'
})

const pageTitle = computed(() => String(route.name ?? 'Dashboard'))

const adminNav = [
  { title: 'Dashboard',    icon: 'mdi-view-dashboard',    to: '/' },
  { title: 'Sections',     icon: 'mdi-school',             to: '/sections' },
  { title: 'Teams',        icon: 'mdi-account-group',      to: '/teams' },
  { title: 'Students',     icon: 'mdi-account-multiple',   to: '/students' },
  { title: 'Instructors',  icon: 'mdi-teach',              to: '/instructors' },
  { title: 'Rubrics',      icon: 'mdi-format-list-checks', to: '/rubrics' },
]

const instructorNav = [
  { title: 'Dashboard',       icon: 'mdi-view-dashboard', to: '/' },
  { title: 'My Teams',        icon: 'mdi-account-group',  to: '/teams' },
  { title: 'WAR Reports',     icon: 'mdi-chart-bar',      to: '/reports/war' },
  { title: 'Peer Reports',    icon: 'mdi-star-outline',   to: '/reports/peer' },
]

const studentNav = [
  { title: 'Dashboard',       icon: 'mdi-view-dashboard', to: '/' },
  { title: 'Weekly Activity', icon: 'mdi-calendar-edit',  to: '/war' },
  { title: 'Peer Evaluation', icon: 'mdi-star-outline',   to: '/peer-eval' },
  { title: 'My Report',       icon: 'mdi-chart-line',     to: '/my-report' },
]

const navItems = computed(() => {
  const map: Record<string, typeof adminNav> = {
    ADMIN:      adminNav,
    INSTRUCTOR: instructorNav,
    STUDENT:    studentNav,
  }
  return map[auth.role ?? ''] ?? []
})

function logout() {
  auth.logout()
  router.push({ name: 'login' })
}
</script>
