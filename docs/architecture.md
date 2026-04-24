# Architecture

## Overview

Project Pulse uses a domain-oriented modular architecture.

The backend is organized by business domain first. Each domain contains its own internal layers. This improves maintainability, reduces coupling, and allows parallel development across team members.

---

## Backend Structure

Each domain module lives under `edu.tcu.cs.projectpulse.<domain>` and contains:

- **Entity** — JPA entity (domain model)
- **Repository** — Spring Data JPA interface (data access only)
- **Service** — business logic, `@Transactional`, `@PreAuthorize`
- **Controller** — thin HTTP handler, delegates to service
- **dto/** — `Request` and `Response` records (never expose entities directly)

```
backend/src/main/java/edu/tcu/cs/projectpulse/
├── auth/             # JWT, SecurityConfig, AuthController
├── common/           # GlobalExceptionHandler, ObjectNotFoundException
├── section/          # Section domain
├── team/             # Team domain
├── user/             # AppUser, UserService, UserController
├── activeweek/       # ActiveWeek domain
├── rubric/           # Rubric + Criterion domain
├── war/              # WARActivity domain
├── peerevaluation/   # PeerEvaluation + EvaluationScore domain
├── report/           # Report aggregation service + controller
├── invitation/       # InvitationToken domain
├── email/            # EmailService (async Gmail SMTP)
└── DataSeeder.java   # Seeds default admin on startup
```

---

## Layer Responsibilities

### Controller
- Handles HTTP requests and responses
- Validates request structure (via `@Valid`)
- Calls service layer
- Returns `ResponseEntity`
- No business logic

### Service
- Implements use cases
- Contains all business logic
- Role-based access control via `@PreAuthorize`
- `@Transactional` for data mutations

### Repository
- Spring Data JPA interface
- Query methods only
- No business logic

### Entity
- JPA entity (`@Entity`)
- Lombok `@Getter @Setter @NoArgsConstructor` (never `@Data`)
- Domain-specific fields and relationships

### DTO
- Java `record` types
- `Request` for incoming data, `Response` for outgoing data
- Never used as persistence entities

---

## Frontend Structure

```
frontend/src/
├── api/index.ts        # Fetch wrapper, Bearer token injection, typed helpers
├── stores/auth.ts      # Pinia auth store (JWT + user state)
├── router/index.ts     # Vue Router with auth guard and role checks
├── views/              # One .vue file per route/use case
└── components/         # Reusable components only (used in 2+ views)
```

---

## API Flow

```
Client → Controller → Service → Repository → Database
```

---

## Key Principles

- Organize by domain first, then layer
- Keep business logic in services
- Keep persistence isolated in repositories
- Avoid tight coupling between modules
- DTOs for all API boundaries — never expose entities
