# Development Plan — Project Pulse

> Work is done **use case by use case**. Each UC gets its own branch, backend
> implementation, frontend view, and tests before merging to `main`.
>
> Before starting any phase, read:
> - `CLAUDE.md` — architecture rules
> - `NAMING_CONVENTIONS.md` — naming standards
> - `TEST_STRATEGY.md` — how to write tests
> - `requirements/use-cases.md` — the specific UC you are implementing

---

## Branch Naming

```
feature/uc-<number>-<short-name>
```
Examples: `feature/uc-27-war-activities`, `feature/uc-28-peer-evaluation`

---

## Phase 0 — Foundation ✅ COMPLETE

> Branch: `feature/domain-model` — merged to main

- [x] JPA domain model (all entities + repositories)
- [x] JWT auth (login, register via invitation token)
- [x] Spring Security config (stateless, CORS, method security)
- [x] Global exception handler
- [x] Email service (async Gmail SMTP)
- [x] CI/CD workflows (GitHub Actions → Azure)
- [x] Dev profile (H2 in-memory, default admin seeded)
- [x] Frontend shell (login, register, dashboard, nav drawer)
- [x] Requirements docs in Markdown

---

## Phase 1 — Admin: Rubrics & Sections

> UC-1, UC-2, UC-3, UC-4, UC-5, UC-6
> Suggested branch: `feature/uc-1-6-admin-sections`

### Backend
- [x] UC-1: POST `/api/rubrics` — create rubric with criteria
- [x] PUT `/api/rubrics/{id}` — edit rubric (added post-UC-1; no separate UC)
- [x] UC-2: GET `/api/sections?name=` — find sections
- [x] UC-3: GET `/api/sections/{id}` — view section detail
- [x] UC-4: POST `/api/sections` — create section (select rubric)
- [x] UC-5: PUT `/api/sections/{id}` — edit section
- [x] UC-6: POST `/api/sections/{id}/weeks` — set up active weeks
- [x] Unit tests: `RubricServiceTest`, `ActiveWeekServiceTest`
- [x] Unit tests: `SectionServiceTest` (7 tests)
- [x] Controller tests: `SectionControllerTest` (5 tests)
- [x] Controller tests: `RubricControllerTest`

### Frontend
- [x] Rubric create/edit form (`RubricFormView.vue`) — dual-mode (create vs. edit based on route param)
- [x] Rubric list view (`RubricListView.vue`) — with edit button per rubric card
- [x] Section list view (`SectionListView.vue`)
- [x] Section create/edit form (`SectionFormView.vue`)
- [x] Active weeks configurator (`ActiveWeekSetupView.vue`)

---

## Phase 2 — Admin: Teams

> UC-7, UC-8, UC-9, UC-10, UC-14
> Suggested branch: `feature/uc-7-10-admin-teams`

### Backend
- [x] UC-7: GET `/api/teams?sectionId=&name=` — find teams
- [x] UC-8: GET `/api/teams/{id}` — view team
- [x] UC-9: POST `/api/teams` — create team
- [x] UC-10: PUT `/api/teams/{id}` — edit team
- [x] UC-14: DELETE `/api/teams/{id}` — delete team (cascade)
- [x] Unit tests: `TeamServiceTest`
- [x] Controller tests: `TeamControllerTest`

### Frontend
- [x] Team list view (`TeamListView.vue`)
- [x] Team create/edit form (`TeamFormView.vue`)
- [x] Delete confirmation dialog

---

## Phase 3 — Admin: User Management

> UC-11, UC-12, UC-13, UC-15, UC-16, UC-17, UC-18, UC-19, UC-20, UC-21, UC-22, UC-23, UC-24
> Suggested branch: `feature/uc-11-24-admin-users`

### Backend
- [x] UC-11: POST `/api/sections/{id}/invitations/students` — invite students
- [x] UC-12: POST `/api/teams/{id}/students` — assign students to team
- [x] UC-13: DELETE `/api/teams/{teamId}/students/{studentId}` — remove student
- [x] UC-15: GET `/api/students?firstName=&lastName=&sectionId=` — find students
- [x] UC-16: GET `/api/students/{id}` — view student
- [x] UC-17: DELETE `/api/students/{id}` — delete student
- [x] UC-18: POST `/api/invitations/instructors` — invite instructors
- [x] UC-19: POST `/api/teams/{id}/instructors` — assign instructors
- [x] UC-20: DELETE `/api/teams/{teamId}/instructors/{instructorId}` — remove instructor
- [x] UC-21: GET `/api/instructors?firstName=&lastName=&active=` — find instructors
- [x] UC-22: GET `/api/instructors/{id}` — view instructor
- [x] UC-23: PATCH `/api/instructors/{id}/deactivate` — deactivate
- [x] UC-24: PATCH `/api/instructors/{id}/reactivate` — reactivate
- [x] Unit tests: `UserServiceTest`
- [x] Controller tests: `UserControllerTest`

