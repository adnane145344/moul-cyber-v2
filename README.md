# Moul Cyber

Moul Cyber is a video game rental and inventory management platform.

The application manages a game catalog, physical copies, customer rentals,
returns, reviews, and administrative inventory operations.

## Requirements

- Java 21
- Maven
- PostgreSQL
- Docker and Docker Compose for the local database

## Technology

- Spring Boot
- Spring Web
- Spring Security and JWT
- Spring Data JPA and Hibernate
- PostgreSQL
- JUnit 5 and Mockito

## Running the application

The application setup and startup commands will be added when the initial
backend structure is available.

## Running the tests

The test command will be:

```bash
mvn test
```

## Features

### Customer

- Create an account and sign in.
- Browse the game catalog.
- View game details and availability.
- Rent an available physical copy.
- View active rentals and rental history.
- Review a game after renting it.

### Administrator

- Add and update games.
- Add physical copies to the inventory.
- View current and overdue rentals.
- Record returned copies.
- Track unavailable, lost, and damaged copies.

## Business rules

- Only authenticated customers can rent games.
- Only administrators can access inventory management operations.
- An unavailable copy cannot be rented.
- Rentals have a start date and an expected return date.
- Late returns generate additional fees.
- A customer can review a game only after renting it.
- A customer can submit only one review per game.
