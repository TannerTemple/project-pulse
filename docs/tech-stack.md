# Tech Stack

## Frontend

- Vue 3 + Vite + TypeScript
- Vuetify 4 (UI component library — no other UI libraries)
- Pinia (state management)
- Vue Router 5

## Backend

- Spring Boot 4.0 (Java 21)
- Spring Web (REST)
- Spring Data JPA + Hibernate
- Spring Security + JJWT 0.12.6 (stateless JWT auth)
- Spring Mail (async Gmail SMTP for invitations)

## Database

- **Development:** H2 in-memory (`ddl-auto=create-drop`, no setup required)
- **Production:** MySQL (Azure MySQL Flexible Server)
- Flyway recommended for production schema migrations

## DevOps

- GitHub Actions (CI/CD)
- Azure App Service (backend deployment)
- Azure MySQL Flexible Server (production database)

## Testing

- Backend: JUnit 5 + Mockito + Spring MockMvc (standalone)
- 119 tests across 16 test classes
