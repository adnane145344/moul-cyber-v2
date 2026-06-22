# Moul Cyber

Moul Cyber is a video game rental and inventory management platform. It is
designed to manage a game catalog, physical copies, customer rentals, returns,
reviews, and administrative inventory operations.

The repository provides a tested Spring Boot backend, PostgreSQL development
environment, layered package structure, stateless authentication, customer
rental workflows, and administrative inventory operations.

## Technology

- Java 21
- Spring Boot 3.5
- Spring Web
- Spring Data JPA and Hibernate
- Spring Security
- JSON Web Tokens with JJWT
- PostgreSQL 17
- JUnit 5 and Mockito
- Maven 3.9
- Docker Compose

## Requirements

- JDK 21
- Maven 3.9 or later
- Docker Desktop or another Docker environment with Compose support

Verify the local tools before starting:

```bash
java -version
mvn -version
docker --version
docker compose version
```

## Running the application

Start PostgreSQL:

```bash
cd backend
docker compose up -d
```

Start the backend from the same directory:

```bash
mvn spring-boot:run
```

The API is available at `http://localhost:8080`.

Check its availability:

```bash
curl http://localhost:8080/api/health
```

Expected response:

```json
{"status":"UP"}
```

Stop the database when it is no longer needed:

```bash
docker compose down
```

The PostgreSQL data is stored in the `postgres_data` Docker volume and is
preserved when the container stops.

## Tests and build

Run the test suite:

```bash
cd backend
mvn clean test
```

Build the executable JAR:

```bash
mvn clean package
```

The generated artifact is written to `backend/target/`.

## Configuration

The application reads its database and server settings from environment
variables. Each variable has a local development default:

| Variable | Default |
| --- | --- |
| `DB_HOST` | `localhost` |
| `DB_PORT` | `5432` |
| `DB_NAME` | `moul_cyber` |
| `DB_USERNAME` | `moul_cyber` |
| `DB_PASSWORD` | `moul_cyber` |
| `SERVER_PORT` | `8080` |
| `JWT_SECRET` | Development-only Base64-encoded secret |
| `JWT_EXPIRATION` | `1h` |

`JWT_SECRET` must contain a Base64-encoded HMAC key of at least 32 bytes. Replace
the local default before deploying the application.

## Backend structure

The backend uses a layered architecture under `com.adnane.moulcyber`:

| Layer | Responsibility |
| --- | --- |
| `api` | HTTP controllers and request validation |
| `application` | Application services, DTOs, and object assembly |
| `domain` | Business models, rules, and domain exceptions |
| `infra` | Persistence and external-system integrations |
| `configuration` | Spring and security configuration |

Only layers with implemented responsibilities are present. The current source
tree contains:

```text
com.adnane.moulcyber
├── api
│   ├── auth
│   ├── catalog
│   ├── error
│   ├── health
│   ├── rental
│   ├── review
│   └── user
├── application
│   ├── auth
│   ├── catalog
│   ├── rental
│   ├── review
│   └── user
├── configuration
│   └── security
├── domain
│   ├── catalog
│   ├── inventory
│   ├── rental
│   ├── review
│   └── user
├── infra
│   └── persistence
│       ├── catalog
│       ├── inventory
│       ├── rental
│       ├── review
│       └── user
└── MoulCyberApplication.java
```

Application services orchestrate authentication, current-user, catalog,
rental, return, review, profile, and administrative use cases. Spring Data
repositories are grouped by feature under `infra/persistence`.

The domain layer owns the business behavior and does not depend on the API,
configuration, or infrastructure layers. Its entities include Jakarta
Persistence metadata, while their rules remain independent from Spring and
repository implementations. Higher layers may depend on the domain.

The API uses stateless bearer-token authentication. Passwords are hashed with
BCrypt and are never included in API responses.

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

The API currently supports two roles:

```text
CLIENT
ADMIN
```

New accounts always receive `CLIENT`. For local development, an administrator
can be created by registering a normal account and then updating its role:

```bash
docker compose exec postgres psql -U moul_cyber -d moul_cyber \
  -c "UPDATE users SET role = 'ADMIN' WHERE email = 'admin@example.com';"
```

Sign in again after changing the role so the new JWT contains the `ADMIN`
authority.

Access rules:

