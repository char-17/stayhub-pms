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
