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
- [x] UC-2: GET `/api/sections?name=` — find sections
- [x] UC-3: GET `/api/sections/{id}` — view section detail
- [x] UC-4: POST `/api/sections` — create section (select rubric)
- [x] UC-5: PUT `/api/sections/{id}` — edit section
- [x] UC-6: POST `/api/sections/{id}/weeks` — set up active weeks
- [ ] Unit tests: `RubricServiceTest`, `SectionServiceTest`, `ActiveWeekServiceTest`
- [ ] Controller tests: `RubricControllerTest`, `SectionControllerTest`

### Frontend
- [ ] Rubric builder view (`RubricFormView.vue`)
- [ ] Section list view (`SectionListView.vue`)
- [ ] Section create/edit form (`SectionFormView.vue`)
- [ ] Active weeks configurator (`ActiveWeekSetupView.vue`)

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
- [ ] Unit tests: `TeamServiceTest`
- [ ] Controller tests: `TeamControllerTest`

### Frontend
- [ ] Team list view (`TeamListView.vue`)
- [ ] Team create/edit form (`TeamFormView.vue`)
- [ ] Team detail view with member roster (`TeamDetailView.vue`)
- [ ] Delete confirmation dialog

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
- [ ] Unit tests: `UserServiceTest`
- [ ] Controller tests: `UserControllerTest`

### Frontend
- [ ] Student list view with invite modal (`StudentListView.vue`)
- [ ] Student detail view (`StudentDetailView.vue`)
- [ ] Instructor list view with invite modal (`InstructorListView.vue`)
- [ ] Instructor detail view with deactivate/reactivate (`InstructorDetailView.vue`)
- [ ] Team assignment UI (drag-and-drop or select from list)

---

## Phase 4 — Student: Account & WAR

> UC-25, UC-26, UC-27
> Suggested branch: `feature/uc-25-27-student-war`

### Backend
- [x] UC-25: POST `/api/auth/register` — set up student account *(done in auth layer)*
- [ ] UC-26: PATCH `/api/users/me` — student edits own account
- [ ] UC-27: CRUD `/api/war-activities` — manage WAR activities
  - GET `/api/war-activities?weekId=` — list for a week
  - POST `/api/war-activities` — add activity
  - PUT `/api/war-activities/{id}` — edit activity
  - DELETE `/api/war-activities/{id}` — delete activity
- [ ] Enforce: cannot select future active week
- [ ] Unit tests: `WARActivityServiceTest`
- [ ] Controller tests: `WARActivityControllerTest`

### Frontend
- [ ] Account settings view (`AccountSettingsView.vue`)
- [ ] WAR form view (`WARActivityView.vue`)
  - Week selector (active weeks only, no future weeks)
  - Activity list with add/edit/delete
  - Inline edit or modal form
  - Category and status dropdowns

---

## Phase 5 — Student: Peer Evaluation

> UC-28, UC-29
> Suggested branch: `feature/uc-28-29-peer-evaluation`

### Backend
- [ ] UC-28: POST `/api/peer-evaluations` — submit peer evaluation
  - Enforce BR-3: cannot edit after submission
  - Enforce BR-4: only previous week, 1-week deadline
  - Enforce: every team member must be evaluated
- [ ] UC-29: GET `/api/peer-evaluations/me/report?weekId=` — own report
  - Enforce BR-5: never expose evaluator identity or private comments
- [ ] Unit tests: `PeerEvaluationServiceTest`
- [ ] Controller tests: `PeerEvaluationControllerTest`

### Frontend
- [ ] Peer evaluation form (`PeerEvaluationView.vue`)
  - One section per teammate
  - Integer score inputs per rubric criterion
  - Public/private comment fields
  - Submit confirmation
- [ ] My report view (`MyReportView.vue`)
  - Week selector
  - Average criterion scores table
  - Public comments display
  - Overall grade

---

## Phase 6 — Instructor: Account & Reports

> UC-30, UC-31, UC-32, UC-33, UC-34
> Suggested branch: `feature/uc-30-34-instructor-reports`

### Backend
- [x] UC-30: POST `/api/auth/register` — instructor account setup *(done in auth layer)*
- [ ] UC-31: GET `/api/sections/{id}/peer-evaluation-report?weekId=` — section-wide report
  - Implement grade algorithm (average of evaluators' total scores)
  - Show who did NOT submit
- [ ] UC-32: GET `/api/teams/{id}/war-report?weekId=` — team WAR report
  - Show who did NOT submit
- [ ] UC-33: GET `/api/students/{id}/peer-evaluation-report?start=&end=` — student report over range
- [ ] UC-34: GET `/api/students/{id}/war-report?start=&end=` — student WAR report over range
- [ ] Unit tests: `ReportServiceTest`
- [ ] Controller tests: `ReportControllerTest`

### Frontend
- [ ] Section peer eval report view (`SectionPeerReportView.vue`)
  - Week selector
  - Table: student, grade, comments, commenter
  - Drill-down modal for individual scores
  - Missing submissions highlighted
- [ ] Team WAR report view (`TeamWARReportView.vue`)
  - Week selector
  - Table: student, category, activity, hours, status
- [ ] Student detail: peer eval tab (`StudentPeerReportView.vue`)
- [ ] Student detail: WAR tab (`StudentWARReportView.vue`)

---

## Phase 7 — Testing & Polish

> All branches merged into main. One final quality pass.

- [ ] Achieve 80%+ service layer test coverage
- [ ] Every controller endpoint has ≥1 happy + ≥1 error test
- [ ] Auth integration test (login, register, token validation)
- [ ] End-to-end smoke test: create section → invite student → submit WAR → submit eval → generate report
- [ ] Fix any Vuetify console warnings
- [ ] Responsive layout check (mobile + desktop)
- [ ] Update `STATUS.md` to reflect 100% completion

---

## Phase 8 — Deployment

> Requires Azure subscription.

- [ ] Provision Azure Database for PostgreSQL
- [ ] Provision Azure App Service (Java 21)
- [ ] Set GitHub secrets: `AZURE_WEBAPP_NAME`, `AZURE_WEBAPP_PUBLISH_PROFILE`
- [ ] Set Azure App Service env vars:
  - `SPRING_PROFILES_ACTIVE=prod`
  - `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`
  - `JWT_SECRET` (generate a real 256-bit key)
  - `MAIL_USERNAME`, `MAIL_PASSWORD`
  - `APP_BASE_URL` (live URL)
- [ ] Update `SecurityConfig` CORS to allow the live Azure URL
- [ ] Trigger CD pipeline and verify live URL
- [ ] Test login on production URL

---

## Work Split Suggestion

| Teammate | Phases |
|---|---|
| **Tanner** | Phase 4 (Student WAR) + Phase 5 (Peer Eval) |
| **Teammate 2** | Phase 6 (Instructor Reports) + Phase 7 (Testing) |
| **Teammate 3** | Phase 1 frontend + Phase 2 frontend + Phase 3 frontend + Phase 8 (Deployment) |

All phases have independent backend + frontend work. No blocking dependencies
between teammates after Phase 0 is merged.

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