| Route | Access |
| --- | --- |
| `/api/health` | Public |
| `/api/auth/register` | Public |
| `/api/auth/login` | Public |
| `/api/games/**` | Public |
| `/api/users/me` | Authenticated |
| `/api/users/me/password` | Authenticated |
| `/api/rentals/**` | Authenticated |
| `/api/admin/**` | `ADMIN` only |

Validation errors return HTTP `400`, duplicate emails return `409`, invalid
credentials or tokens return `401`, and insufficient permissions return `403`.

## User profile

Authenticated users can update their first and last names:

```bash
curl -X PUT http://localhost:8080/api/users/me \
  -H "Authorization: Bearer <client-jwt>" \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Adnane",
    "lastName": "Lardi"
  }'
```

Names are trimmed, required, and limited to 100 characters. Email addresses and
roles cannot be changed through this endpoint.

Users can change their password after confirming the current password:

```bash
curl -X PUT http://localhost:8080/api/users/me/password \
  -H "Authorization: Bearer <client-jwt>" \
  -H "Content-Type: application/json" \
  -d '{
    "currentPassword": "CurrentPassword1!",
    "newPassword": "NewPassword1!"
  }'
```

The new password must contain between 8 and 72 characters and must differ from
the current password. A successful change returns HTTP `204`. Existing JWTs
remain valid until their normal expiration time.

## Public catalog

The game catalog is available without authentication.

List all games:

```bash
curl http://localhost:8080/api/games
```

Search by partial title:

```bash
curl "http://localhost:8080/api/games?title=cyber"
```

The `title` parameter is optional, trimmed, and case-insensitive. An absent or
blank value returns the complete catalog. Results are sorted by title and then
by identifier.

List responses contain summaries:

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

Detail responses also include the description:

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
Games without an available copy remain visible with a count of zero. An unknown
identifier returns HTTP `404` with the standard API error format.

## Rentals

Authenticated clients can rent one available copy of a game:

```bash
curl -X POST http://localhost:8080/api/rentals \
  -H "Authorization: Bearer <client-jwt>" \
  -H "Content-Type: application/json" \
  -d '{"gameId": 1}'
```

A rental lasts seven days. Creation, copy selection, and the transition from
`AVAILABLE` to `RENTED` happen in one transaction. The selected copy is locked
while the rental is created so concurrent requests cannot rent it twice.

Successful creation returns HTTP `201`:

```json
{
  "id": 10,
  "status": "ACTIVE",
  "startDate": "2026-06-22",
  "dueDate": "2026-06-29",
  "items": [
    {
      "id": 22,
      "gameId": 1,
      "gameTitle": "Cyber Quest",
      "copyId": 4,
      "status": "ACTIVE",
      "rentalPrice": 5.00,
      "processedDate": null,
      "lateFee": 0.00
    }
  ]
}
```

If no copy is available, the API returns HTTP `409`.

Read the current user's rentals and one owned rental:

```bash
curl http://localhost:8080/api/rentals/me \
  -H "Authorization: Bearer <client-jwt>"

curl http://localhost:8080/api/rentals/10 \
  -H "Authorization: Bearer <client-jwt>"
```

Users cannot read another user's rental. Such requests return HTTP `404`.

## Returns and inventory outcomes

Administrators process active rental items:

```bash
curl -X POST http://localhost:8080/api/admin/rental-items/22/return \
  -H "Authorization: Bearer <admin-jwt>"

curl -X POST http://localhost:8080/api/admin/rental-items/22/mark-lost \
  -H "Authorization: Bearer <admin-jwt>"

curl -X POST http://localhost:8080/api/admin/rental-items/22/mark-damaged \
  -H "Authorization: Bearer <admin-jwt>"
```

Rental item statuses are:

```text
ACTIVE
RETURNED
LATE_RETURNED
LOST
DAMAGED
```

A normal or late return makes the physical copy `AVAILABLE`. A lost or damaged
item keeps its copy unavailable. Late fees are `2.00` for each day after the
due date. Completed items cannot be processed again.

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

Update an existing game:

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

Titles and descriptions are required. Titles are limited to 255 characters,
and rental prices must be strictly positive.

Add between 1 and 100 available physical copies:

```bash
curl -X POST http://localhost:8080/api/admin/games/1/copies \
  -H "Authorization: Bearer <admin-jwt>" \
  -H "Content-Type: application/json" \
  -d '{"quantity": 3}'
```

