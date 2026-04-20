# Project Pulse — Development Status

> Last updated: 2026-04-20
> Branch: `feature/domain-model`

---

## Overall Completion: ~65%

| Area | Status | % Done |
|---|---|---|
| Domain model (JPA entities) | Complete | 100% |
| Auth / JWT / Spring Security | Complete | 100% |
| Admin REST APIs | Complete | 100% |
| Student REST APIs (UC-26–29) | Complete | 100% |
| Instructor / Report APIs (UC-31–34) | Not started | 0% |
| Frontend — Auth views | Complete | 100% |
| Frontend — Admin views | Mostly complete | ~75% |
| Frontend — Student views | Complete | 100% |
| Frontend — Instructor views | Not started | 0% |
| Database (PostgreSQL) | Schema defined, not connected | ~10% |
| Deployment (Azure) | CI/CD written, Azure not configured | ~15% |
| Tests | Smoke test only | ~5% |

---

## What Has Been Done

### Phase 0 — Foundation (Complete)
- JPA domain model (all entities + repositories)
- JWT auth (login, register via invitation token)
- Spring Security config (stateless, CORS, method security)
- Global exception handler
- Email service (async Gmail SMTP)
- CI/CD workflows (GitHub Actions → Azure)
- Dev profile (H2 in-memory, default admin seeded)

### Phase 1–3 — Admin Backend APIs (Complete)
All admin endpoints for rubrics, sections, active weeks, teams, students, and instructors.
See `DEVELOPMENT_PLAN.md` for full endpoint list.

### Phase 4 — Student APIs (Complete)
- `GET /api/users/me` — view own profile
- `PATCH /api/users/me` — edit own account (UC-26)
- `GET /api/war-activities?weekId=` — list activities (UC-27)
- `POST /api/war-activities` — create activity (UC-27)
- `PUT /api/war-activities/{id}` — edit activity (UC-27)
- `DELETE /api/war-activities/{id}` — delete activity (UC-27)
- `POST /api/peer-evaluations` — submit evaluation (UC-28)
- `GET /api/peer-evaluations/me/report?weekId=` — own report (UC-29)

**Business rules enforced:**
- Cannot submit WAR for a future week
- Cannot submit eval for non-teammates
- Cannot edit a submitted peer evaluation (BR-3)
- Evaluations must be for the previous week, within 1-week deadline (BR-4)
- Evaluator identity and private comments never returned to student (BR-5)

### Frontend — Auth Views (Complete)
- `LoginView.vue` — email/password login
- `RegisterView.vue` — token-based registration

### Frontend — Admin Views (Mostly Complete)
- `DashboardView.vue` — role-based quick-action dashboard
- `SectionListView.vue` — search and list sections
- `SectionFormView.vue` — create/edit section
- `TeamListView.vue` — search and list teams with delete
- `TeamFormView.vue` — create/edit team with student/instructor assignment
- `StudentListView.vue` — search students, invite modal, delete
- `InstructorListView.vue` — search instructors, invite modal, deactivate/reactivate
- `RubricListView.vue` — list rubrics
- `RubricFormView.vue` — create rubric with criteria

**Still needed:** `ActiveWeekSetupView.vue`, student detail view, instructor detail view

### Frontend — Student Views (Complete)
- `AccountSettingsView.vue` — edit name and password
- `WARActivityView.vue` — week selector, activity CRUD with summary
- `PeerEvaluationView.vue` — per-teammate evaluation form with rubric scores
- `MyReportView.vue` — grade, criterion breakdown, public feedback

---

## What Still Needs to Be Done

### High Priority
- [ ] `ActiveWeekSetupView.vue` — admin configures active weeks per section
- [ ] Instructor/Report backend APIs (UC-31–34)
- [ ] Instructor frontend views (team WAR report, section peer eval report)
- [ ] Unit + integration tests (service layer + controllers)

### Lower Priority
- [ ] Student detail view
- [ ] Instructor detail view
- [ ] Azure provisioning + CD pipeline configuration
- [ ] PostgreSQL production setup

---

## Known Issues / Gaps
- **Team assignment in edit mode:** The TeamFormView assigns new members but does not remove existing ones. Removal must be done separately (individual delete endpoints exist).
- **WAR activity categories/statuses:** Currently hardcoded in the frontend dropdown; should be driven from backend enums.
- **Active weeks:** Students see weeks filtered by date client-side. A dedicated endpoint returning only non-future weeks would be cleaner.
- **Report format:** Use cases specify HTML output — endpoints return JSON. A print/PDF view layer is not yet implemented.
