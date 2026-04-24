# Team Workflow

## Branching

- `main` = stable, always deployable
- `feature/<short-name>` = one branch per use case or small cluster

---

## Pull Requests

Required for all changes into `main`. PR should include:

- Description of what was implemented
- Related use case(s)
- Evidence that it runs (screenshot or test output)

---

## Code Review

Check for:

- Requirements alignment (does it match the use case?)
- Architecture compliance (domain module structure, no logic in controllers)
- Code clarity and naming conventions

---

## Rules

- No direct push to `main`
- No major architectural change without team discussion
- Update `DEVELOPMENT_PLAN.md` when tasks change

---

## Commit Message Format

```
feat(UC-N): implement <use case name>
fix(UC-N): <what was broken>
test(UC-N): add unit/integration tests
chore: <non-feature work>
```
