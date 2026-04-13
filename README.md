# Project Pulse

Monorepo with a Spring Boot backend and a Vue + Vuetify frontend.

```
project-pulse/
├── backend/    # Spring Boot 4.x (Maven, Java 21)
└── frontend/   # Vue 3 + Vuetify + Pinia + Vue Router (Vite, TypeScript)
```

---

## Prerequisites

| Tool | Version |
|------|---------|
| Java | 21+ |
| Maven wrapper (`./mvnw`) | bundled in `backend/` |
| Node.js | 18+ |
| npm | 9+ |

---

## Running locally

### Backend

```bash
cd backend

# First run – download dependencies and compile
./mvnw compile

# Start the dev server (port 8080)
./mvnw spring-boot:run
```

The app starts without a database configured (DataSourceAutoConfiguration is
excluded in `application.properties`). To connect PostgreSQL, add the following
to `backend/src/main/resources/application.properties` and remove the exclusion:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/projectpulse
spring.datasource.username=<user>
spring.datasource.password=<password>
```

Actuator health check: <http://localhost:8080/actuator/health>

### Frontend

```bash
cd frontend

# Install dependencies
npm install

# Start the dev server (port 5173)
npm run dev
```

Other useful commands:

```bash
npm run build        # production build → dist/
npm run type-check   # TypeScript check
npm run lint         # ESLint
```
