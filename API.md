# QuestApp API Reference

All endpoints except `/api/auth/register`, `/api/auth/login`, and `/api/health` require a **JWT Bearer token**:
`Authorization: Bearer <token>`

Obtain a token by calling `POST /api/auth/login`.

## Default Accounts

| Role  | Email                | Password |
|-------|----------------------|----------|
| ADMIN | admin@questapp.com   | admin123 |
| USER  | alice@test.com       | password |
| USER  | bob@test.com         | password |

> ADMIN accounts are created by the system on startup. Users cannot self-register as ADMIN.

---

## Auth

| Method | Endpoint           | Auth | Description                        |
|--------|--------------------|------|------------------------------------|
| POST   | /api/auth/register | No   | Register new user                  |
| POST   | /api/auth/login    | No   | Login and receive JWT token        |
| GET    | /api/auth/me       | Yes  | Current user profile               |

**Register body:**
```json
{
  "username": "alice",
  "email": "alice@example.com",
  "password": "secret123"
}
```
> Role is NOT included. All registered users are automatically assigned the `USER` role.

**Login body:**
```json
{
  "email": "alice@test.com",
  "password": "password"
}
```

**Login response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "type": "Bearer",
  "email": "alice@test.com",
  "role": "USER"
}
```
Use the `token` value in subsequent requests: `Authorization: Bearer <token>`

---

## Users

| Method | Endpoint        | Auth | Role  | Description                        |
|--------|-----------------|------|-------|------------------------------------|
| GET    | /api/users/me   | Yes  | All   | Current user profile               |
| GET    | /api/users/list | Yes  | All   | List all USER-role accounts        |
| GET    | /api/users      | Yes  | ADMIN | List all users including admins    |

---

## Tasks

| Method | Endpoint                | Auth | Role       | Description                              |
|--------|-------------------------|------|------------|------------------------------------------|
| POST   | /api/tasks              | Yes  | USER       | Create task (DRAFT)                      |
| GET    | /api/tasks              | Yes  | All        | List: USER=own+accepted, ADMIN=all       |
| GET    | /api/tasks/published    | Yes  | All        | List published tasks available to accept |
| GET    | /api/tasks/{id}         | Yes  | *          | Get task by ID                           |
| PUT    | /api/tasks/{id}         | Yes  | USER       | Update (DRAFT only, owner only)          |
| POST   | /api/tasks/{id}/publish | Yes  | USER       | Publish (DRAFT → PUBLISHED, owner only)  |
| POST   | /api/tasks/{id}/accept  | Yes  | USER       | Accept task (self-assign, non-owner)     |
| POST   | /api/tasks/{id}/cancel  | Yes  | USER/ADMIN | Cancel task (owner or admin)             |
| DELETE | /api/tasks/{id}         | Yes  | ADMIN      | Permanently delete task                  |

**Create task body:**
```json
{
  "title": "Fix bug in login",
  "description": "The login form does not validate email."
}
```

> `POST /api/tasks/{id}/accept` requires no request body. The current user self-assigns.
> A user cannot accept their own task.

---

## Assignments

| Method | Endpoint              | Auth | Role  | Description                              |
|--------|-----------------------|------|-------|------------------------------------------|
| GET    | /api/assignments      | Yes  | All   | List grouped: asOwner + asTaker          |
| GET    | /api/assignments/{id} | Yes  | *     | Get assignment by ID                     |
| PUT    | /api/assignments/{id} | Yes  | USER  | Update status (start, complete, decline) |

**GET /api/assignments response:**
```json
{
  "asOwner": [
    { "id": 1, "task": {}, "taker": {}, "status": "IN_PROGRESS", "assignedAt": "...", "completedAt": null, "note": null }
  ],
  "asTaker": [
    { "id": 2, "task": {}, "taker": {}, "status": "ASSIGNED", "assignedAt": "...", "completedAt": null, "note": null }
  ]
}
```
- `asOwner` — assignments for tasks you created (monitor progress)
- `asTaker` — assignments you accepted (tasks you need to act on)

**Update assignment body:**
```json
{
  "status": "IN_PROGRESS",
  "note": null
}
```
`status`: `IN_PROGRESS` | `COMPLETED` | `DECLINED`

---

## Task Flow

1. **USER A** creates task → `POST /api/tasks` (status: `DRAFT`)
2. **USER A** publishes → `POST /api/tasks/{id}/publish` (status: `PUBLISHED`)
3. **USER B** accepts → `POST /api/tasks/{id}/accept` (status: `ASSIGNED`)
4. **USER B** starts work → `PUT /api/assignments/{id}` with `{"status": "IN_PROGRESS"}` (status: `IN_PROGRESS`)
5. **USER B** completes → `PUT /api/assignments/{id}` with `{"status": "COMPLETED", "note": "Done"}` (status: `COMPLETED`)

Or **USER B** declines → `PUT /api/assignments/{id}` with `{"status": "DECLINED"}` → task returns to `PUBLISHED`.
