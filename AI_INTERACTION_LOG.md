# AI Interaction Log — Project Pulse

> These are real exchanges between Tanner Temple and Claude (Anthropic) during
> development of Project Pulse. Each example shows a substantive engineering
> problem, the AI's diagnosis, and the resulting fix.

---

## Example 1 — Building the Entire Backend from Requirements Documents

### Context
The project started with only a scaffolded Spring Boot + Vue 3 shell and a set of
requirements documents (`requirements/use-cases.md`, `requirements/glossary.md`,
`requirements/vision-and-scope.md`). The goal was to have Claude read those documents
and implement every admin use case (UC-1 through UC-24) in a single focused session,
following a strict architecture.

### User Prompt (paraphrased from session)
> "Read the requirements documents in the requirements/ folder. Build the full backend
> for the admin use cases — rubrics, sections, active weeks, teams, and user management.
> Follow the feature-package structure (no flat service/controller/repository layers).
> Every feature needs an Entity, Repository, Service, Controller, and DTOs. Use
> @PreAuthorize for role guards. Throw typed exceptions that map to 400/404/409."

### What Claude Did
Claude read all three requirements documents, cross-referenced UC numbers with
business rules, and produced the following in one session:

**Architecture enforced across every feature:**
- Feature-package layout: `rubric/`, `section/`, `activeweek/`, `team/`, `user/`
- Every feature: `Entity → Repository → Service → Controller → DTOs (Request + Response records)`
- `@PreAuthorize("hasRole('ADMIN')")` on all admin service methods
- `ObjectNotFoundException` → 404, `IllegalArgumentException` → 400, `IllegalStateException` → 409
- `GlobalExceptionHandler` mapping all typed exceptions to structured `ApiError` responses

**Endpoints produced (34 files, 2,217 lines added in one commit):**
- `POST /api/rubrics` — create rubric with criteria (UC-1)
- `GET/POST /api/sections`, `PUT /api/sections/{id}` — section management (UC-2–5)
- `POST /api/sections/{id}/weeks` — active week setup (UC-6)
- `GET/POST/PUT/DELETE /api/teams/{id}` + assign/remove students and instructors (UC-7–14)
- `GET/DELETE /api/students/{id}`, invite via email token (UC-15–17)
- `GET /api/instructors`, deactivate/reactivate, invite (UC-18–24)
- Async email service (Gmail SMTP) for invitation, assignment, and removal notifications
- CI/CD workflows (GitHub Actions → Azure App Service)

### What This Demonstrates
- Using AI to translate requirement documents directly into working, structured code
- AI enforcing architectural rules (feature packages, no flat layers) consistently
  across 5 separate domain features without drift
- A single well-structured prompt producing 34 files and a fully functional backend
  rather than generating code piecemeal with no coherence

---

## Example 2 — Creating AI Guidance Documents That Govern Every Future Session

### Context
After the backend was built, the project needed a way to ensure that every future AI
session (and every team member using AI) would follow the same architecture, naming
conventions, testing strategy, and workflow — without having to re-explain everything
from scratch each time.

### User Prompt (paraphrased from session)
> "Create a set of markdown files that Claude will read at the start of every session.
> One should be a master guide covering architecture rules, what already exists, how
> to implement a use case step by step, commit format, branch strategy, and business
> rules to enforce in code. Also create a development plan checklist, a testing
> strategy, and a naming conventions reference."

### What Claude Produced
Four documents totalling 879 lines, committed on April 15 2026:

**`CLAUDE.md` — Master AI development guide (read before every response)**
- Required tech stack table (Spring Boot 4.x, Java 21, Vue 3, Vuetify 4 — no exceptions)
- Repository structure diagram
- Backend architecture rules: feature-package layout, Entity→Repository→Service→Controller→DTO chain, security rules, error handling, database profiles
- Frontend architecture rules: Vuetify-only UI, all API calls through `src/api/index.ts`, auth state only in Pinia store
- Step-by-step "How to Implement a Use Case" checklist (6 steps in order)
- Commit message format with examples
- Branch strategy table
- "What Already Exists — Do Not Recreate" table (prevents AI from re-implementing done work)
- Key business rules table mapping each BR to the exact service method that enforces it

**`DEVELOPMENT_PLAN.md` — Phase-by-phase UC checklist**
- Phases 0–8 with every use case broken into backend and frontend checkboxes
- Work split recommendations for 3 teammates
- How-to-start-a-new-UC instructions with exact git commands