### Frontend
- [x] Student list view with invite modal (`StudentListView.vue`)
- [x] Student detail view (`StudentDetailView.vue`)
- [x] Instructor list view with invite modal + deactivate/reactivate (`InstructorListView.vue`)
- [x] Instructor detail view (`InstructorDetailView.vue`)
- [x] Team assignment UI (select from list in `TeamFormView.vue`)

---

## Phase 4 — Student: Account & WAR

> UC-25, UC-26, UC-27
> Suggested branch: `feature/uc-25-27-student-war`

### Backend
- [x] UC-25: POST `/api/auth/register` — set up student account *(done in auth layer)*
- [x] UC-26: PATCH `/api/users/me` — student edits own account
- [x] UC-27: CRUD `/api/war-activities` — manage WAR activities
  - GET `/api/war-activities?weekId=` — list for a week
  - POST `/api/war-activities` — add activity
  - PUT `/api/war-activities/{id}` — edit activity
  - DELETE `/api/war-activities/{id}` — delete activity
- [x] Enforce: cannot select future active week
- [x] Unit tests: `WARActivityServiceTest`
- [x] Controller tests: `WARActivityControllerTest`

### Frontend
- [x] Account settings view (`AccountSettingsView.vue`)
- [x] WAR form view (`WARActivityView.vue`)
  - Week selector (past weeks only, no future weeks)
  - Activity list with add/edit/delete
  - Modal form
  - Category and status dropdowns

---

## Phase 5 — Student: Peer Evaluation

> UC-28, UC-29
> Suggested branch: `feature/uc-28-29-peer-evaluation`

### Backend
- [x] UC-28: POST `/api/peer-evaluations` — submit peer evaluation
  - [x] Enforce BR-3: cannot edit after submission
  - [x] Enforce BR-4: only previous week, 1-week deadline
  - [x] Enforce BR-2: week must be active
  - [x] Enforce: evaluator and evaluatee must be teammates
- [x] UC-29: GET `/api/peer-evaluations/me/report?weekId=` — own report
  - [x] Enforce BR-5: never expose evaluator identity or private comments
- [x] Unit tests: `PeerEvaluationServiceTest`
- [x] Controller tests: `PeerEvaluationControllerTest`

### Frontend
- [x] Peer evaluation form (`PeerEvaluationView.vue`)
  - One card per teammate
  - Integer score inputs per rubric criterion
  - Public/private comment fields
  - Submit per-teammate
- [x] My report view (`MyReportView.vue`)
  - Week selector
  - Average criterion scores with progress bars
  - Public comments display
  - Overall grade

---

## Phase 6 — Instructor: Account & Reports

> UC-30, UC-31, UC-32, UC-33, UC-34
> Suggested branch: `feature/uc-30-34-instructor-reports`

### Backend
- [x] UC-30: POST `/api/auth/register` — instructor account setup *(done in auth layer)*
- [x] UC-31: GET `/api/sections/{id}/peer-evaluation-report?weekId=` — section-wide report
- [x] UC-32: GET `/api/teams/{id}/war-report?weekId=` — team WAR report
- [x] UC-33: GET `/api/students/{id}/peer-evaluation-report?start=&end=` — student report over range
- [x] UC-34: GET `/api/students/{id}/war-report?start=&end=` — student WAR report over range
- [x] Unit tests: `ReportServiceTest`
- [x] Controller tests: `ReportControllerTest`

### Frontend
- [x] Section peer eval report view (`SectionPeerReportView.vue`)
- [x] Team WAR report view (`TeamWARReportView.vue`)
- [x] Student detail view with peer eval + WAR history (`StudentDetailView.vue`)

---

## Phase 7 — Testing & Polish ✅ COMPLETE

### Tests (119 passing)

- [x] `ActiveWeekServiceTest` — 5 tests
- [x] `RubricControllerTest` — 6 tests
- [x] `TeamControllerTest` — 11 tests
- [x] `UserControllerTest` — 14 tests
- [x] `ReportServiceTest` — 13 tests

