# Project Pulse — Development Status

> Last updated: 2026-04-15
> Branch: `feature/domain-model`

---

## Overall Completion: ~35%

| Area | Status | % Done |
|---|---|---|
| Domain model (JPA entities) | Complete | 100% |
| Auth / JWT / Spring Security | Complete | 100% |
| Admin REST APIs | Mostly complete | ~90% |
| Student REST APIs | Not started | 0% |
| Instructor / Report APIs | Not started | 0% |
| Frontend (Vue 3 + Vuetify) | Scaffolding only | ~2% |
| Database (PostgreSQL) | Schema defined, not connected | ~10% |
| Deployment (Azure) | CI/CD written, Azure not configured | ~15% |

---

## What Has Been Done

### Commit 1 — JPA Domain Model
**Files:** 22 added | **Package:** `backend/src/main/java/edu/tcu/cs/projectpulse/`

All domain entities modelled with JPA annotations, Lombok, validation constraints,
and Spring Data repositories. Compiles clean against Spring Boot 4.0.0-RC2 / Java 21.

| Package | Contents |
|---|---|
| `user/` | `AppUser`, `UserRole` enum, `UserRepository` |
| `section/` | `Section`, `SectionRepository` |
| `team/` | `Team` (+ `team_instructors` join table), `TeamRepository` |
| `rubric/` | `Rubric`, `Criterion`, `RubricRepository`, `CriterionRepository` |
| `activeweek/` | `ActiveWeek`, `ActiveWeekRepository` |
| `war/` | `WARActivity`, `ActivityCategory` enum, `ActivityStatus` enum, `WARActivityRepository` |
| `peerevaluation/` | `PeerEvaluation`, `EvaluationScore`, repositories |
| `invitation/` | `InvitationToken`, `InvitationTokenRepository` |

**Key design decisions:**
- Single `AppUser` table with a `UserRole` discriminator — simpler Spring Security integration than table-per-class inheritance
- Students get a `section` FK (set at invite time) + nullable `team` FK (set when assigned by admin)
- Instructors use the `team_instructors` ManyToMany join table; `supervisedTeams` is the back-reference
- `PeerEvaluation` has a unique constraint on `(evaluator_id, evaluatee_id, week_id)`
- `EvaluationScore.score` is `Integer` per UC-28 requirement
- `InvitationToken.section` is nullable — student invites include it, instructor invites do not

---

### Commit 2 — Auth / JWT Layer
**Files:** 14 added | **Package:** `auth/`, updates to `pom.xml` and `application.properties`

Stateless JWT authentication using JJWT 0.12.6 and Spring Security 7.

| File | Purpose |
|---|---|
| `JwtProperties` | Binds `jwt.secret` / `jwt.expiration-ms` from properties |
| `JwtService` | Generates and validates signed HS256 tokens |
| `JwtAuthFilter` | `OncePerRequestFilter` — reads `Bearer` header, sets `SecurityContext` |
| `AppUserDetails` | `UserDetails` wrapper around `AppUser` |
| `AppUserDetailsService` | Loads user by email for Spring Security |
| `SecurityConfig` | Stateless filter chain, CORS (port 5173/4173), `@EnableMethodSecurity` |
| `AuthService` | Login and registration business logic |
| `AuthController` | `POST /api/auth/login`, `POST /api/auth/register` |
| `dto/` | `LoginRequest`, `LoginResponse`, `RegisterRequest` (Java records) |

**Registration flow:** validates `InvitationToken` (not used, not expired) → creates `AppUser`
with correct role/section → marks token as used → returns JWT for immediate login.

**application.properties changes:** Removed temporary DB exclusions, added env-var
placeholders for datasource, JWT secret, and expiration. Added `application-local.properties`
to `.gitignore` for per-dev overrides.

