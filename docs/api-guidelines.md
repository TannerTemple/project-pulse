# API Guidelines

## Base URL

All endpoints are under `/api`.

---

## Conventions

- Use **nouns** for resource names, not verbs: `/sections`, not `/getSection`
- Use **plural** resource names: `/teams`, `/students`
- Use path parameters for resource identity: `/teams/{id}`
- Use query parameters for filtering: `/students?lastName=Smith`

---

## HTTP Methods

| Method | Use |
|--------|-----|
| `GET` | Read resource(s) |
| `POST` | Create a new resource |
| `PUT` | Full update of a resource |
| `PATCH` | Partial update |
| `DELETE` | Remove a resource |

---

## Status Codes

| Code | Meaning |
|------|---------|
| 200 | OK |
| 201 | Created |
| 204 | No Content (successful delete) |
| 400 | Bad Request (invalid input) |
| 401 | Unauthorized (missing/invalid token) |
| 403 | Forbidden (wrong role) |
| 404 | Not Found |
| 409 | Conflict (duplicate name, etc.) |
| 500 | Internal Server Error |

---

## Error Response Format

All errors return a consistent JSON body:

```json
{
  "status": 404,
  "error": "Not Found",
  "messages": ["Section with ID 99 not found"],
  "timestamp": "2024-10-07T14:30:00"
}
```

---

## Authentication

All endpoints (except `/api/auth/**`) require a valid JWT:

```
Authorization: Bearer <token>
```

Tokens are obtained from `POST /api/auth/login`.

---

## Role-Based Access

| Role | Access |
|------|--------|
| `ADMIN` | All endpoints |
| `INSTRUCTOR` | Read teams, read/generate reports |
| `STUDENT` | Own profile, WAR activities, peer evaluations, own report |
