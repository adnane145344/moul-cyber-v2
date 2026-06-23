# Moul Cyber

[![Backend CI](https://github.com/adnane145344/moul-cyber-v2/actions/workflows/backend-ci.yml/badge.svg)](https://github.com/adnane145344/moul-cyber-v2/actions/workflows/backend-ci.yml)

Moul Cyber is a Spring Boot backend for video game rentals and inventory
management. It provides a public game catalog, customer authentication,
rentals, returns, reviews, user profile management, and administrative catalog
and inventory workflows.

## Technology

- Java 21
- Spring Boot 3.5
- Spring Web
- Spring Security with stateless JWT authentication
- Spring Data JPA and Hibernate
- PostgreSQL 17
- Flyway database migrations
- Docker Compose
- Maven
- JUnit 5, Mockito, MockMvc, ArchUnit, and Testcontainers

## Requirements

- JDK 21
- Maven 3.9 or later
- Docker Desktop or another Docker environment with Compose support

Verify the local tools:

```bash
java -version
mvn -version
docker --version
docker compose version
```

## Backend architecture

The backend follows a layered package structure under
`com.adnane.moulcyber`:

```text
com.adnane.moulcyber
├── api
├── application
├── domain
├── infra
├── configuration
└── MoulCyberApplication.java
```

Layer responsibilities:

| Layer | Responsibility |
| --- | --- |
| `api` | REST controllers, request validation, and API error mapping |
| `application` | Use cases, DTOs, and application-level orchestration |
| `domain` | Business rules, entities, enums, and domain exceptions |
| `infra/persistence` | Spring Data JPA repositories and persistence projections |
| `configuration` | Spring, security, and application configuration |

Architecture tests enforce the main dependency rules:

- the domain layer does not depend on API, application, configuration, or infrastructure packages;
- controllers stay in the API layer;
- repositories stay in `infra/persistence`;
- application services do not depend on API classes.

## Configuration

The application reads its runtime configuration from environment variables:

| Variable | Default |
| --- | --- |
| `DB_HOST` | `localhost` |
| `DB_PORT` | `5432` |
| `DB_NAME` | `moul_cyber` |
| `DB_USERNAME` | `moul_cyber` |
| `DB_PASSWORD` | `moul_cyber` |
| `SERVER_PORT` | `8080` |
| `JWT_SECRET` | Required |
| `JWT_EXPIRATION` | `1h` |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:5173` |
| `FLYWAY_BASELINE_ON_MIGRATE` | `false` |

`JWT_SECRET` must be a Base64-encoded secret of at least 32 bytes.

Generate one with PowerShell:

```powershell
$bytes = New-Object byte[] 32
[Security.Cryptography.RandomNumberGenerator]::Fill($bytes)
[Convert]::ToBase64String($bytes)
```

Set it for the current PowerShell session:

```powershell
$env:JWT_SECRET="<base64-secret>"
```

## Database

PostgreSQL is the runtime database. Start it from the backend directory:

```bash
cd backend
docker compose up -d
```

The database container uses:

| Setting | Value |
| --- | --- |
| Database | `moul_cyber` |
| Username | `moul_cyber` |
| Password | `moul_cyber` |
| Port | `5432` |

Schema changes are managed by Flyway migrations in:

```text
backend/src/main/resources/db/migration
```

Hibernate validates the schema at startup with `ddl-auto: validate`; it does
not create or update tables automatically.

If a local database already contains tables created before Flyway was added,
baseline it once:

```powershell
$env:FLYWAY_BASELINE_ON_MIGRATE="true"
```

Use this only for an existing local development database. Leave it disabled for
new databases and automated environments.

Inspect the local schema:

```bash
docker compose exec postgres psql -U moul_cyber -d moul_cyber -c "\dt"
```

Stop PostgreSQL:

```bash
docker compose down
```

The local PostgreSQL data is stored in the `postgres_data` Docker volume.

## Running the backend

From the `backend` directory:

```bash
mvn spring-boot:run
```

The API runs at:

```text
http://localhost:8080
```

Health check:

```bash
curl http://localhost:8080/api/health
```

Expected response:

```json
{"status":"UP"}
```

## Tests and build

Run all tests:

```bash
cd backend
mvn clean test
```

The test suite uses PostgreSQL through Testcontainers, so Docker must be
running. Tests cover domain rules, application services, REST contracts,
security access rules, persistence behavior, Flyway migration startup, and
architecture boundaries.

Build the executable JAR:

```bash
mvn clean package
```

The generated artifact is written to:

```text
backend/target/
```

GitHub Actions runs the backend test suite on pushes and pull requests targeting
`main`.

## API documentation

OpenAPI documentation is generated at runtime:

```text
http://localhost:8080/v3/api-docs
```

Swagger UI is available at:

```text
http://localhost:8080/swagger-ui.html
```

The documentation endpoints are public so the API can be explored easily during
local development.

## Frontend integration

Cross-origin browser requests are enabled through the `CORS_ALLOWED_ORIGINS`
environment variable. The default value is intended for a local Vite frontend:

```text
http://localhost:5173
```

For another frontend origin, set a comma-separated list:

```powershell
$env:CORS_ALLOWED_ORIGINS="http://localhost:5173,http://localhost:3000"
```

The API expects JWTs in the `Authorization` header:

```text
Authorization: Bearer <jwt>
```

## Authentication

Create a client account:

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Adnane",
    "lastName": "Lardi",
    "email": "adnane@example.com",
    "password": "StrongPassword1!"
  }'
```

Successful registration returns HTTP `201`:

```json
{
  "token": "<jwt>",
  "userId": 1,
  "email": "adnane@example.com",
  "role": "CLIENT"
}
```

Sign in:

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "adnane@example.com",
    "password": "StrongPassword1!"
  }'
```

Read the authenticated user:

```bash
curl http://localhost:8080/api/users/me \
  -H "Authorization: Bearer <jwt>"
```

Supported roles:

```text
CLIENT
ADMIN
```

New users receive the `CLIENT` role. For local development, an administrator can
be created by updating a registered user directly in PostgreSQL:

```bash
docker compose exec postgres psql -U moul_cyber -d moul_cyber \
  -c "UPDATE users SET role = 'ADMIN' WHERE email = 'admin@example.com';"
```

Sign in again after changing the role so the JWT contains the updated
authority.

Access rules:

| Route | Access |
| --- | --- |
| `/api/health` | Public |
| `/api/auth/register` | Public |
| `/api/auth/login` | Public |
| `/api/games/**` | Public |
| `/api/users/me/**` | Authenticated |
| `/api/rentals/**` | Authenticated |
| `/api/admin/**` | `ADMIN` only |

Existing JWTs remain valid until expiration after a password change.

## Public catalog

List all games:

```bash
curl http://localhost:8080/api/games
```

Search by title:

```bash
curl "http://localhost:8080/api/games?title=cyber"
```

The `title` parameter is optional, trimmed, and case-insensitive. Blank searches
return the full catalog. Results are sorted by title and then by identifier.

List response:

```json
[
  {
    "id": 1,
    "title": "Cyber Quest",
    "rentalPrice": 5.00,
    "availableCopies": 2
  }
]
```

Read one game:

```bash
curl http://localhost:8080/api/games/1
```

Detail response:

```json
{
  "id": 1,
  "title": "Cyber Quest",
  "description": "A cooperative science-fiction adventure.",
  "rentalPrice": 5.00,
  "availableCopies": 2
}
```

`availableCopies` counts only physical copies with the `AVAILABLE` status.
Unknown games return HTTP `404` with the standard API error format.

## Rentals and returns

Create a rental:

```bash
curl -X POST http://localhost:8080/api/rentals \
  -H "Authorization: Bearer <client-jwt>" \
  -H "Content-Type: application/json" \
  -d '{"gameId": 1}'
```

A rental lasts seven days. Copy selection is transactional and locks the chosen
available copy so concurrent requests cannot rent it twice.

Read current user rentals:

```bash
curl http://localhost:8080/api/rentals/me \
  -H "Authorization: Bearer <client-jwt>"
```

Read one owned rental:

```bash
curl http://localhost:8080/api/rentals/10 \
  -H "Authorization: Bearer <client-jwt>"
```

Administrators process rental items:

```bash
curl -X POST http://localhost:8080/api/admin/rental-items/22/return \
  -H "Authorization: Bearer <admin-jwt>"

curl -X POST http://localhost:8080/api/admin/rental-items/22/mark-lost \
  -H "Authorization: Bearer <admin-jwt>"

curl -X POST http://localhost:8080/api/admin/rental-items/22/mark-damaged \
  -H "Authorization: Bearer <admin-jwt>"
```

Rental item statuses:

```text
ACTIVE
RETURNED
LATE_RETURNED
LOST
DAMAGED
```

Game copy statuses:

```text
AVAILABLE
RENTED
LOST
DAMAGED
```

A normal or late return makes the physical copy `AVAILABLE` again. Lost and
damaged copies remain unavailable. Late fees are `2.00` per day after the due
date. A processed item cannot be processed again.

## Reviews

Reviews are publicly readable:

```bash
curl http://localhost:8080/api/games/1/reviews
```

Authenticated clients can create one review after completing a rental of the
game:

```bash
curl -X POST http://localhost:8080/api/games/1/reviews \
  -H "Authorization: Bearer <client-jwt>" \
  -H "Content-Type: application/json" \
  -d '{
    "rating": 5,
    "comment": "Excellent cooperative game."
  }'
```

Ratings must be between `1` and `5`. Comments are required and limited to 1,000
characters. A user without a completed rental receives HTTP `403`; duplicate
reviews receive HTTP `409`.

## Administration

All administrative endpoints require an `ADMIN` bearer token.

Create a game:

```bash
curl -X POST http://localhost:8080/api/admin/games \
  -H "Authorization: Bearer <admin-jwt>" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Cyber Quest",
    "description": "A cooperative science-fiction adventure.",
    "rentalPrice": 5.00
  }'
```

Update a game:

```bash
curl -X PUT http://localhost:8080/api/admin/games/1 \
  -H "Authorization: Bearer <admin-jwt>" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Cyber Quest Deluxe",
    "description": "An expanded cooperative adventure.",
    "rentalPrice": 7.50
  }'
```

Add copies:

```bash
curl -X POST http://localhost:8080/api/admin/games/1/copies \
  -H "Authorization: Bearer <admin-jwt>" \
  -H "Content-Type: application/json" \
  -d '{"quantity": 3}'
```

Read inventory:

```bash
curl http://localhost:8080/api/admin/inventory \
  -H "Authorization: Bearer <admin-jwt>"
```

Read rentals:

```bash
curl "http://localhost:8080/api/admin/rentals?page=0&size=20" \
  -H "Authorization: Bearer <admin-jwt>"

curl "http://localhost:8080/api/admin/rentals?status=OVERDUE&page=0&size=20" \
  -H "Authorization: Bearer <admin-jwt>"

curl http://localhost:8080/api/admin/rentals/10 \
  -H "Authorization: Bearer <admin-jwt>"
```

Supported rental filters are `ACTIVE`, `OVERDUE`, and `COMPLETED`. List results
are paginated with `page` and `size`; `page` starts at `0`, and `size` accepts
values from `1` to `100`.

Rental list response:

```json
{
  "content": [
    {
      "id": 10,
      "userId": 1,
      "customerName": "Client User",
      "customerEmail": "client@example.com",
      "status": "ACTIVE",
      "overdue": false,
      "startDate": "2026-06-23",
      "dueDate": "2026-06-30",
      "itemCount": 1
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1,
  "first": true,
  "last": true
}
```

## User profile

Update the current user's profile:

```bash
curl -X PUT http://localhost:8080/api/users/me \
  -H "Authorization: Bearer <client-jwt>" \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Adnane",
    "lastName": "Lardi"
  }'
```

Change the current user's password:

```bash
curl -X PUT http://localhost:8080/api/users/me/password \
  -H "Authorization: Bearer <client-jwt>" \
  -H "Content-Type: application/json" \
  -d '{
    "currentPassword": "CurrentPassword1!",
    "newPassword": "NewPassword1!"
  }'
```

Profile names are required and limited to 100 characters. Passwords must contain
between 8 and 72 characters and the new password must differ from the current
one.

## Error responses

API errors use a consistent JSON structure:

```json
{
  "timestamp": "2026-06-23T12:00:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Game not found.",
  "path": "/api/games/999",
  "errors": {}
}
```

Common status codes:

| Status | Meaning |
| --- | --- |
| `400` | Invalid request or validation error |
| `401` | Missing, invalid, or expired credentials |
| `403` | Authenticated user lacks permission |
| `404` | Resource not found |
| `409` | Business conflict, such as duplicate email or unavailable copy |