### Final polish

- [x] End-to-end smoke: create section → invite student → submit WAR → eval → generate report ✅
- [x] `STATUS.md` updated to 100% complete ✅

---

## Phase 8 — Database & Cloud Deployment ✅ COMPLETE

> **Note:** The deployment target may be **MySQL on AWS** instead of PostgreSQL on Azure.
> The Spring Boot app is database-agnostic at the service layer — only the JDBC driver,
> dialect, and connection URL change. The steps below show both paths; the team should
> confirm which cloud/DB they are using before provisioning.
>
> **Status:** CI/CD workflows already exist in `.github/workflows/`. The pipeline
> deploys to Azure on every merge to `main`. If switching to AWS, the CD workflow
> will need to be updated (e.g., deploy to Elastic Beanstalk or App Runner instead).

### Branch strategy

```
main  ←  feature/domain-model ✅ merged
main  ←  fix/active-weeks-rubric-team-fixes (Tanner — bug fixes, pending merge)
main  ←  feature/postgres-setup (Partner 1)
main  ←  feature/azure-deploy   (Partner 2)
```

Partners branch off `main` independently. No dependency between them until
Azure App Service is provisioned (Partner 2 needs the DB URL from Partner 1).

---

### 8-A  PostgreSQL Setup (Partner 1)

**Goal:** give the app a persistent database it can connect to in production.

#### Step 1 — Provision the database on Azure

1. In the Azure Portal, create a **Azure Database for PostgreSQL — Flexible Server**.
2. Choose the cheapest tier (Burstable B1ms is fine for a course project).
3. Set a database name, admin username, and password. Save these — they become env vars.
4. Under **Networking**, add a firewall rule to allow Azure services (and your own IP for local testing).

#### Step 2 — Create `application-prod.properties`

Create this file at `backend/src/main/resources/application-prod.properties`:

```properties
# PostgreSQL connection (values injected via Azure App Service env vars)
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
spring.datasource.driver-class-name=org.postgresql.Driver

spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=validate

# H2 console must be OFF in prod
spring.h2.console.enabled=false

# Email (already in base properties but needs prod values)
spring.mail.username=${MAIL_USERNAME}
spring.mail.password=${MAIL_PASSWORD}

# JWT
security.jwt.secret-key=${JWT_SECRET}

# App base URL (used in invite emails)
app.base-url=${APP_BASE_URL}
```

The `${VAR}` placeholders are resolved by Spring from environment variables at
runtime — you never hardcode secrets in a properties file.

#### Step 3 — Let Hibernate create the schema on first deploy

Temporarily set `ddl-auto=create` in `application-prod.properties`, deploy once,
then switch back to `ddl-auto=validate` for all future deploys. This lets
Hibernate generate the initial tables from the JPA entities.

Alternatively, use **Flyway**:
1. Add `spring-boot-starter-data-jpa` Flyway dependency to `pom.xml`.
2. Create `backend/src/main/resources/db/migration/V1__init.sql` with the schema.
3. Set `spring.flyway.enabled=true` in prod properties.

#### Step 4 — Test locally against PostgreSQL

```bash
# Install PostgreSQL locally if not already present
# Create a local database named projectpulse

export SPRING_PROFILES_ACTIVE=prod
export DB_URL=jdbc:postgresql://localhost:5432/projectpulse
export DB_USERNAME=youruser
export DB_PASSWORD=yourpassword
export JWT_SECRET=any-256-bit-string-for-local-testing
export MAIL_USERNAME=yourgmail@gmail.com
export MAIL_PASSWORD=your-app-password
export APP_BASE_URL=http://localhost:3000

cd backend && ./mvnw spring-boot:run
```

If the app starts and `/api/auth/login` returns a token, the DB connection works.

---

### 8-B  Azure App Service Deployment (Partner 2)

**Goal:** deploy the Spring Boot JAR to Azure and expose it at a public URL.
The GitHub Actions workflow (`.github/workflows/`) already handles the build and
deploy steps — you just need to provision the Azure resources and set the secrets.

#### Step 1 — Provision Azure App Service

1. In the Azure Portal, create an **App Service**.
2. Runtime stack: **Java 21**, OS: **Linux**.
3. Region: same as the PostgreSQL server to avoid cross-region latency.
4. Download the **Publish Profile** from the App Service overview page — you'll need it for GitHub.

