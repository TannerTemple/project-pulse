# Project Pulse

A web application for managing weekly activity reports (WAR) and peer evaluations in TCU's Senior Design courses.

## Problem

The current process for submitting and grading WARs and peer evaluations relies on spreadsheets and manual file transfers through the university's LMS. This workflow is time-consuming, error-prone, and creates a heavy manual burden for both students and instructors.

## Solution

Project Pulse consolidates the entire workflow into a single web application where:

- **Students** submit weekly activity reports and peer evaluations directly in the system
- **Instructors** view reports, review peer evaluations, and access automatically generated grades and feedback
- **Admins** manage senior design sections, teams, and student/instructor assignments

## Key Features

- Manage senior design sections, teams, students, and instructors
- Invite students and instructors via email
- Submit and track weekly activity reports (WAR)
- Submit peer evaluations with rubric-based scoring
- Generate peer evaluation and WAR reports automatically

## Tech Stack

- **Frontend:** Vue 3 + Vuetify 4 + Vite + TypeScript
- **Backend:** Spring Boot 4 (Java 21)
- **Database:** MySQL (production) / H2 (local dev)
- **CI/CD:** GitHub Actions
- **Deployment:** Microsoft Azure

## Project Structure

```
project-pulse/
├── backend/          # Spring Boot application (Maven)
├── frontend/         # Vue 3 application (Vite)
├── requirements/     # Vision & scope, use cases, glossary
├── docs/             # Architecture, API guidelines, deployment, etc.
└── .github/          # CI/CD workflows
```

## Getting Started

### Prerequisites

| Tool | Version |
|------|---------|
| Java | 21+ |
| Node.js | 18+ |
| npm | 9+ |

The Maven wrapper (`./mvnw`) is bundled — no separate Maven install needed.

---

### 1. Start the backend

```bash
cd backend
./mvnw spring-boot:run
```

- Runs on **http://localhost:8080**
- Uses an **H2 in-memory database** by default (no setup required)
- A default admin account is created automatically on first startup:
  - **Email:** `admin@projectpulse.app`
  - **Password:** `Admin1234!`

Verify it is running:

```
GET http://localhost:8080/actuator/health
```

---

### 2. Start the frontend

```bash
cd frontend
npm install       # first time only
npm run dev
```

- Runs on **http://localhost:3000**
- API calls are proxied to the backend automatically (no CORS setup needed)

Open **http://localhost:3000** and log in with the admin credentials above.

---

### Other frontend commands

```bash
npm run build        # production build → dist/
npm run type-check   # TypeScript type check
npm run lint         # ESLint
```

---

## Documentation

- [Architecture](docs/architecture.md)
- [Tech Stack](docs/tech-stack.md)
- [API Guidelines](docs/api-guidelines.md)
- [Coding Standards](docs/coding-standards.md)
- [Development Plan](docs/development-plan.md)
- [Team Workflow](docs/team-workflow.md)
- [Testing Strategy](docs/testing-strategy.md)
- [Deployment](docs/deployment.md)
- [Status](STATUS.md)
