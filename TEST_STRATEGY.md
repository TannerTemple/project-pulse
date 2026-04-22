# Test Strategy — Project Pulse

> Every use case implementation must include tests before the branch is merged.
> No exceptions. Untested code is unfinished code.

---

## Tools

| Tool | Purpose |
|---|---|
| JUnit 5 (`@Test`, `@ExtendWith`) | All backend tests |
| Mockito (`@Mock`, `@InjectMocks`) | Unit test isolation |
| `@MockitoBean` | Spring context mock injection (**Spring Boot 4.x — NOT `@MockBean`**) |
| `@SpringBootTest` | Full integration tests |
| `@WebMvcTest` | Controller-layer slice tests |
| `@AutoConfigureMockMvc` | MockMvc in full Spring context |
| `MockMvc` | HTTP request simulation |
| H2 in-memory | Test database (already configured in `src/test/resources/application.properties`) |
| AssertJ (`assertThat`) | Fluent assertions (preferred over JUnit `assertEquals`) |

---

## Test Types

### 1. Unit Tests — Service Layer

**When:** For every service method with business logic.

**Setup:**
```java
@ExtendWith(MockitoExtension.class)
class SectionServiceTest {

    @Mock
    private SectionRepository sectionRepository;

    @Mock
    private RubricRepository rubricRepository;

    @InjectMocks
    private SectionService sectionService;
}
```

**Rules:**
- Mock all dependencies with `@Mock`.
- Test one behaviour per test method.
- Use `given(...).willReturn(...)` (BDDMockito style).
- Verify exception types with `assertThatThrownBy(() -> ...)`.
- Do NOT start the Spring context — pure unit tests are fast.

**Example:**
```java
@Test
void findById_givenValidId_returnsSection() {
    // given
    Section section = new Section();
    section.setId(1L);
    section.setName("2024-2025");
    given(sectionRepository.findById(1L)).willReturn(Optional.of(section));

    // when
    SectionResponse result = sectionService.findById(1L);

    // then
    assertThat(result.name()).isEqualTo("2024-2025");
}

@Test
void findById_givenInvalidId_throwsObjectNotFoundException() {
    given(sectionRepository.findById(99L)).willReturn(Optional.empty());

    assertThatThrownBy(() -> sectionService.findById(99L))
        .isInstanceOf(ObjectNotFoundException.class)
        .hasMessageContaining("99");
}
```

---

### 2. Controller Tests — Web Layer Slice

**When:** For every controller endpoint to verify HTTP status codes, request
mapping, JSON serialization, and security (`@PreAuthorize`).

**Setup:**
```java
@WebMvcTest(SectionController.class)
@Import(SecurityConfig.class)
class SectionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean                    // Spring Boot 4.x — NOT @MockBean
    private SectionService sectionService;

    @Autowired
    private ObjectMapper objectMapper;
}
```

**Rules:**
- Use `@WebMvcTest` — loads only the web layer, not the full context.
- Mock the service with `@MockitoBean`.
- Test HTTP status, response body shape, and error responses.
- Test both authenticated and unauthenticated requests where relevant.

**Example:**
```java
@Test
@WithMockUser(roles = "ADMIN")
void findAll_asAdmin_returns200() throws Exception {
    given(sectionService.findAll(null)).willReturn(List.of());

    mockMvc.perform(get("/api/sections"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON));
}

@Test
void findAll_unauthenticated_returns401() throws Exception {
    mockMvc.perform(get("/api/sections"))
        .andExpect(status().isUnauthorized());
}

@Test
@WithMockUser(roles = "STUDENT")
void create_asStudent_returns403() throws Exception {
    mockMvc.perform(post("/api/sections")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
        .andExpect(status().isForbidden());
}
```

---

### 3. Integration Tests — Full Stack

**When:** For the most critical flows end-to-end (login, register, core CRUD).
Run against H2 in-memory — no mocking.

**Setup:**
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional                      // rolls back after each test
class SectionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SectionRepository sectionRepository;
}
```

**Rules:**
- Use `@Transactional` so each test starts with a clean database.
- Seed required data in `@BeforeEach` via repository directly.
- Test the full HTTP → Service → Database → HTTP response cycle.
- Keep integration tests focused — not every endpoint needs one.

---

## Test Coverage Targets

| Layer | Target |
|---|---|
| Service (unit) | 80%+ line coverage |
| Controller (web slice) | Every endpoint has ≥1 happy-path + ≥1 error test |
| Integration | Auth flow + at least one full CRUD cycle per major feature |

---

## File Location

```
backend/src/test/java/edu/tcu/cs/projectpulse/
├── section/
│   ├── SectionServiceTest.java          ← unit test
│   └── SectionControllerTest.java       ← web slice test
├── team/
│   ├── TeamServiceTest.java
│   └── TeamControllerTest.java
├── auth/
│   └── AuthIntegrationTest.java         ← integration test
└── ...
```

Mirror the main source structure exactly.

---

## Running Tests

```bash
# All tests
cd backend && ./mvnw test

# Single class
cd backend && ./mvnw test -Dtest=SectionServiceTest

# Skip tests during build (use sparingly)
cd backend && ./mvnw package -DskipTests
```

---

## What to Test Per Use Case

When implementing any UC, write these tests at minimum:

| UC type | Required tests |
|---|---|
| Any service method | Unit: happy path + all exception paths |
| Any controller endpoint | Web slice: 200/201 happy path, 400 bad input, 401 unauth, 403 wrong role, 404 not found |
| Auth flows (login, register) | Integration test |
| Business rule enforcement (BR-1 through BR-5) | Unit test in the service that enforces it |
