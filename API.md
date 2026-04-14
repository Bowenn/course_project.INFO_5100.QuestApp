# QuestApp API Reference

All endpoints except `/api/auth/register`, `/api/auth/login`, and `/api/health` require **HTTP Basic Auth** or **Bearer Token**:
- `Authorization: Basic base64(email:password)`
- `Authorization: Bearer {jwt_token}`

## Test Users (loaded on first run)

| Role  | Email          | Password |
|-------|----------------|----------|
| GIVER | giver@test.com | password |
| TAKER | taker@test.com | password |
| ADMIN | admin@test.com | password |

---

## Auth

| Method | Endpoint           | Auth | Description                    |
|--------|--------------------|------|--------------------------------|
| POST   | /api/auth/register | No   | Register new user              |
| POST   | /api/auth/login    | No   | Login and get JWT token        |
| GET    | /api/auth/me       | Yes  | Current user profile           |

**Register body:**
```json
{
  "username": "alice",
  "email": "alice@example.com",
  "password": "secret123",
  "role": "GIVER"
}
```
`role`: `GIVER` | `TAKER` | `ADMIN`

**Login body:**
```json
{
  "email": "alice@example.com",
  "password": "secret123"
}
```

**Login response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "user": {
    "id": 1,
    "username": "alice",
    "email": "alice@example.com",
    "role": "GIVER",
    "createdAt": "2023-01-01T00:00:00Z"
  }
}
```

---

## Users

| Method | Endpoint        | Auth | Role        | Description              |
|--------|-----------------|------|-------------|--------------------------|
| GET    | /api/users/me   | Yes  | All         | Current user             |
| GET    | /api/users/takers | Yes | GIVER, ADMIN | List takers (for assign) |
| GET    | /api/users      | Yes  | ADMIN       | List all users           |

---

## Tasks

| Method | Endpoint              | Auth | Role  | Description                          |
|--------|-----------------------|------|-------|--------------------------------------|
| POST   | /api/tasks            | Yes  | GIVER | Create task (DRAFT)                  |
| GET    | /api/tasks            | Yes  | All   | List: GIVER=own, TAKER=assigned, ADMIN=all |
| GET    | /api/tasks/published  | Yes  | All   | List published tasks                 |
| GET    | /api/tasks/{id}       | Yes  | *     | Get task by ID                       |
| PUT    | /api/tasks/{id}      | Yes  | GIVER | Update (DRAFT only)                  |
| POST   | /api/tasks/{id}/publish | Yes | GIVER | Publish (DRAFT → PUBLISHED)        |
| POST   | /api/tasks/{id}/assign | Yes | GIVER | Assign to taker (direct assign)   |
| POST   | /api/tasks/{id}/cancel | Yes | GIVER, ADMIN | Cancel task                  |

**Create task body:**
```json
{
  "title": "Fix bug in login",
  "description": "The login form does not validate email.",
  "bounty": 50.0
}
```

**Assign body:**
```json
{
  "takerId": 2
}
```

---

## Assignments

| Method | Endpoint              | Auth | Role  | Description                          |
|--------|-----------------------|------|-------|--------------------------------------|
| GET    | /api/assignments      | Yes  | All   | List: GIVER=my tasks, TAKER=mine, ADMIN=all |
| GET    | /api/assignments/{id} | Yes  | *     | Get assignment by ID                |
| PUT    | /api/assignments/{id} | Yes  | TAKER | Update status (start, complete, decline) |
| POST   | /api/assignments/{id}/confirm | Yes | GIVER | Confirm completion and transfer bounty |

**Update assignment body:**
```json
{
  "status": "IN_PROGRESS",
  "note": null
}
```
`status`: `IN_PROGRESS` | `COMPLETED` | `DECLINED`

---

## Task Flow (Direct Assign)

1. **GIVER** creates task → `POST /api/tasks` (status: DRAFT)
2. **GIVER** publishes → `POST /api/tasks/{id}/publish` (status: PUBLISHED)
3. **GIVER** assigns to taker → `POST /api/tasks/{id}/assign` with `{"takerId": 2}` (status: ASSIGNED)
4. **TAKER** starts work → `PUT /api/assignments/{id}` with `{"status": "IN_PROGRESS"}` (status: IN_PROGRESS)
5. **TAKER** completes → `PUT /api/assignments/{id}` with `{"status": "COMPLETED", "note": "Done"}` (status: COMPLETED)
6. **GIVER** confirms → `POST /api/assignments/{id}/confirm` (status: CONFIRMED, bounty transferred)

Or **TAKER** declines → `PUT /api/assignments/{id}` with `{"status": "DECLINED"}` → task returns to PUBLISHED.
