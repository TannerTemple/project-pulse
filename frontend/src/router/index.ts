import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    // ── Public ──────────────────────────────────────────────────────────────
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/LoginView.vue'),
      meta: { public: true },
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('@/views/RegisterView.vue'),
      meta: { public: true },
    },

    // ── Shared ───────────────────────────────────────────────────────────────
    {
      path: '/',
      name: 'dashboard',
      component: () => import('@/views/DashboardView.vue'),
    },
    {
      path: '/account',
      name: 'account',
      component: () => import('@/views/AccountSettingsView.vue'),
    },

    // ── Admin ─────────────────────────────────────────────────────────────────
    {
      path: '/sections',
      name: 'sections',
      component: () => import('@/views/SectionListView.vue'),
      meta: { roles: ['ADMIN'] },
    },
    {
      path: '/sections/new',
      name: 'section-create',
      component: () => import('@/views/SectionFormView.vue'),
      meta: { roles: ['ADMIN'] },
    },
    {
      path: '/sections/:id/edit',
      name: 'section-edit',
      component: () => import('@/views/SectionFormView.vue'),
      meta: { roles: ['ADMIN'] },
    },
    {
      path: '/sections/:id/weeks',
      name: 'section-weeks',
      component: () => import('@/views/ActiveWeekSetupView.vue'),
      meta: { roles: ['ADMIN'] },
    },
    {
      path: '/teams',
      name: 'teams',
      component: () => import('@/views/TeamListView.vue'),
      meta: { roles: ['ADMIN', 'INSTRUCTOR'] },
    },
    {
      path: '/teams/new',
      name: 'team-create',
      component: () => import('@/views/TeamFormView.vue'),
      meta: { roles: ['ADMIN'] },
    },
    {
      path: '/teams/:id/edit',
      name: 'team-edit',
      component: () => import('@/views/TeamFormView.vue'),
      meta: { roles: ['ADMIN'] },
    },
    {
      path: '/students',
      name: 'students',
      component: () => import('@/views/StudentListView.vue'),
      meta: { roles: ['ADMIN', 'INSTRUCTOR'] },
    },
    {
      path: '/students/:id',
      name: 'student-detail',
      component: () => import('@/views/StudentDetailView.vue'),
      meta: { roles: ['ADMIN', 'INSTRUCTOR'] },
    },
    {
      path: '/instructors',
      name: 'instructors',
      component: () => import('@/views/InstructorListView.vue'),
      meta: { roles: ['ADMIN'] },
    },
    {
      path: '/rubrics',
      name: 'rubrics',
      component: () => import('@/views/RubricListView.vue'),
      meta: { roles: ['ADMIN'] },
    },
    {
      path: '/rubrics/new',
      name: 'rubric-create',
      component: () => import('@/views/RubricFormView.vue'),
      meta: { roles: ['ADMIN'] },
    },

    // ── Instructor ────────────────────────────────────────────────────────────
    {
      path: '/reports/peer',
      name: 'reports-peer',
      component: () => import('@/views/SectionPeerReportView.vue'),
      meta: { roles: ['ADMIN', 'INSTRUCTOR'] },
    },
    {
      path: '/reports/war',
      name: 'reports-war',
      component: () => import('@/views/TeamWARReportView.vue'),
      meta: { roles: ['ADMIN', 'INSTRUCTOR'] },
    },

    // ── Student ───────────────────────────────────────────────────────────────
    {
      path: '/war',
      name: 'war',
      component: () => import('@/views/WARActivityView.vue'),
      meta: { roles: ['STUDENT'] },
    },
    {
      path: '/peer-eval',
      name: 'peer-eval',
      component: () => import('@/views/PeerEvaluationView.vue'),
      meta: { roles: ['STUDENT'] },
    },
    {
      path: '/my-report',
      name: 'my-report',
      component: () => import('@/views/MyReportView.vue'),
      meta: { roles: ['STUDENT'] },
    },

    // Fallback
    { path: '/:pathMatch(.*)*', redirect: '/' },
  ],
})

router.beforeEach(to => {
  const auth = useAuthStore()

  // Redirect unauthenticated users away from protected routes
  if (!to.meta.public && !auth.isAuthenticated) {
    return { name: 'login' }
  }

  // Redirect already-logged-in users away from login/register
  if (to.meta.public && auth.isAuthenticated) {
    return { name: 'dashboard' }
  }

  // Role-based access
  const roles = to.meta.roles as string[] | undefined
  if (roles && auth.role && !roles.includes(auth.role)) {
    return { name: 'dashboard' }
  }
})

export default router
