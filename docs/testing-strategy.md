# Testing Strategy

## Backend

### Unit Tests (Service Layer)

- Framework: JUnit 5 + Mockito
- Use `@ExtendWith(MockitoExtension.class)`
- Mock all dependencies with `@Mock` / `@InjectMocks`
- Use BDD-style: `given(...).willReturn(...)`, `assertThat(...)`
- Test happy path + all error paths per service method

### Controller Tests (HTTP Layer)

- Use Spring MockMvc in **standalone** mode (no Spring context)
- `MockMvcBuilders.standaloneSetup(controller).setControllerAdvice(new GlobalExceptionHandler())`
- Test HTTP status codes and response body shape
- Mock the service layer — controller tests do not test business logic

### What We Do NOT Test

- Repository interfaces (Spring Data JPA handles this)
- DTO mapping (covered by service tests)
- Spring Security filter chain in unit tests

---

## Current Coverage

119 tests across 16 test classes — all passing:

| Test Class | Tests | What it covers |
|------------|-------|----------------|
| `SectionServiceTest` | 7 | Section CRUD business rules |
| `SectionControllerTest` | 6 | GET/POST /sections HTTP |
| `RubricServiceTest` | 5 | Rubric create + duplicate check |
| `RubricControllerTest` | 6 | GET/POST /rubrics HTTP |
| `TeamServiceTest` | 10 | Team CRUD, member assignment |
| `TeamControllerTest` | 11 | Full team HTTP surface |
| `UserServiceTest` | 13 | Student/instructor management |
| `UserControllerTest` | 14 | Full user HTTP surface |
| `ActiveWeekServiceTest` | 5 | Week generation, inactive marking |
| `WARActivityServiceTest` | 6 | WAR business rules |
| `WARActivityControllerTest` | 6 | WAR HTTP |
| `PeerEvaluationServiceTest` | 6 | Eval business rules (BR-2–5) |
| `PeerEvaluationControllerTest` | 4 | Eval HTTP |
| `ReportServiceTest` | 13 | Grade algorithm, date filtering |
| `ReportControllerTest` | 6 | Report HTTP |
| `ProjectpulseApplicationTests` | 1 | Context loads smoke test |

---

## Definition of Done

- Each new service method has ≥1 happy path test + ≥1 error path test
- Each new controller endpoint has ≥1 status code test
- All tests pass before merge to `main`