Read the inventory summary:

```bash
curl http://localhost:8080/api/admin/inventory \
  -H "Authorization: Bearer <admin-jwt>"
```

The response contains one entry per game, including games without physical
copies. Counts are grouped by `AVAILABLE`, `RENTED`, `LOST`, and `DAMAGED`, and
results are sorted by title and identifier.

Administrators can inspect all rentals or filter them:

```bash
curl http://localhost:8080/api/admin/rentals \
  -H "Authorization: Bearer <admin-jwt>"

curl "http://localhost:8080/api/admin/rentals?status=OVERDUE" \
  -H "Authorization: Bearer <admin-jwt>"

curl http://localhost:8080/api/admin/rentals/10 \
  -H "Authorization: Bearer <admin-jwt>"
```

Supported filters are `ACTIVE`, `OVERDUE`, and `COMPLETED`. Overdue state is
calculated from the current date and the rental due date. Results are sorted by
start date and identifier in descending order.

## Reviews

Reviews are publicly readable:

```bash
curl http://localhost:8080/api/games/1/reviews
```

An authenticated client may create one review after completing a rental of the
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

Ratings must be between `1` and `5`. Comments are required and limited to
1,000 characters. A user without a completed rental receives HTTP `403`, while
a duplicate review receives HTTP `409`.

## Core domain

The core domain is implemented with plain Java objects and does not depend on
Spring, HTTP controllers, or persistence infrastructure.

### Game copies

Each `GameCopy` belongs to a game and has one of the following statuses:

```text
AVAILABLE
RENTED
LOST
DAMAGED
```

Only an available copy can be rented. Returning a rented copy makes it
`AVAILABLE` again. Lost and damaged copies cannot be rented or returned.

### Rentals

A `Rental` belongs to a user and contains physical game copies through
`RentalItem` objects. Each item stores the rental price at creation time so
later catalog changes do not alter rental history.

- The start date and due date are required.
- The due date must be after the start date.
- Processing dates cannot be before the rental start date.
- Active rentals become overdue after their due date.
- A rental is completed when none of its items remain active.
- The late fee is `2.00` for each late day and is stored per item.

Dates use `LocalDate`, and monetary calculations use `BigDecimal` to keep
business calculations deterministic and precise.

### Reviews

A `Review` links one user to one game.

- Ratings must be between `1` and `5`.
- Comments cannot be null, empty, or blank.
- Comments cannot exceed 1,000 characters.
- Surrounding whitespace is removed from comments.

Review eligibility is based on completed rental items. A database constraint
also enforces one review per user and game.

## Testing approach

Domain rules are covered by focused unit tests:

```text
GameCopyTest
RentalTest
ReviewTest
```

Application services are covered independently:

```text
RentalItemProcessingServiceTest
RentalServiceTest
ReviewServiceTest
```

These tests run without a database or Spring application context. HTTP tests
verify authentication, authorization, JSON contracts, and error responses.

Repository tests use an in-memory H2 database configured in PostgreSQL
compatibility mode. They verify generated identifiers, entity relationships,
cascades, status persistence, derived repository queries, and uniqueness
constraints.

## Persistence

The application uses Spring Data JPA and Hibernate with the following tables:

```text
users
games
game_copies
rentals
rental_items
reviews
```

The main relationships are:

```text
User       1 ─── * Rental
User       1 ─── * Review
Game       1 ─── * GameCopy
Game       1 ─── * Review
Rental     1 ─── * RentalItem
RentalItem * ─── 1 GameCopy
```

Repositories are intentionally simple Spring Data interfaces:

```text
UserRepository
GameRepository
GameCopyRepository
RentalRepository
RentalItemRepository
ReviewRepository
```

For local development, Hibernate currently uses `ddl-auto: update` to maintain
the PostgreSQL schema. This is a temporary learning-oriented setup. Versioned
database migrations will replace automatic schema updates before production
delivery.

The Maven test suite does not require Docker. Persistence tests use the `test`
profile and recreate an in-memory database for each test context. PostgreSQL 17
remains the runtime database and can be started with Docker Compose.

To inspect the runtime schema:

```bash
cd backend
docker compose up -d
docker compose exec postgres psql -U moul_cyber -d moul_cyber -c "\dt"
```