**`TEST_STRATEGY.md` — Testing rules**
- When to write unit tests vs. `@WebMvcTest` controller slice tests vs. `@SpringBootTest`
- Coverage targets per feature
- Note that Spring Boot 4.x uses `@MockitoBean` not `@MockBean`

**`NAMING_CONVENTIONS.md` — Naming rules**
- Package, class, method, variable, REST endpoint, Vue file, branch, and commit naming
- Both backend (Java) and frontend (TypeScript/Vue) covered

### Why This Matters
These documents meant that every subsequent AI session — even after the context
window reset — produced code consistent with the original architecture. Claude read
`CLAUDE.md` before touching anything, which is why 125 tests across 17 test classes
all follow the same patterns, all 20 Vue views use Vuetify consistently, and no
feature ever broke the layer separation rules.

### What This Demonstrates
- Using AI not just to write code, but to create the governance structure that makes
  all future AI-assisted code trustworthy and consistent
- A single prompt producing self-referential documentation that amplifies the value
  of every subsequent AI interaction in the project

---

## Example 3 — Diagnosing a Non-Obvious 500 Error on "Generate Weeks"

### Context
The admin's "Generate Weeks" button on the Active Week Setup page was throwing a 500
error with the message "An unexpected error occurred." The feature had been coded
correctly — the issue was environmental.

### User Prompt
> "I click on generate reports and I get error [Active Week Setup 500 error shown]"

### AI Diagnosis
The root cause was a Spring Boot configuration setting:

```properties
spring.jpa.open-in-view=false
```

With this set, the Hibernate session closes **before** Jackson serializes the HTTP
response. The `ActiveWeekController` was returning raw `List<ActiveWeek>` entities,
and `ActiveWeek` had a lazy `@ManyToOne` relationship to `Section`:

```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "section_id", nullable = false)
private Section section;
```

When Jackson tried to serialize this field after the session closed, Hibernate threw
a `LazyInitializationException`. Since this wasn't a typed exception in the
`GlobalExceptionHandler`, it fell through to the generic handler:

```java
@ExceptionHandler(Exception.class)
public ResponseEntity<ApiError> handleGeneric(Exception ex) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiError.of(500, "Internal Server Error", "An unexpected error occurred."));
}
```

### Fix Applied
Added `@JsonIgnore` to the `section` field in `ActiveWeek.java` so Jackson skips
it during serialization:

```java
@JsonIgnore
@NotNull
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "section_id", nullable = false)
private Section section;
```

### What This Demonstrates
- Tracing a runtime error through multiple layers (HTTP → serialization → ORM session lifecycle)
- Understanding how `open-in-view=false` changes when the Hibernate session is available
- Identifying that the fix belongs on the entity, not the controller or service

---

## Example 4 — Challenging AI Output: Verifying a Business Rule

### Context
During implementation of `TeamService.removeInstructor()`, Claude enforced BR-1:
"Every team must have at least one instructor at all times." The user pushed back,
saying this rule wasn't in the requirements.

### User Prompt
> "I looked through the use cases, nowhere does it say at least one instructor at all times"

### AI Response
Rather than backing down or agreeing without verification, the AI checked the
requirements file directly:

```
requirements/use-cases.md line 905:
"Every senior design team must be assigned at least one instructor."
```

It also found that UC-19 (Assign Instructor to Team) and UC-20 (Remove Instructor
from Team) both explicitly reference BR-1 as a constraint.

### User Response
> "ok that makes sense then"

### Fix Confirmed
The guard in `TeamService.removeInstructor()` was kept:

```java
if (team.getInstructors().size() <= 1) {
    throw new IllegalStateException(
        "Cannot remove the last instructor from a team. Assign another instructor first.");
}
```

### What This Demonstrates
- AI should verify claims against source material, not just agree with the user
- Business rules can be scattered across requirement documents — cross-referencing matters
- The developer challenged the AI and the AI responded with evidence, not capitulation

---

## Example 5 — Full Backend Fix Cycle: Rubric Edit 500 with Unit Tests

### Context
The admin could create rubrics but had no way to edit them after creation. When a
`PUT /rubrics/{id}` endpoint was added and the user tried editing a rubric that was
already assigned to a section, they got another 500 "unexpected error occurred."

### User Prompt
> "There is no way to change a rubric or edit it after it is made. any way to change
> this to be able to edit?"
>
> *(After the edit endpoint was added)*
> "on admin, there is an error when you try to edit the rubric for a class that is
> used. maybe this is okay, but it says an unexpected error occurred."

### AI Diagnosis
The `RubricService.update()` method called `rubric.getCriteria().clear()` on a
collection annotated with `orphanRemoval = true`:

