# Warehouse Inventory & Order Fulfillment System

Multi-warehouse inventory with a fulfillment engine that routes each order to the
nearest warehouse holding stock — and will not oversell a product when orders
arrive at the same instant.

**Stack:** Java 21 · Spring Boot 3.3 · Spring Data JPA / Hibernate 6 · MySQL 8 · Docker Compose · JUnit 5

---

## The problem this solves

Two customers order the last unit of a product at the same millisecond. Both
requests read "1 in stock", both decrement, and the warehouse now owes one unit
it does not have. The naive version of this system oversells; this one does not,
and there is a test that proves it rather than a claim that asserts it.

## The proof

`ConcurrentFulfillmentTest` fires 30 threads at a single product holding 12
units. All threads park on a `CountDownLatch` and are released together, so they
genuinely contend rather than trickling in.

The invariant: **stock must land on exactly 0, and exactly 12 orders may
succeed.**

```
==== CONCURRENCY RESULT ====
threads fired      : 30
units available    : 12
orders fulfilled   : 12
orders failed      : 18
stock remaining    : 0
total attempts used: 30
============================
```

## Pessimistic vs optimistic: measured, not guessed

Both strategies are implemented and switchable via `application.yml`. Same test,
same workload:

| Strategy | Attempts | Retries | Oversold |
|---|---|---|---|
| Pessimistic (`SELECT ... FOR UPDATE`) | 30 | 0 | 0 |
| Optimistic (`@Version` + retry) | 109 | 79 | 0 |

Both are correct. Optimistic does **3.6× the work** to get there.

**Why pessimistic is the default here:** optimistic locking is cheaper only when
conflicts are rare, because it pays for conflicts after the fact by redoing work.
On a single hot SKU, conflicts are the normal case, not the exception — so
optimistic just converts blocking into wasted work. If contention were spread
across thousands of SKUs, the answer would flip.

## The bug worth reading about

The first pessimistic run reported **113 attempts with 83 optimistic-lock
failures** — under `SELECT ... FOR UPDATE`, which should never produce them.

Root cause was Hibernate's persistence context. The unlocked query that ranks
candidate warehouses by distance was loading `Stock` **entities**, which put them
in the session. When `findForUpdate` then ran, Hibernate took a genuine row lock
at the database but returned the **already-cached instance** instead of the freshly
read row. Hibernate noticed the version mismatch during `upgradeLockMode` and
threw `StaleObjectStateException`.

The lock was real. The in-memory state it protected was stale.

The fix was to make the ranking query return a **projection** (`StockCandidate`)
rather than entities, so the locked read is the only managed entity in the
transaction. Attempts dropped from 113 to 30, retries from 83 to 0.

## Three design decisions

**The retry loop is not transactional.** You cannot retry an optimistic failure
inside the transaction that failed — it is already marked rollback-only.
`FulfillmentService` therefore runs outside any transaction and calls
`FulfillmentAttemptService` (`REQUIRES_NEW`) once per attempt.

**One attempt covers the whole order, not one item at a time.** If item 2 cannot
be fulfilled, item 1's decrement has to be undone. Letting the transaction roll
back does that for free; compensating writes are how you get phantom stock.

**The decision trace is collected in memory and written separately.** Logging
inside the attempt would mean a rollback erases the very rows that explain why
the retry happened.

Defence in depth on the `stock` table: `UNIQUE (warehouse_id, product_id)` so
there is exactly one row to lock, `CHECK (quantity >= 0)` so the database rejects
an oversell even if the application logic is wrong, and a `version` column for
the optimistic path.

## Running it

```bash
docker compose up -d          # MySQL on 3308
mvn spring-boot:run           # app on 8081
```

`schema.sql` is the source of truth; Hibernate runs with `ddl-auto: validate`, so
the app refuses to start if the entities and tables drift apart.

```bash
mvn test                      # the concurrency demo
```

## API

| Method | Endpoint | Purpose |
|---|---|---|
| `POST` | `/api/orders` | Place an order and run fulfillment |
| `GET` | `/api/orders/{id}` | Order status |
| `GET` | `/api/orders/{id}/trace` | Decision trace — every warehouse considered, distance, and why it was picked or skipped |
| `GET` | `/api/stock` | Stock across all warehouses |
| `GET` | `/api/stock/low` | Rows at or below their low-stock threshold |

Example trace output — the routing logic, made visible:

```json
[{
  "warehouseName": "Gachibowli DC",
  "outcome": "SELECTED",
  "availableQty": 8,
  "requestedQty": 2,
  "distanceKm": 4.361,
  "note": "PESSIMISTIC lock held"
}]
```

## Not yet built

Auth is stubbed open — JWT with admin / warehouse-manager / staff roles is next.
Also pending: warehouse and product CRUD endpoints, low-stock alerting, dashboard
analytics, CSV bulk import, and the React frontend.