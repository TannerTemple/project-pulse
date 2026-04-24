import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { api } from '@/api'

export type UserRole = 'ADMIN' | 'INSTRUCTOR' | 'STUDENT'

export interface AuthUser {
  userId: number
  firstName: string
  lastName: string
  role: UserRole
  token: string
}

export const useAuthStore = defineStore('auth', () => {
  const stored = localStorage.getItem('pp_user')
  let parsedUser: AuthUser | null = null
  try {
    parsedUser = stored ? JSON.parse(stored) : null
  } catch {
    localStorage.removeItem('pp_user')
    localStorage.removeItem('pp_token')
  }
  const user = ref<AuthUser | null>(parsedUser)

  const isAuthenticated = computed(() => !!user.value)
  const role = computed(() => user.value?.role ?? null)
  const fullName = computed(() =>
    user.value ? `${user.value.firstName} ${user.value.lastName}` : '',
  )

  async function login (email: string, password: string): Promise<void> {
    const data = await api.post<AuthUser>('/auth/login', { email, password })
    user.value = data
    localStorage.setItem('pp_user', JSON.stringify(data))
    localStorage.setItem('pp_token', data.token)
  }

  async function register (payload: {
    token: string
    firstName: string
    lastName: string
    middleInitial?: string
    password: string
    confirmPassword: string
  }): Promise<void> {
    const data = await api.post<AuthUser>('/auth/register', payload)
    user.value = data
    localStorage.setItem('pp_user', JSON.stringify(data))
    localStorage.setItem('pp_token', data.token)
  }

  function logout (): void {
    user.value = null
    localStorage.removeItem('pp_user')
    localStorage.removeItem('pp_token')
  }

  return { user, isAuthenticated, role, fullName, login, register, logout }
})
