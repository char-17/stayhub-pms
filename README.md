# StayHub — Hotel Property Management System

A hotel **PMS** built as a **modular monolith** with Spring Boot (Java 21) and
server-rendered Thymeleaf pages, backed by PostgreSQL and packaged with Docker.

## Modules

One deployable application, organised into bounded modules. The allowed
dependencies between them are checked automatically by `ArchitectureTest` (ArchUnit).

| Module | Responsibility |
|--------|----------------|
| `auth` | Staff accounts and login (roles: `RECEPTIONIST`, `MANAGER`) |
| `rooms` | Room types and rooms |
| `guests` | Guest records |
| `reservations` | Bookings, availability, check-in / check-out |
| `folio` | Extra charges posted to a stay (breakfast, minibar, …) |
| `housekeeping` | Room cleaning status board |
| `billing` | Invoices issued at check-out, payments |
| `reports` | Occupancy, revenue, ADR and RevPAR for a date range |
| `web` | Dashboard, guest profile, error pages |
| `common` | Shared exceptions (`BusinessRuleException`, `NotFoundException`) |
| `config` | Security, demo data, first manager account |

## Demo accounts

Available when demo data is on (`dev` profile, or `docker compose up`).
Billing and reports are restricted to the manager.

| Role | Username | Password |
|------|----------|----------|
| Manager | `manager` | `manager` |
| Reception | `reception` | `reception` |

## Run without Docker

Requires **JDK 21**. Maven is not needed (the included wrapper downloads it on first use).

Windows:

```bat
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"
```

macOS / Linux:

```bash
sh mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

Then open http://localhost:8080 and sign in with one of the accounts above.
The `dev` profile uses an in-memory database with demo data, which is reset on every restart.

## Run with Docker

```bash
docker compose up --build
```

This starts PostgreSQL and the application at http://localhost:8080, with demo data
(`PMS_DEMO_DATA=true` in `docker-compose.yml`). For a real installation remove that line and set
`PMS_ADMIN_USERNAME` and `PMS_ADMIN_PASSWORD` (at least 8 characters) to create the first manager.

## Tests

```bat
.\mvnw.cmd test
```

37 tests run on in-memory H2: entity rules, services, concurrency (five threads, one booking),
error handling and the architecture rules. No database or Docker is needed.
