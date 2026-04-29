# CLAUDE.md — Project Pulse AI Development Guide

> **Read this file before responding to any request in this project.**
> It defines the architecture, constraints, patterns, and workflow that every
> contributor (human or AI) must follow consistently.

---

## 1. Project Overview

**Project Pulse** is a peer evaluation and weekly activity reporting system for
TCU's Senior Design course. Three user roles exist: **Admin**, **Instructor**,
and **Student**. Full requirements are in `requirements/`.

---

## 2. Required Tech Stack — No Exceptions

| Layer | Technology | Version |
|---|---|---|
| Backend | Spring Boot | 4.0.0-RC2 |
| Language | Java | 21 |
| Database | PostgreSQL (prod) / H2 (dev) | — |
| ORM | Hibernate / Spring Data JPA | — |
| Security | Spring Security + JJWT | 7.x / 0.12.6 |
| Frontend | Vue 3 + **Vuetify 4** | 3.5 / 4.0 |
| State | Pinia | 3.x |
| Router | Vue Router | 5.x |
| Build | Vite | 8.x |
| Testing (BE) | JUnit 5 + Mockito + SpringBootTest | — |

**Forbidden:** ElementPlus, Bootstrap, Tailwind, any UI library that is not Vuetify 4.

---

## 3. Repository Structure

```
project-pulse/
├── backend/                        Spring Boot application
│   └── src/main/java/edu/tcu/cs/projectpulse/
│       ├── auth/                   JWT, SecurityConfig, AuthController
│       ├── common/exception/       GlobalExceptionHandler, ApiError
│       ├── <feature>/              One package per domain feature (see §4)
│       │   ├── <Entity>.java
│       │   ├── <Entity>Repository.java
│       │   ├── <Entity>Service.java
│       │   ├── <Entity>Controller.java
│       │   └── dto/
│       │       ├── <Entity>Request.java
│       │       └── <Entity>Response.java
│       └── DataSeeder.java         Seeds default admin in dev
├── frontend/
│   └── src/
│       ├── api/index.ts            Fetch wrapper (Bearer token injection)
│       ├── stores/auth.ts          Pinia auth store
│       ├── router/index.ts         Vue Router with auth guard
│       ├── views/                  One .vue file per route/use-case
│       └── components/             Reusable components only
├── requirements/                   Glossary, Vision & Scope, Use Cases (MD)
├── CLAUDE.md                       ← You are here
├── DEVELOPMENT_PLAN.md             Phase checklist
├── TEST_STRATEGY.md                How to write tests
├── NAMING_CONVENTIONS.md           Naming rules
└── STATUS.md                       Progress snapshot
```

---

## 4. Backend Architecture Rules

### 4.1 Feature packages
Every domain feature lives in its own package. Do **not** use a flat
`service/`, `controller/`, `repository/` layer structure.

```
edu.tcu.cs.projectpulse.section       ← correct
edu.tcu.cs.projectpulse.service       ← WRONG
```

### 4.2 Entity → Repository → Service → Controller → DTO flow
Every feature follows this exact chain. Never skip a layer.

- **Entity** — JPA entity, Lombok (`@Getter @Setter @NoArgsConstructor`).
  No `@Data` on entities (breaks Hibernate lazy-loading).
- **Repository** — `JpaRepository<Entity, Long>` interface. No logic here.
- **Service** — All business logic. `@Transactional`. `@PreAuthorize` for role guards.
- **Controller** — Thin. Calls service, returns `ResponseEntity`. No logic.
- **DTOs** — Java `record` types. `Request` for input, `Response` for output.
  Never expose entities directly in API responses.

### 4.3 Security
- All service methods that touch data must have `@PreAuthorize("hasRole('ADMIN')")` etc.
- `@EnableMethodSecurity` is already on `SecurityConfig`.
- JWT filter is already wired. Do not modify `SecurityConfig` unless adding new public endpoints.
- Spring Boot 4.x uses `@MockitoBean` in tests (not `@MockBean`).

### 4.4 Error handling
- Throw `ObjectNotFoundException` for missing records → maps to 404.
- Throw `IllegalArgumentException` for bad input → maps to 400.
- Throw `IllegalStateException` for conflicts (duplicate name etc.) → maps to 409.
- Never throw raw `RuntimeException`. Always use the typed exceptions in `common/exception/`.

### 4.5 Database profiles
- `dev` profile (default): H2 in-memory, `ddl-auto=create-drop`.
- `prod` profile: PostgreSQL, `ddl-auto=validate`.
- Override via `SPRING_PROFILES_ACTIVE` env var on Azure.

---

## 5. Frontend Architecture Rules

### 5.1 Structure
- Route-level pages go in `src/views/`. One file per use case or logical screen.
- Reusable pieces go in `src/components/`. Only create a component if it is
  used in more than one view.
