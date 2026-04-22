# Coding Standards

## Backend (Java / Spring Boot)

### Naming

| Element | Convention | Example |
|---------|-----------|---------|
| Class | PascalCase | `SectionService` |
| Method | camelCase | `findById` |
| Variable | camelCase | `sectionId` |
| Package | lowercase | `edu.tcu.cs.projectpulse.section` |
| Constant | UPPER_SNAKE | `MAX_SCORE` |

### Entities

- Use `@Getter @Setter @NoArgsConstructor` from Lombok
- **Never** use `@Data` on entities (breaks Hibernate lazy loading)
- Use `@ToString(exclude = "...")` to avoid circular references on bidirectional relationships

### Services

- All business logic lives in the service layer
- Annotate with `@Transactional`
- Use `@PreAuthorize` for role-based access control
- Throw typed exceptions only:
  - `ObjectNotFoundException` → 404
  - `IllegalArgumentException` → 400
  - `IllegalStateException` → 409

### Controllers

- Thin — call service, return `ResponseEntity`, nothing else
- Never write business logic in a controller
- Use `@Valid` on `@RequestBody` parameters

### DTOs

- Use Java `record` types
- `*Request` for input, `*Response` for output
- Never return JPA entities from controllers

---

## Frontend (Vue 3 / TypeScript)

### Naming

| Element | Convention |
|---------|-----------|
| Component file | PascalCase (`SectionListView.vue`) |
| Variables / functions | camelCase |
| Props | camelCase |

### Structure

- Route-level pages → `src/views/`
- Reusable pieces → `src/components/` (only if used in 2+ views)
- All API calls → `src/api/index.ts`
- All auth state → `src/stores/auth.ts`

### Vuetify 4

- Use Vuetify components exclusively — no other UI libraries
- Form fields: `variant="outlined"` and `density="comfortable"`
- Cards: `rounded="lg"`
- Every form must: use `v-form` with ref, call `.validate()` before submit, show `v-alert type="error"` for API errors, show `v-btn :loading="loading"` during submission

### TypeScript

- Prefer explicit types over `any`
- Use the typed API helpers from `src/api/index.ts`