```java
@OneToMany(mappedBy = "rubric", cascade = CascadeType.ALL, orphanRemoval = true)
private List<Criterion> criteria = new ArrayList<>();
```

This cascaded a DELETE on all `Criterion` rows for that rubric. However,
`EvaluationScore` has a FK on `criterion_id`:

```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "criterion_id", nullable = false)
private Criterion criterion;
```

Deleting criteria that evaluation scores still reference violates the FK constraint
→ DB throws a constraint violation → caught by the generic handler → "unexpected
error occurred."

### Fix Applied

**1. Added derived query to `SectionRepository`:**
```java
boolean existsByRubricId(Long rubricId);
```

**2. Added guard to `RubricService.update()`:**
```java
@Transactional
public RubricResponse update(Long id, RubricRequest request) {
    Rubric rubric = getRubricOrThrow(id);
    if (sectionRepository.existsByRubricId(id)) {
        throw new IllegalStateException(
            "This rubric is assigned to one or more sections and cannot be modified. " +
            "Create a new rubric instead.");
    }
    // ... rest of update logic
}
```

This maps to a 409 via the `GlobalExceptionHandler`, and the message surfaces
directly in the frontend error alert.

**3. Two new unit tests added to `RubricServiceTest`:**

```java
@Test
void update_givenRubricAssignedToSection_throwsIllegalStateException() {
    given(rubricRepository.findById(1L)).willReturn(Optional.of(rubricWithId(1L, "Rubric A")));
    given(sectionRepository.existsByRubricId(1L)).willReturn(true);

    RubricRequest req = new RubricRequest("Rubric A Updated", List.of(
            new CriterionDto(null, "Quality", null, new BigDecimal("10"), 0)
    ));

    assertThatThrownBy(() -> rubricService.update(1L, req))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("assigned to one or more sections");
    then(rubricRepository).should(never()).save(any());
}

@Test
void update_givenUnassignedRubric_savesAndReturns() {
    Rubric existing = rubricWithId(1L, "Rubric A");
    given(rubricRepository.findById(1L)).willReturn(Optional.of(existing));
    given(sectionRepository.existsByRubricId(1L)).willReturn(false);
    given(rubricRepository.existsByName("Rubric A Updated")).willReturn(false);
    given(rubricRepository.save(any(Rubric.class))).willAnswer(inv -> inv.getArgument(0));

    RubricResponse result = rubricService.update(1L, req);

    assertThat(result.name()).isEqualTo("Rubric A Updated");
    then(rubricRepository).should().save(any(Rubric.class));
}
```

Test suite: **125 tests, 0 failures.**

### What This Demonstrates
- Tracing a bug through entity relationships and JPA cascade behavior
- Knowing when to block an operation vs. attempt to make it work
- Writing unit tests that verify both the failure path and the success path
- Full cycle: user reports bug → diagnosis → backend fix → repository query → tests

---

## Example 6 — Role-Based Data Scoping: Instructor Sees Only Their Data

### Context
Instructors could see all teams in the WAR report dropdown and all sections in the
peer evaluation report dropdown — not just the ones they were assigned to. The
team list view had already been fixed in a prior session using the same pattern,
but the two report views were missed.

### User Prompt
> "under war reports, i see everyone's teams not just the ones the instructor is
> in charge of. Finally, i see more than just the section that that instructor is
> in charge of which is wrong."

### AI Diagnosis
Both report views fetched all data with no role filtering:

**`TeamWARReportView.vue`** (bug):
```typescript
onMounted(async () => {
  teams.value = await api.get<Team[]>('/teams')  // returns ALL teams
})
```

**`SectionPeerReportView.vue`** (bug):
```typescript
onMounted(async () => {
  sections.value = await api.get<Section[]>('/sections')  // returns ALL sections
})
```

There was no backend endpoint to fetch "my sections" directly, but the instructor's
teams already contained `sectionId` — so the section list could be derived
client-side without a new API endpoint.

### Fix Applied

**`TeamWARReportView.vue`** — same pattern as `TeamListView`:
```typescript
onMounted(async () => {
  let myUserId: number | null = null
  if (auth.role === 'INSTRUCTOR') {
    const me = await api.get<any>('/users/me').catch(() => null)
    if (me) myUserId = me.id
  }
  const all = await api.get<Team[]>('/teams')
  teams.value = myUserId
    ? all.filter(t => t.instructors?.some((i: any) => i.id === myUserId))
    : all
})
```