- All API calls go through `src/api/index.ts`. Never use `fetch` or `axios` directly in a component.
- All auth state lives in `src/stores/auth.ts`. Never store a JWT anywhere other than `localStorage` via the auth store.

### 5.2 Vuetify 4
- Use Vuetify components exclusively for all UI elements.
- Use `variant="outlined"` on all form fields.
- Use `density="comfortable"` on form fields.
- Use `rounded="lg"` on cards.
- Do not import individual Vuetify components — auto-import is configured via `vite-plugin-vuetify`.

### 5.3 Forms
Every form must:
1. Use `v-form` with a `ref`.
2. Call `formRef.value.validate()` before submitting.
3. Show a `v-alert type="error"` for API errors.
4. Show a `v-btn :loading="loading"` state during submission.

### 5.4 Navigation guard
`src/router/index.ts` already has a guard. When adding routes:
- Public routes (no auth): add `meta: { public: true }`.
- All other routes are protected by default.
- Add `meta: { roles: ['ADMIN'] }` for role-restricted routes (update the guard accordingly).

---

## 6. How to Implement a Use Case

Follow these steps **in order** for every use case:

1. **Read** `requirements/use-cases.md` for the UC being implemented.
2. **Check** `STATUS.md` — confirm the UC is not already done.
3. **Backend first:**
   a. Entity changes (if any) — update the JPA entity.
   b. Repository method (if needed) — add to the repository interface.
   c. Service method — business logic + `@PreAuthorize`.
   d. Controller endpoint — thin, delegates to service.
   e. DTOs — `Request` record + `Response` record.
   f. Unit test for the service method.
   g. Integration test for the controller endpoint.
4. **Frontend second:**
   a. Add route to `src/router/index.ts`.
   b. Add nav item in `src/App.vue` if needed.
   c. Create `src/views/<FeatureView>.vue`.
   d. Wire API call through `src/api/index.ts`.
5. **Update** `DEVELOPMENT_PLAN.md` — check the box.
6. **Commit** with message format: `feat(UC-N): <short description>`.

---

## 7. Commit Message Format

```
feat(UC-N): implement <use case name>
fix(UC-N): <what was broken>
test(UC-N): add unit/integration tests
chore: <non-feature work>
```

Examples:
```
feat(UC-27): student manages WAR activities
feat(UC-28): student submits peer evaluation
test(UC-27): unit + integration tests for WAR activity service
```

---

## 8. Branch Strategy

| Branch | Purpose |
|---|---|
| `main` | Stable, always deployable |
| `feature/uc-N-short-name` | One branch per use case (or small cluster) |
| `bugfix/short-name` | Bug fixes |

Always branch off `main`. Open a PR into `main` when the UC is complete and tested.

**MANDATORY — Start of every session:**
```bash
git checkout main && git pull
```
Do this before creating any branch or writing any code. Local `main` can silently
fall behind `origin/main` between sessions, causing branches to be built on stale
history. Never skip this step.

---

## 9. What Already Exists — Do Not Recreate

| What | Location | Status |
|---|---|---|
| All JPA entities + repositories | `backend/.../` each feature package | ✅ Done |
| JWT auth (login + register) | `auth/` | ✅ Done |
| Spring Security config + CORS | `auth/SecurityConfig.java` | ✅ Done |
| Global exception handler | `common/exception/` | ✅ Done |
| Email service | `email/EmailService.java` | ✅ Done |
| Admin APIs (rubric, section, team, user, activeweek) | each feature package | ✅ Done |
| DataSeeder (default admin) | `DataSeeder.java` | ✅ Done |
| Frontend auth (login, register, dashboard shell) | `src/views/` | ✅ Done |
| Vite proxy `/api` → `:8080` | `vite.config.mts` | ✅ Done |
| CI/CD workflows | `.github/workflows/` | ✅ Done |
| Dev profile (H2 default) | `application.properties` | ✅ Done |

---

## 10. Key Business Rules to Enforce in Code

| Rule | Where to enforce |
|---|---|
| BR-1: Every team needs ≥1 instructor | `TeamService.assignStudents` |
| BR-2: Students submit evals only during active weeks | `PeerEvaluationService` |
| BR-3: Peer evals cannot be edited once submitted | `PeerEvaluationService.submit` |
| BR-4: Eval is for previous week only; 1-week deadline | `PeerEvaluationService` |
| BR-5: Students never see evaluator identity or private comments | `PeerEvaluationService.getOwnReport` |

---

## 11. Running Locally

```bash
# Terminal 1 — backend (IntelliJ green button OR:)
cd backend && ./mvnw spring-boot:run
# Starts on http://localhost:8080 with H2 in-memory DB

# Terminal 2 — frontend
cd frontend && npm run dev
# Starts on http://localhost:3000

# Default admin login
# Email:    admin@projectpulse.app
# Password: Admin1234!
```
