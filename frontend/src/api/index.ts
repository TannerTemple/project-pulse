/**
 * Thin fetch wrapper for the Project Pulse REST API.
 * All requests are relative to /api (proxied to :8080 by Vite in dev).
 */

const BASE = '/api'

async function request<T> (path: string, options: RequestInit = {}): Promise<T> {
  const token = localStorage.getItem('pp_token')
  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
  }

  const res = await fetch(BASE + path, { ...options, headers })

  if (res.status === 204) {
    return undefined as T
  }

  const body = await res.json().catch(() => null)

  if (!res.ok) {
    const message
      = body?.messages?.[0] ?? body?.message ?? `Request failed (${res.status})`
    throw new Error(message)
  }

  return body as T
}

export const api = {
  get: <T>(path: string) => request<T>(path),
  post: <T>(path: string, data: unknown) => request<T>(path, { method: 'POST', body: JSON.stringify(data) }),
  put: <T>(path: string, data: unknown) => request<T>(path, { method: 'PUT', body: JSON.stringify(data) }),
  patch: <T>(path: string, data?: unknown) => request<T>(path, { method: 'PATCH', body: data ? JSON.stringify(data) : undefined }),
  delete: <T>(path: string) => request<T>(path, { method: 'DELETE' }),
}

// ── Typed API helpers ────────────────────────────────────────────────────────

export interface Section {
  id: number
  name: string
  startDate: string
  endDate: string
  rubric: Rubric | null
  teamNames: string[]
}
export interface Rubric {
  id: number
  name: string
  criteria: Criterion[]
}
export interface Criterion {
  id: number
  name: string
  description: string
  maxScore: number
  orderIndex: number
}
export interface Team {
  id: number
  name: string
  sectionId: number
  sectionName: string
  studentNames: string[]
  instructorNames: string[]
}
export interface User {
  id: number
  firstName: string
  lastName: string
  middleInitial?: string
  email: string
  role: string
  active: boolean
  registrationComplete: boolean
  sectionId?: number
  sectionName?: string
  teamId?: number
  teamName?: string
}
export interface ActiveWeek {
  id: number
  weekStart: string
  active: boolean
}
export interface WARActivity {
  id: number
  category: string
  activity: string
  description?: string
  plannedHours: number
  actualHours?: number
  status: string
  weekId: number
  weekStart: string
  studentId: number
  studentName: string
}
export interface PeerEvaluationReport {
  weekId: number
  studentId: number
  studentName: string
  evaluatorCount: number
  overallGrade: number
  criterionAverages: { criterionId: number, criterionName: string, averageScore: number, maxScore: number }[]
  publicComments: string[]
}

// ── Report types (UC-31–34) ───────────────────────────────────────────────────

export interface ScoreDetail {
  criterionName: string
  score: number
}
export interface EvaluatorDetail {
  evaluatorId: number
  evaluatorName: string
  scores: ScoreDetail[]
  publicComments: string | null
  privateComments: string | null
}
export interface StudentPeerSummary {
  studentId: number
  studentName: string
  grade: number
  submitted: boolean
  evaluations: EvaluatorDetail[]
}
export interface SectionPeerReport {
  sectionId: number
  sectionName: string
  weekId: number
  weekStart: string
  students: StudentPeerSummary[]
  nonSubmitters: string[]
}
export interface ActivityRow {
  id: number
  category: string
  activity: string
  description: string | null
  plannedHours: number
  actualHours: number | null
  status: string
}
export interface StudentWAREntry {
  studentId: number
  studentName: string
  activities: ActivityRow[]
}
export interface TeamWARReport {
  teamId: number
  teamName: string
  weekId: number
  weekStart: string
  entries: StudentWAREntry[]
  nonSubmitters: string[]
}
export interface StudentPeerWeekEntry {
  weekId: number
  weekStart: string
  grade: number
  evaluations: EvaluatorDetail[]
}
export interface StudentPeerRangeReport {
  studentId: number
  studentName: string
  weeks: StudentPeerWeekEntry[]
}
export interface StudentWARWeekEntry {
  weekId: number
  weekStart: string
  activities: ActivityRow[]
}
export interface StudentWARRangeReport {
  studentId: number
  studentName: string
  weeks: StudentWARWeekEntry[]
}
