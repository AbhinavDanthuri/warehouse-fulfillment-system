# Warehouse Inventory & Order Fulfillment System

Multi-warehouse inventory with a fulfillment engine that will not oversell a
product when orders arrive at the same instant.

## Status

Foundation in place: schema, entities, repositories, Docker MySQL.
Next: auth, then CRUD, then the fulfillment engine.

## Running the database

```bash
docker compose up -d
```

MySQL is on **3308** (3306 is the local install, 3307 belongs to FinFlow's
container). Backend runs on **8081** for the same reason.

```bash
mvn spring-boot:run
```

## Schema notes

`schema.sql` is the source of truth; Hibernate runs with `ddl-auto: validate`
so the app refuses to start if the entities and the tables drift apart.

The `stock` table is the contended one. It carries three defences:

1. `UNIQUE (warehouse_id, product_id)` — one row per pair, so there is exactly
   one thing to lock.
2. `CHECK (quantity >= 0)` — the database itself rejects an oversell even if
   the application logic is wrong.
3. `version` — the optimistic-locking column.
