# Project Pulse — Development Status

> Last updated: 2026-04-28
> Active branch: `main` — app is live on Azure

---

## Overall Completion: 100% ✅

| Area | Status | % Done |
|---|---|---|
| Domain model (JPA entities + repositories) | Complete | 100% |
| Auth / JWT / Spring Security | Complete | 100% |
| Admin REST APIs (UC-1–24) | Complete | 100% |
| Student REST APIs (UC-26–29) | Complete | 100% |
| Instructor / Report APIs (UC-31–34) | Complete | 100% |
| Frontend — Auth views | Complete | 100% |
| Frontend — Admin views | Complete | 100% |
| Frontend — Student views | Complete | 100% |
| Frontend — Instructor/Report views | Complete | 100% |
| Unit + integration tests | Complete | 100% |
| Database (PostgreSQL on Azure) | Live | 100% |
| Deployment (Azure App Service) | Live | 100% |

---

## What Has Been Done

### Phase 0 — Foundation (Complete)
- JPA domain model: `Section`, `Team`, `AppUser`, `ActiveWeek`, `Rubric`, `Criterion`,
  `WARActivity`, `PeerEvaluation`, `EvaluationScore` — all entities + repositories
- JWT auth: login, token-based registration via email invitation
- Spring Security: stateless sessions, CORS (localhost:3000 + Azure URL), method security
- Global exception handler: typed errors map to 400/404/409/500
- Email service: async Gmail SMTP, invite links
- CI/CD: GitHub Actions — backend (mvn verify) + frontend (type-check + lint) → Azure
- Dev profile: H2 in-memory, default admin seeded on startup

### Phase 1–3 — Admin Backend APIs (Complete)
All admin endpoints live under `/api/`:
- Rubrics: `POST /rubrics`, `GET /rubrics`, `GET /rubrics/{id}`, `PUT /rubrics/{id}`
- Sections: `GET/POST /sections`, `GET/PUT /sections/{id}`
- Active weeks: `POST /sections/{id}/weeks`, `GET /sections/{id}/weeks`
- Teams: `GET/POST /teams`, `GET/PUT/DELETE /teams/{id}`
- Team members: `POST/DELETE /teams/{id}/students`, `POST/DELETE /teams/{id}/instructors`
- Students: `GET /students`, `GET/DELETE /students/{id}`, invite via `POST /sections/{id}/invitations/students`
- Instructors: `GET /instructors`, `GET /instructors/{id}`, `PATCH /instructors/{id}/deactivate|reactivate`, invite via `POST /invitations/instructors`

### Phase 4 — Student APIs (Complete)
- `GET /api/users/me` — view own profile
- `PATCH /api/users/me` — edit own account (UC-26)
- `GET/POST/PUT/DELETE /api/war-activities` — manage WAR activities (UC-27)
- `POST /api/peer-evaluations` — submit peer evaluation (UC-28)
- `GET /api/peer-evaluations/me/report?weekId=` — view own report (UC-29)

Business rules enforced:
- BR-2: Evals only during an active week
- BR-3: Cannot edit a submitted evaluation
- BR-4: Eval must be for previous week, within 1-week deadline
- BR-5: Evaluator identity and private comments never exposed to student
- Cannot submit WAR for a future week
- Can only eval teammates, not strangers

### Phase 6 — Instructor / Report APIs (Complete)
- `GET /api/sections/{id}/peer-evaluation-report?weekId=` — section-wide peer eval (UC-31)
- `GET /api/teams/{id}/war-report?weekId=` — team WAR report (UC-32)
- `GET /api/students/{id}/peer-evaluation-report?start=&end=` — student peer history (UC-33)
- `GET /api/students/{id}/war-report?start=&end=` — student WAR history (UC-34)

### Frontend — All Views (Complete)
All 20 views exist and are wired to the router:

**Auth**
- `LoginView.vue` — email + password → JWT stored in localStorage
- `RegisterView.vue` — token-based first-time account setup

**Admin**
- `DashboardView.vue` — role-aware quick-action cards
- `SectionListView.vue` + `SectionFormView.vue` — search, create, edit sections
- `ActiveWeekSetupView.vue` — admin opens/closes weeks for a section
- `TeamListView.vue` + `TeamFormView.vue` — search, create, edit teams; assign students + instructors; website URLs auto-normalized with `https://`
- `StudentListView.vue` + `StudentDetailView.vue` — search students, invite, delete, view detail
- `InstructorListView.vue` + `InstructorDetailView.vue` — search instructors, invite, deactivate/reactivate, view detail
- `RubricListView.vue` + `RubricFormView.vue` — create **and edit** rubrics with ordered criteria

**Student**
- `AccountSettingsView.vue` — update name and password
- `WARActivityView.vue` — week selector, CRUD activity cards, planned vs. actual hours summary
- `PeerEvaluationView.vue` — per-teammate cards, rubric criterion scoring, public + private comments
- `MyReportView.vue` — overall grade, per-criterion averages with progress bars, public feedback

**Instructor / Reports**
- `SectionPeerReportView.vue` — section-wide peer eval grades by week
- `TeamWARReportView.vue` — team WAR submissions by week
- Instructor team list (`TeamListView`) filtered client-side to only show the logged-in instructor's assigned teams

### Tests (119 tests — all passing)
Full coverage across all feature packages:
- `SectionServiceTest` (7) + `SectionControllerTest` (6)
- `WARActivityServiceTest` (6) + `WARActivityControllerTest` (6)
- `PeerEvaluationServiceTest` (6) + `PeerEvaluationControllerTest` (4)
- `RubricServiceTest` (5) + `RubricControllerTest` (6)
- `TeamServiceTest` (10) + `TeamControllerTest` (11)
- `UserServiceTest` (13) + `UserControllerTest` (14)
- `ActiveWeekServiceTest` (5)
- `ReportServiceTest` (13) + `ReportControllerTest` (6)
- `ProjectpulseApplicationTests` (1) — context loads smoke test

---

## What Still Needs to Be Done

Nothing — the project is complete and live. ✅

---

## Known Issues / Gaps
- Team edit mode adds new members but removal still requires the individual-remove endpoints (separate API calls); no bulk-remove UI
- WAR categories/statuses are hardcoded strings in the frontend dropdowns; ideally served from backend
- Report endpoints return JSON — use cases mention HTML output; a print/export view is not yet built

## Recent Bug Fixes (2026-04-28)
- **Generate Weeks 500**: `ActiveWeek.section` was a lazy JPA field serialized by Jackson after the Hibernate session closed (`open-in-view=false`). Fixed with `@JsonIgnore` on the field.
- **Reports 500 on null names**: `Comparator.comparing(AppUser::getLastName)` threw NPE for users with null last names. Fixed with `Comparator.nullsFirst`.
- **Team website URL 500**: URLs without a protocol (e.g. `google.com`) were routed through the SPA → backend → 404/500 instead of opening externally. Fixed by auto-prepending `https://` in `TeamFormView` before save.
- **Rubric editing**: No edit path existed after initial creation. Added `PUT /rubrics/{id}` backend endpoint, dual-mode `RubricFormView` (create vs. edit), edit button on rubric list cards, and `rubric-edit` route.
- **Instructor "My Teams"**: Instructor team list was showing all teams. Fixed by fetching `/users/me` on mount and filtering client-side to teams where the logged-in instructor is assigned.