**`SectionPeerReportView.vue`** — derive sections from instructor's teams:
```typescript
onMounted(async () => {
  if (auth.role === 'INSTRUCTOR') {
    const me = await api.get<any>('/users/me').catch(() => null)
    const myId = me?.id ?? null
    const allTeams = await api.get<any[]>('/teams')
    const myTeams = myId
      ? allTeams.filter(t => t.instructors?.some((i: any) => i.id === myId))
      : []
    const sectionIds = new Set(myTeams.map((t: any) => t.sectionId))
    const allSections = await api.get<Section[]>('/sections')
    sections.value = allSections.filter(s => sectionIds.has(s.id))
  } else {
    sections.value = await api.get<Section[]>('/sections')
  }
})
```

No new backend endpoints were needed — the existing `/users/me` and `/teams`
responses contained all the data required to derive the correct scoped list.

### What This Demonstrates
- Identifying a pattern of missing role-based filtering applied consistently across views
- Solving a scoping problem without adding new backend endpoints by reusing existing API data
- Understanding that `/users/me` + client-side filtering is a valid approach when the
  backend already returns the necessary fields (`instructors` array on each team)

---

> \* Examples 7–10 below are reconstructed from git commit history and code output.
> The exact prompts were given in earlier sessions outside the current context window.

---

## Example 7 * — Designing the Full JPA Domain Model

### Context
Before any REST endpoints could be written, the full data model needed to be designed
and implemented as JPA entities. This required translating the requirements documents
into entity relationships, deciding on cascade rules, and setting up enums.

### User Prompt (reconstructed)
> "Read the requirements and create the full JPA domain model for the project.
> Every entity needs proper relationships, cascade settings, and Lombok annotations.
> No @Data on entities — use @Getter @Setter @NoArgsConstructor to avoid breaking
> Hibernate lazy-loading."

### What Claude Produced
Nine entities with full relationship mapping in one commit:

| Entity | Key Relationships |
|---|---|
| `Section` | One-to-many `ActiveWeek`, many-to-one `Rubric` |
| `Team` | Many-to-one `Section`, many-to-many `AppUser` (students + instructors) |
| `AppUser` | Enum role (`ADMIN`, `INSTRUCTOR`, `STUDENT`), many-to-many `Team` |
| `ActiveWeek` | Many-to-one `Section`, lazy-loaded |
| `Rubric` | One-to-many `Criterion` (cascade ALL, orphanRemoval) |
| `Criterion` | Many-to-one `Rubric`, ordered by `orderIndex` |
| `WARActivity` | Many-to-one `AppUser` + `ActiveWeek`, enum category + status |
| `PeerEvaluation` | Many-to-one evaluator/evaluatee (`AppUser`) + `ActiveWeek` |
| `EvaluationScore` | Many-to-one `PeerEvaluation` + `Criterion`, unique constraint |

Cascade and fetch strategies were set intentionally:
- `Rubric.criteria`: `CascadeType.ALL + orphanRemoval = true` (criteria owned by rubric)
- `ActiveWeek.section`: `FetchType.LAZY` (later required `@JsonIgnore` when `open-in-view=false`)
- `PeerEvaluation` → `EvaluationScore`: unique constraint on `(peer_evaluation_id, criterion_id)`

### What This Demonstrates
- Translating business requirements into a normalized relational data model
- Making deliberate decisions about cascade types and fetch strategies
- Anticipating constraints (unique evals per criterion per evaluation) from the requirements

---

## Example 8 * — Implementing JWT Auth with Spring Security

### Context
The project required stateless JWT authentication with role-based access, token-based
email invitation registration, and CORS configured for both local dev and Azure.

### User Prompt (reconstructed)
> "Implement the full auth layer — JWT login, stateless Spring Security config,
> and token-based registration flow so invited users can set up their account via
> an email link. Use JJWT 0.12.x. No session state."

### What Claude Produced
A complete auth layer in one commit:

- **`JwtService`** — token generation, validation, and claims extraction using JJWT 0.12.x
- **`JwtAuthFilter`** — `OncePerRequestFilter` extracting Bearer token, loading user, setting `SecurityContextHolder`
- **`SecurityConfig`** — stateless sessions, CORS (localhost:3000 + Azure URL), public routes (`/api/auth/**`, `/api/invitations/**`), method security enabled via `@EnableMethodSecurity`
- **`AuthController`** — `POST /api/auth/login` and `POST /api/auth/register` (token-validated invitation flow)
- **`DataSeeder`** — seeds a default admin account on startup in the dev profile so the app is immediately usable

The invitation flow:
1. Admin invites user → backend generates a signed JWT invitation token → emailed as a link
2. User clicks link → `RegisterView.vue` reads token from URL → `POST /api/auth/register` with token + new password
3. Backend validates token, activates account, returns auth JWT

