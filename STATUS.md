# Project Pulse — Development Status

> Last updated: 2026-04-29
> Active branch: `bugfix/instructor-scoping-rubric-edit-guard` (PR open → main)

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
- BR-1: Every team must have ≥1 instructor before an instructor can be removed
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
All views exist and are wired to the router:

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
- `RubricListView.vue` + `RubricFormView.vue` — create **and edit** rubrics with ordered criteria (edit blocked with clear message if rubric is in use by a section)

**Student**
- `AccountSettingsView.vue` — update name and password
- `WARActivityView.vue` — week selector, CRUD activity cards, planned vs. actual hours summary
- `PeerEvaluationView.vue` — per-teammate cards, rubric criterion scoring, public + private comments
- `MyReportView.vue` — overall grade, per-criterion averages with progress bars, public feedback

**Instructor / Reports**
- `SectionPeerReportView.vue` — section dropdown scoped to instructor's sections only; section-wide peer eval grades by week
- `TeamWARReportView.vue` — team dropdown scoped to instructor's assigned teams only; WAR submissions by week
- `TeamListView.vue` — filtered client-side to only show the logged-in instructor's assigned teams

### Tests (125 tests — all passing)
Full coverage across all feature packages:
- `SectionServiceTest` (7) + `SectionControllerTest` (6)
- `WARActivityServiceTest` (6) + `WARActivityControllerTest` (6)
- `PeerEvaluationServiceTest` (6) + `PeerEvaluationControllerTest` (4)
- `RubricServiceTest` (7) + `RubricControllerTest` (6)
- `TeamServiceTest` (12) + `TeamControllerTest` (11)
- `UserServiceTest` (13) + `UserControllerTest` (14)
- `ActiveWeekServiceTest` (5)
- `ReportServiceTest` (15) + `ReportControllerTest` (6)
- `ProjectpulseApplicationTests` (1) — context loads smoke test

---

## What Still Needs to Be Done

Nothing — the project is complete and live on Azure. ✅

Merge open PRs:
- [ ] `bugfix/instructor-scoping-rubric-edit-guard` → main (this session's fixes)
- [ ] `chore/update-md-for-completion` → main (previous session's MD updates + .gitignore)

---

## Known Issues / Gaps
- Team edit mode can add members but bulk-removal has no UI — removal requires the individual-remove endpoints separately
- WAR categories/statuses are hardcoded strings in the frontend dropdowns; ideally served from backend
- Report endpoints return JSON — a print/export view is not yet built

---

## Bug Fix Log

### 2026-04-29
- **Rubric edit 500 on in-use rubrics**: `RubricService.update()` called `getCriteria().clear()` which cascaded a DELETE on `Criterion` rows still referenced by `EvaluationScore.criterion_id` (FK constraint). DB violation was caught only by the generic handler → "unexpected error occurred". Fixed: check `sectionRepository.existsByRubricId()` first; throw `IllegalStateException` → 409 with message "This rubric is assigned to one or more sections and cannot be modified. Create a new rubric instead."
- **WAR report shows all teams to instructor**: `TeamWARReportView` fetched all teams with no role filtering. Fixed: fetch `/users/me` on mount (instructor only), filter team list to teams where the instructor is assigned.
- **Peer report shows all sections to instructor**: `SectionPeerReportView` fetched all sections with no role filtering. Fixed: derive instructor's accessible sections by intersecting their assigned teams' `sectionId` values against the full section list.
- Added `SectionRepository.existsByRubricId()` derived query.
- Added 2 new `RubricServiceTest` cases covering the blocked and allowed update paths. (125 total)

### 2026-04-28
- **Generate Weeks 500**: `ActiveWeek.section` lazy field serialized by Jackson after Hibernate session closed (`open-in-view=false`) → `LazyInitializationException`. Fixed with `@JsonIgnore`.
- **Reports 500 on null names**: `Comparator.comparing(AppUser::getLastName)` threw NPE for users with null last names. Fixed with `Comparator.nullsFirst`.
- **Team website URL 500**: URLs without protocol routed through SPA → 500. Fixed by auto-prepending `https://` in `TeamFormView` before save.
- **Rubric editing**: No edit path existed post-creation. Added `PUT /rubrics/{id}` backend endpoint, dual-mode `RubricFormView`, edit button on rubric list cards, and `rubric-edit` route.
- **Instructor "My Teams"**: `TeamListView` showed all teams to instructors. Fixed by fetching `/users/me` and filtering client-side.
- **Private peer comments visible to students**: Restricted private comment visibility to instructors only.
- **Active week save collision + invite dialog reset**: Fixed save collision on active week setup and reset state of invite dialog after submission.
- **BR-1 enforcement**: `TeamService.removeInstructor` now blocks removal if it would leave a team with no instructors.
- **Email invite link**: Fixed broken invite link when a custom message was provided.