**Teammates:** Create `backend/src/main/resources/application-local.properties` (git-ignored):
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/projectpulse
spring.datasource.username=postgres
spring.datasource.password=yourpassword
jwt.secret=<base64-256bit-key>
```

---

### Commit 3 — CI/CD, Requirements Docs, Admin APIs
**Files:** 34 added | **Packages:** `.github/`, `requirements/`, multiple backend packages

#### CI/CD
| File | Purpose |
|---|---|
| `.github/workflows/ci.yml` | Runs on push/PR: backend `mvnw verify` + frontend type-check/lint |
| `.github/workflows/cd.yml` | Azure App Service deploy — triggered manually or after CI on `main`. **Requires secrets:** `AZURE_WEBAPP_NAME`, `AZURE_WEBAPP_PUBLISH_PROFILE` |

#### Requirements Docs (`requirements/`)
| File | Contents |
|---|---|
| `glossary.md` | All domain term definitions |
| `vision-and-scope.md` | Business context, process flows, objectives, stakeholders |
| `use-cases.md` | All 34 use cases + business rules in Markdown |

#### Backend

**Common:**
- `common/exception/ObjectNotFoundException` — thrown by all services for 404 cases
- `common/exception/ApiError` — standard error response record
- `common/exception/GlobalExceptionHandler` — `@RestControllerAdvice` mapping exceptions to HTTP responses

**Email (`email/`):**
- `EmailService` — `@Async` Gmail SMTP methods: invitation, team assignment/removal, account reactivation

**Admin APIs — all protected with `@PreAuthorize("hasRole('ADMIN')")`:**

| Package | UC Coverage | Endpoints |
|---|---|---|
| `rubric/` | UC-1 | `GET /api/rubrics`, `GET /api/rubrics/{id}`, `POST /api/rubrics` |
| `section/` | UC-2–5 | `GET /api/sections`, `GET /api/sections/{id}`, `POST`, `PUT /{id}` |
| `activeweek/` | UC-6 | `GET /api/sections/{id}/weeks`, `POST /api/sections/{id}/weeks` |
| `team/` | UC-7–14, 19–20 | Full CRUD + `/students`, `/instructors` assignment sub-resources |
| `user/` | UC-11–18, 23–24 | Student/instructor invite, find, view, deactivate, reactivate, delete |

**`pom.xml` additions:** `spring-boot-starter-mail`, `h2` (test scope)

**Test override:** `src/test/resources/application.properties` uses H2 in-memory so CI
runs without a real PostgreSQL instance.

---

## What Still Needs to Be Done

### Backend — Student APIs
> Good standalone task for a teammate.

| UC | Endpoint | Description |
|---|---|---|
| UC-26 | `PATCH /api/users/me` | Student edits their own account |
| UC-27 | `GET/POST/PUT/DELETE /api/war-activities` | Manage WAR activities for a week |
| UC-28 | `POST /api/peer-evaluations` | Submit peer evaluation for the previous week |
| UC-29 | `GET /api/peer-evaluations/me/report` | View own peer evaluation report |

**Business rules to enforce:**
- UC-27: Cannot select a future week; students can submit WARs outside active weeks
- UC-28: Only previous week; cannot edit after submission (BR-3); 1-week deadline (BR-4)
- UC-29: Never expose evaluator identity or private comments (BR-5)

---

### Backend — Instructor / Report APIs
> Good standalone task for a teammate.

| UC | Endpoint | Description |
|---|---|---|
| UC-31 | `GET /api/sections/{id}/peer-evaluation-report` | Section-wide peer eval report for a week |
| UC-32 | `GET /api/teams/{id}/war-report` | Team WAR report for a week |
| UC-33 | `GET /api/students/{id}/peer-evaluation-report` | Student peer eval report over date range |
| UC-34 | `GET /api/students/{id}/war-report` | Student WAR report over date range |

**Report grade algorithm (UC-31, UC-33, UC-29):**
1. Collect all `PeerEvaluation` rows where `evaluatee = student` for the week
2. For each, sum all `EvaluationScore.score` values → per-evaluator total
3. Average the per-evaluator totals → final grade

---

### Frontend (Vue 3 + Vuetify 4)
> Largest remaining effort. No ElementPlus — Vuetify only.

#### Auth Views
- [ ] Login page (`/login`)
- [ ] Registration page (`/register?token=...`) — pre-fills email from token

#### Admin Views
- [ ] Dashboard / nav shell with role-based sidebar
- [ ] Sections list + create/edit forms
- [ ] Teams list + create/edit + member assignment
- [ ] Students list + invite modal + view detail
- [ ] Instructors list + invite modal + deactivate/reactivate
- [ ] Rubric builder
- [ ] Active weeks configurator

#### Student Views
- [ ] Weekly dashboard (current active week)
- [ ] WAR form (add/edit/delete activities)
- [ ] Peer evaluation form (one card per teammate)
- [ ] My peer evaluation report view

#### Instructor Views
- [ ] Team WAR report view
- [ ] Section peer evaluation report view
- [ ] Student detail with drill-down report

---

### Database / Infrastructure
- [ ] Provision Azure Database for PostgreSQL
- [ ] Set `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` as Azure App Service env vars
- [ ] Set `JWT_SECRET`, `MAIL_USERNAME`, `MAIL_PASSWORD` as Azure App Service env vars
- [ ] Add GitHub secrets: `AZURE_WEBAPP_NAME`, `AZURE_WEBAPP_PUBLISH_PROFILE`
- [ ] Set `spring.jpa.hibernate.ddl-auto=validate` for production (use Flyway or Liquibase for migrations)
- [ ] Seed the initial Admin account (no invite flow exists for the first admin — needs a data script or `/api/admin/seed` endpoint behind a one-time-use flag)

---

### Known Gaps / TODOs
- **Admin seeding:** There is currently no way to create the first `ADMIN` user — the registration endpoint only works with invitation tokens. A seed script or a one-time setup endpoint is needed.
- **Pagination:** UC search results don't paginate yet (acceptable for the course scale of ~40 students).
- **WAR delete cascade:** When a team is deleted, WARs/peer evals belonging to that team's students are detached at the service layer but not yet bulk-deleted via a repository query.
- **BR-3 enforcement:** `PeerEvaluation.submitted` flag is persisted but the service layer for submission (UC-28) is not yet written.
- **Report format:** Use cases specify HTML output — the report endpoints currently plan to return JSON; a Thymeleaf or raw-HTML response layer will be needed.

---

## Recommended Handoff Split

| Teammate | Task |
|---|---|
| **You** | Student APIs (UC-26–29) + frontend auth views |
| **Teammate 2** | Instructor/Report APIs (UC-31–34) + frontend instructor views |
| **Teammate 3** | Frontend admin views + Azure infrastructure setup |

All three can work in parallel on separate branches from `feature/domain-model`.
