# Naming Conventions — Project Pulse

> Apply these rules consistently across all files. Inconsistent naming is the
> fastest way to confuse teammates and break AI-assisted development.

---

## Backend (Java)

### Packages
- All lowercase, feature-based, singular noun.
- Pattern: `edu.tcu.cs.projectpulse.<feature>`

```
edu.tcu.cs.projectpulse.section        ✅
edu.tcu.cs.projectpulse.peerevaluation ✅
edu.tcu.cs.projectpulse.services       ❌ (layer-based)
edu.tcu.cs.projectpulse.SectionStuff   ❌ (PascalCase)
```

### Classes

| Type | Pattern | Example |
|---|---|---|
| Entity | `PascalCase`, singular noun | `Section`, `PeerEvaluation` |
| Repository | `<Entity>Repository` | `SectionRepository` |
| Service | `<Entity>Service` | `SectionService` |
| Controller | `<Entity>Controller` | `SectionController` |
| Request DTO | `<Entity>Request` | `SectionRequest` |
| Response DTO | `<Entity>Response` | `SectionResponse` |
| Shared DTO | `<Entity>Dto` | `CriterionDto` |
| Exception | descriptive + `Exception` | `ObjectNotFoundException` |
| Test (unit) | `<ClassUnderTest>Test` | `SectionServiceTest` |
| Test (integration) | `<ClassUnderTest>IntegrationTest` | `SectionControllerIntegrationTest` |

### Methods

| Type | Pattern | Example |
|---|---|---|
| Service CRUD | `findAll`, `findById`, `create`, `update`, `delete` | `sectionService.findById(1L)` |
| Repository queries | Spring Data derived names | `findBySectionId(Long)` |
| Boolean checks | `is` / `has` / `exists` prefix | `existsByName(String)` |

### Variables & Fields
- `camelCase` everywhere.
- No abbreviations except well-known ones (`id`, `dto`, `url`).
- Boolean fields: `active`, `submitted`, `registrationComplete` (not `isActive` in fields — Lombok generates `isActive()` getter).

### Constants
- `UPPER_SNAKE_CASE` in `static final` fields.

### REST Endpoints
- All lowercase, plural nouns, kebab-case for multi-word.
- Pattern: `/api/<resource>` and `/api/<resource>/{id}`

```
GET    /api/sections              → list
GET    /api/sections/{id}         → single
POST   /api/sections              → create
PUT    /api/sections/{id}         → full update
PATCH  /api/sections/{id}         → partial update
DELETE /api/sections/{id}         → delete

POST   /api/teams/{id}/students   → sub-resource action
```

### Test method names
Pattern: `methodName_givenCondition_thenExpectedResult`

```java
findById_givenValidId_returnsSection()
findById_givenInvalidId_throwsObjectNotFoundException()
create_givenDuplicateName_throwsIllegalStateException()
```

---

## Frontend (TypeScript / Vue)

### Files

| Type | Pattern | Example |
|---|---|---|
| View (route-level) | `PascalCase` + `View.vue` | `LoginView.vue`, `SectionListView.vue` |
| Component (reusable) | `PascalCase` + `.vue` | `ConfirmDialog.vue`, `UserAvatar.vue` |
| Store | `use<Feature>Store.ts` | `useAuthStore.ts`, `useSectionStore.ts` |
| API module | `<feature>.ts` in `src/api/` | `sections.ts`, `teams.ts` |
| Router | `src/router/index.ts` | (single file) |
| Types/interfaces | `<Feature>.types.ts` or inline in store | `Section.types.ts` |

### Variables & Functions
- `camelCase` for variables, functions, and composable functions.
- `PascalCase` for TypeScript interfaces and type aliases.
- `UPPER_SNAKE_CASE` for module-level constants.

```typescript
const sectionList = ref<Section[]>([])          // ✅
const SectionList = ref<Section[]>([])           // ❌

interface SectionResponse { ... }                // ✅
interface sectionResponse { ... }                // ❌
```

### Vue component conventions
- Use `<script lang="ts" setup>` (Composition API with `<script setup>`).
- Props: `defineProps<{ ... }>()` with TypeScript types.
- Emits: `defineEmits<{ ... }>()` with TypeScript types.
- No Options API. No `this`.

### Route paths
- Lowercase, kebab-case.

```
/login                  ✅
/sections               ✅
/sections/:id/edit      ✅
/ManageSections         ❌
```

### Pinia store structure
All stores use the **setup store** style (not options store):

```typescript
export const useFeatureStore = defineStore('feature', () => {
  // state: ref()
  // getters: computed()
  // actions: async function()
  return { ... }
})
```

---

## Git

### Branch names
```
feature/uc-27-war-activities
feature/uc-28-peer-evaluation
bugfix/login-cors
chore/update-dependencies
```

### Commit messages
```
feat(UC-27): student manages WAR activities
fix(UC-28): enforce BR-3 no-edit-after-submit
test(UC-27): unit and integration tests for WARActivityService
chore: update STATUS.md and DEVELOPMENT_PLAN.md
```
