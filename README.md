# Lanka Bank Core — modular monolith reference

One deployable Spring Boot application with three hard module boundaries, enforced by
tests instead of by convention.

- Spring Boot **4.1.1**
- Spring Modulith **2.1.1**
- ArchUnit **1.4.2** (the version Spring Modulith 2.1.1 itself depends on — pinning a
  newer `archunit-junit5` leaves the engine and the core library on different versions)
- Java **21** (Spring Boot 4.1.1 baseline is 17)

Everything below was run, not sketched: `mvn test` green on H2, and the same code booted
and exercised against PostgreSQL 16.

## Run it

```bash
mvn test                              # 13 tests: modularity, ArchUnit, per-module, end to end
mvn spring-boot:run -Dspring-boot.run.profiles=demo
```

With the `demo` profile the customer module seeds three accounts: `C-001` and `C-002`
active, `C-999` closed.

```bash
curl -i -X POST http://localhost:8080/api/payments \
  -H 'Content-Type: application/json' \
  -d '{"customerId":"C-001","amount":2500.00,"reference":"REF-1"}'
```

The response is `201 Created`. The SMS is sent by the notifications module, on another
thread, after the payment transaction commits.

## Modules

```
com.example.bank
├── customer         allowedDependencies = {}          leaf module
├── payments         allowedDependencies = "customer"
└── notifications    allowedDependencies = "payments"  listens, never called
```

Each module keeps its state under `internal`. Those classes are `public` for JPA's sake,
and unreachable anyway: `ApplicationModules.verify()` rejects a reference to another
module's `internal` package.

## What the tests actually enforce

| Test | Enforces |
|---|---|
| `ModularityTests` | No cycles, no `internal` access across modules, `allowedDependencies` honoured |
| `ArchitectureRulesTest` | Layering inside a module, anti-corruption layer around a vendor SDK |
| `PaymentsModuleTests` | Payments in isolation (`STANDALONE`), with `customer` mocked |
| `NotificationsModuleTests` | Notifications driven only by a published event |
| `PaymentFlowIntegrationTests` | HTTP → payment → event → SMS → publication completed |
| `OutboxResilienceTests` | A dead SMS gateway does not roll back a payment, and the undelivered event survives for resubmission |

Add `import com.example.bank.notifications.NotificationService;` to `PaymentService` and
`mvn test` fails with:

```
- Cycle detected: Slice notifications ->
- Module 'payments' depends on module 'notifications' via
  com.example.bank.payments.PaymentService -> com.example.bank.notifications.NotificationService.
  Allowed targets: customer.
```

## Database

Flyway owns the schema; Hibernate is on `ddl-auto: validate`.

```
src/main/resources/db/migration
├── __root/          event_publication + event_publication_archive (framework-owned)
├── customer/
├── payments/
└── notifications/
```

Module-aware Flyway is **opt-in**:

```yaml
spring.modulith.runtime.flyway-enabled: true
```

Without that flag Flyway scans `db/migration` recursively, finds four files that all call
themselves `V1`, and fails at startup with `Found more than one migration with version 1`.
With it, each module migrates independently and gets its own history table —
`flyway_schema_history` for `__root`, then `flyway_schema_history_customer`,
`flyway_schema_history_payments`, `flyway_schema_history_notifications`. That is also why a
`@ApplicationModuleTest` for payments only runs the `__root` and `payments` migrations.

No foreign keys cross a module boundary. That is deliberate: a foreign key from `payment`
to `customer` is the thing that makes extracting a module later impossible.

## Generated documentation

`mvn test` writes to `target/spring-modulith-docs`:

- `components.puml` — C4 component diagram of all modules
- `module-<name>.puml` — one diagram per module
- `module-<name>.adoc` — the Application Module Canvas, with descriptions pulled from the
  Javadoc on each `package-info.java`

## Postgres

```bash
docker run -d --name bank-pg -p 5432:5432 \
  -e POSTGRES_DB=bank -e POSTGRES_USER=bank -e POSTGRES_PASSWORD=bank postgres:16
mvn spring-boot:run -Dspring-boot.run.profiles=postgres,demo
```

The migrations are plain SQL that both H2 and PostgreSQL accept, verified on 16.

One trap: Flyway 11 ships each database as a separate module. Leave out
`org.flywaydb:flyway-database-postgresql` and the app starts happily on H2, then dies the
first time it meets the real database:

```
FlywayException: Unsupported Database: PostgreSQL 16.15
```

Verified against PostgreSQL, after one `POST /api/payments`:

```
 payments | notifications | incomplete | archived
        1 |             1 |          0 |        1

 flyway_schema_history                 <- db/migration/__root
 flyway_schema_history_customer
 flyway_schema_history_notifications
 flyway_schema_history_payments
```

Posting the same `reference` twice returns the original `paymentId` and publishes no second
event. Posting for the closed account `C-999` returns RFC 9457 problem details:

```json
{"status":409,"title":"Conflict","detail":"Customer C-999 is not active",
 "instance":"/api/payments"}
```
