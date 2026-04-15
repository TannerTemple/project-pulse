/**
 * Thin fetch wrapper for the Project Pulse REST API.
 * All requests are relative to /api (proxied to :8080 by Vite in dev).
 */

const BASE = '/api'

async function request<T>(path: string, options: RequestInit = {}): Promise<T> {
  const token = localStorage.getItem('pp_token')
  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
  }

  const res = await fetch(BASE + path, { ...options, headers })

  if (res.status === 204) return undefined as T

  const body = await res.json().catch(() => null)

  if (!res.ok) {
    const message =
      body?.messages?.[0] ?? body?.message ?? `Request failed (${res.status})`
    throw new Error(message)
  }

  return body as T
}

export const api = {
  get:    <T>(path: string)                   => request<T>(path),
  post:   <T>(path: string, data: unknown)    => request<T>(path, { method: 'POST',  body: JSON.stringify(data) }),
  put:    <T>(path: string, data: unknown)    => request<T>(path, { method: 'PUT',   body: JSON.stringify(data) }),
  patch:  <T>(path: string, data?: unknown)   => request<T>(path, { method: 'PATCH', body: data ? JSON.stringify(data) : undefined }),
  delete: <T>(path: string)                   => request<T>(path, { method: 'DELETE' }),
}