#### Step 2 — Set GitHub secrets

In the GitHub repo → **Settings → Secrets and variables → Actions**, add:

| Secret name | Value |
|---|---|
| `AZURE_WEBAPP_NAME` | The App Service name (e.g. `project-pulse-app`) |
| `AZURE_WEBAPP_PUBLISH_PROFILE` | Paste the full XML content of the publish profile |

#### Step 3 — Set App Service environment variables

In the Azure Portal → App Service → **Configuration → Application settings**, add:

| Key | Value |
|---|---|
| `SPRING_PROFILES_ACTIVE` | `prod` |
| `DB_URL` | `jdbc:postgresql://<server>.postgres.database.azure.com:5432/<dbname>` |
| `DB_USERNAME` | The PostgreSQL admin username |
| `DB_PASSWORD` | The PostgreSQL admin password |
| `JWT_SECRET` | A random 256-bit (32-byte) base64 string — generate with `openssl rand -base64 32` |
| `MAIL_USERNAME` | Gmail address used for invite emails |
| `MAIL_PASSWORD` | Gmail app password (not your account password) |
| `APP_BASE_URL` | The live Azure URL, e.g. `https://project-pulse-app.azurewebsites.net` |

#### Step 4 — Update CORS in SecurityConfig

Open `backend/src/main/java/edu/tcu/cs/projectpulse/auth/SecurityConfig.java` and
add the live Azure frontend URL to the CORS allowed origins list alongside
`http://localhost:3000`. The static frontend (Vite build) is served from the same
App Service, so the origin is the same as `APP_BASE_URL`.

#### Step 5 — Trigger the pipeline and verify

1. Merge any open PR to `main` — the GitHub Actions CD job fires automatically.
2. Watch the Actions tab for a green checkmark.
3. Visit `https://<your-app>.azurewebsites.net/api/auth/login` — should return 405 (wrong method) meaning the app is up.
4. Test full login from the frontend URL.

---

### 8-C  Recommended execution order

```
1. ✅ Merge feature/domain-model PR → main           [Tanner, done]
2. ✅ Tests written (119 passing) + bug fixes merged  [Tanner, done]
3. Merge fix/active-weeks-rubric-team-fixes → main  [Tanner, in progress]
4. Branch feature/postgres-setup off main           [Partner 1, parallel]
   Create application-prod.properties → PR → main
5. Branch feature/azure-deploy off main             [Partner 2, parallel with 4]
   Provision App Service, set secrets → PR → main
6. Partner 2 sets DB env vars (needs Partner 1's DB URL)
7. Trigger CD pipeline → verify live URL            [all]
8. End-to-end smoke test on production              [all]
```

Steps 4 and 5 are fully independent — both can proceed in parallel after step 3 merges.

---

## What Still Needs to Be Done

### Tanner

- [x] Merge `feature/domain-model` → `main` ✅
- [x] Merge `fix/active-weeks-rubric-team-fixes` → `main` ✅
- [x] Azure URL added to CORS in `SecurityConfig.java` ✅
- [x] End-to-end smoke test on production ✅
- [x] `STATUS.md` updated to 100% complete ✅

### Partner 1 — Database ✅ COMPLETE

- [x] Confirmed database target: Azure PostgreSQL
- [x] Provisioned Azure Database for PostgreSQL (Flexible Server)
- [x] Created `backend/src/main/resources/application-prod.properties`
- [x] PostgreSQL JDBC driver already in `pom.xml`
- [x] Shared DB credentials with Partner 2

### Partner 2 — Deployment ✅ COMPLETE

- [x] Provisioned Azure App Service (Java 21, Linux)
- [x] Set all GitHub secrets: `AZURE_WEBAPP_NAME`, `AZURE_WEBAPP_PUBLISH_PROFILE`
- [x] Set all App Service environment variables
- [x] CD pipeline triggered — app deployed successfully
- [x] App is live and responding

---

## How to Start a New Use Case

```bash
# 1. Make sure you're on latest main
git checkout main && git pull

# 2. Create your branch
git checkout -b feature/uc-27-war-activities

# 3. Read the UC
# Open requirements/use-cases.md → find UC-27

# 4. Implement backend → frontend → tests

# 5. Commit with standard message
git commit -m "feat(UC-27): student manages WAR activities"

# 6. Push and open PR into main
git push origin feature/uc-27-war-activities
# Open PR on GitHub
```
