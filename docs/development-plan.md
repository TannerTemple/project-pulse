# Development Plan

## Status: ~95% Complete

See [STATUS.md](../STATUS.md) for the full progress snapshot.

---

## Phase 1 – Foundation ✅

- Domain model (all JPA entities + repositories)
- JWT auth (login, invitation-based registration)
- Spring Security (stateless, CORS, method security)
- Global exception handler
- Email service (async Gmail SMTP)
- CI/CD (GitHub Actions → Azure)
- H2 dev profile with default admin seeded on startup
- Frontend shell (login, register, dashboard, nav drawer)

---

## Phase 2 – Admin APIs ✅

- Rubrics: create, list, view
- Sections: create, edit, list, view; set up active weeks
- Teams: create, edit, delete, list; assign/remove students and instructors
- Users: invite students + instructors via email, find, view, delete, deactivate/reactivate

---

## Phase 3 – Student APIs ✅

- View and update own account (UC-26)
- Manage WAR activities for a week (UC-27)
- Submit peer evaluation (UC-28)
- View own peer evaluation report (UC-29)

---

## Phase 4 – Instructor / Report APIs ✅

- Section-wide peer evaluation report (UC-31)
- Team WAR report (UC-32)
- Student peer evaluation history (UC-33)
- Student WAR history (UC-34)

---

## Phase 5 – Frontend ✅

All 20 views implemented and routed:

Admin: Dashboard, Sections, Teams, Students, Instructors, Rubrics, ActiveWeekSetup, StudentDetail, InstructorDetail

Student: AccountSettings, WARActivity, PeerEvaluation, MyReport

Instructor/Reports: SectionPeerReport, TeamWARReport

Auth: Login, Register

---

## Phase 6 – Testing ✅

119 tests across 16 test classes — all passing.

---

## Phase 7 – Database & Deployment (In Progress)

- [ ] Provision Azure MySQL Flexible Server
- [ ] Add `application-prod.properties` with MySQL datasource config
- [ ] Add MySQL JDBC driver to `pom.xml`
- [ ] Provision Azure App Service (Java 21)
- [ ] Set GitHub secrets and App Service environment variables
- [ ] Update SecurityConfig CORS for live URL
- [ ] Trigger CD pipeline and verify production URL

---

## Team Ownership

| Member | Area |
|--------|------|
| Tanner | Frontend, backend APIs, tests |
| Partner 1 | MySQL / database setup |
| Partner 2 | Azure App Service deployment |
