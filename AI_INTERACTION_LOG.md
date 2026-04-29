# AI Interaction Log — Project Pulse

> These are real exchanges between Tanner Temple and Claude (Anthropic) during
> development of Project Pulse. Each example shows a substantive engineering
> problem, the AI's diagnosis, and the resulting fix.

---

## Example 1 — Diagnosing a Non-Obvious 500 Error on "Generate Weeks"

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

## Example 2 — Challenging AI Output: Verifying a Business Rule

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

## Example 3 — Full Backend Fix Cycle: Rubric Edit 500 with Unit Tests

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

## Example 4 — Role-Based Data Scoping: Instructor Sees Only Their Data

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