### What This Demonstrates
- Implementing a non-trivial stateless auth system end-to-end
- Using JWT for two separate purposes (auth sessions + invitation tokens) with different expiry
- Configuring Spring Security 6.x correctly for a stateless REST API

---

## Example 9 * — Implementing All Student and Instructor Features (UC-27–34)

### Context
After the admin backend was complete, all student-facing and instructor-facing use
cases needed to be implemented: WAR activity management, peer evaluation with 5
business rules, 4 report endpoints, and all 20 frontend Vue views.

### User Prompt (reconstructed)
> "Implement UC-27 through UC-34 — WAR activities, peer evaluation submission and
> report, instructor reports, and account management. Enforce all business rules.
> Then build all the frontend views for every role: admin, student, and instructor."

### What Claude Produced

**Backend (UC-27–34):**
- `WARActivityService/Controller` — CRUD for weekly activities, blocks future weeks
- `PeerEvaluationService/Controller` — submit evaluations with 5 enforced business rules:
  - BR-2: Week must be currently active
  - BR-3: Cannot edit after submission (immutable once submitted)
  - BR-4: Must be for the previous week, submitted within 1 week of it ending
  - BR-5: Evaluator identity and private comments never returned to students
  - Evaluator and evaluatee must be teammates (not strangers)
- `ReportService/Controller` — 4 report endpoints for section peer evals, team WAR, student peer history, student WAR history
- `PATCH /api/users/me` — students and instructors edit their own account

**Frontend (all 20 views):**
- Auth: `LoginView`, `RegisterView`
- Admin: `DashboardView`, `SectionListView`, `SectionFormView`, `ActiveWeekSetupView`, `TeamListView`, `TeamFormView`, `StudentListView`, `StudentDetailView`, `InstructorListView`, `InstructorDetailView`, `RubricListView`, `RubricFormView`
- Student: `AccountSettingsView`, `WARActivityView`, `PeerEvaluationView`, `MyReportView`
- Instructor: `SectionPeerReportView`, `TeamWARReportView`

### What This Demonstrates
- Implementing complex business rules in service-layer code with clear enforcement points
- Building an entire frontend (20 views, role-aware routing, Pinia auth store) from scratch
- Keeping frontend and backend in sync across a large feature set in one coordinated session

---

## Example 10 * — Writing 125 Unit and Integration Tests Systematically

### Context
After features were complete, the project needed comprehensive test coverage across
all 17 feature test classes. This required both unit tests (Mockito) and controller
slice tests (`@WebMvcTest`) following the project's `TEST_STRATEGY.md`.

### User Prompt (reconstructed)
> "Write unit tests and controller tests for every feature package. Follow
> TEST_STRATEGY.md. Use @MockitoBean not @MockBean (Spring Boot 4.x). Cover
> the happy path, not-found cases, validation errors, and business rule violations."

### What Claude Produced
125 tests across 17 test classes, all passing:

| Test Class | Count | What It Covers |
|---|---|---|
| `RubricServiceTest` | 7 | create, find, duplicate name, update guarded by section assignment |
| `RubricControllerTest` | 6 | all endpoints, 403 for non-admin |
| `SectionServiceTest` | 7 | CRUD, duplicate name, not-found |
| `SectionControllerTest` | 6 | all endpoints |
| `TeamServiceTest` | 12 | CRUD, assign/remove students and instructors, BR-1 enforcement |
| `TeamControllerTest` | 11 | all endpoints |
| `UserServiceTest` | 13 | invite, find, deactivate, reactivate, delete |
| `UserControllerTest` | 14 | all endpoints, role guards |
| `ActiveWeekServiceTest` | 5 | setup, inactive marking, regen, invalid section |
| `WARActivityServiceTest` | 6 | CRUD, future-week block |
| `WARActivityControllerTest` | 6 | all endpoints |
| `PeerEvaluationServiceTest` | 6 | submit, BR-2/3/4/5 enforcement |
| `PeerEvaluationControllerTest` | 4 | submit, report |
| `ReportServiceTest` | 15 | all 4 report methods, date range filtering, non-submitter logic |
| `ReportControllerTest` | 6 | all report endpoints |
| `ProjectpulseApplicationTests` | 1 | context loads smoke test |

### What This Demonstrates
- Writing tests that verify behavior, not just coverage (business rule violation paths tested explicitly)
- Adapting to framework-specific quirks (`@MockitoBean` in Spring Boot 4.x vs. `@MockBean` in 3.x)
- Systematic, consistent test structure across an entire codebase rather than scattered spot-checks
