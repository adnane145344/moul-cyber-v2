# Moul Cyber

Moul Cyber is a video game rental and inventory management platform. It is
designed to manage a game catalog, physical copies, customer rentals, returns,
reviews, and administrative inventory operations.

The repository currently provides a tested Spring Boot backend foundation,
PostgreSQL development environment, layered package structure, and public
health endpoint.

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
│   ├── error
│   ├── health
│   └── user
├── application
│   ├── auth
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

Application services orchestrate authentication and current-user use cases.
Spring Data repositories are grouped by feature under `infra/persistence`.

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
| `/api/rentals/**` | Authenticated |
| `/api/admin/**` | `ADMIN` only |

Validation errors return HTTP `400`, duplicate emails return `409`, invalid
credentials or tokens return `401`, and insufficient permissions return `403`.

## Core domain

The core domain is implemented with plain Java objects and does not depend on
Spring, HTTP controllers, or persistence infrastructure.

### Game copies

Each `GameCopy` belongs to a game and has one of the following statuses:

```text
AVAILABLE
RENTED
RETURNED
LOST
DAMAGED
```

Only an available copy can be rented. A rented copy can be returned, while
rented, returned, lost, and damaged copies cannot be rented again. A returned
copy remains in the `RETURNED` state until a separate inventory operation makes
it available again.

### Rentals

A `Rental` belongs to a user and contains physical game copies through
`RentalItem` objects.

- The start date and due date are required.
- The due date must be after the start date.
- The actual return date cannot be before the start date.
- Active rentals become overdue after their due date.
- A returned rental is late when its return date is after its due date.
- The late fee is `2.00` for each late day.

Dates use `LocalDate`, and monetary calculations use `BigDecimal` to keep
business calculations deterministic and precise.

### Reviews

A `Review` links one user to one game.

- Ratings must be between `1` and `5`.
- Comments cannot be null, empty, or blank.
- Surrounding whitespace is removed from comments.

Review eligibility and the one-review-per-user-and-game constraint will be
enforced when rental history and persistence are connected to application use
cases.

## Testing approach

Domain rules are covered by focused unit tests:

```text
GameCopyTest
RentalTest
ReviewTest
```

These tests run without a database or Spring application context. The existing
web test separately verifies the public health endpoint.

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

## Planned capabilities

### Customer

- Create an account and sign in.
- Browse the game catalog and view availability.
- Rent an available physical copy.
- View active rentals and rental history.
- Review a game after renting it.

### Administrator

- Add and update games.
- Manage physical inventory.
- View current and overdue rentals.
- Record returned, lost, or damaged copies.
