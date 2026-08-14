-- Warehouse Inventory & Order Fulfillment System
-- Source of truth for the schema. Hibernate runs in `validate` mode against this.

CREATE TABLE IF NOT EXISTS users (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    email         VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name     VARCHAR(120) NOT NULL,
    role          ENUM('ADMIN','WAREHOUSE_MANAGER','STAFF') NOT NULL,
    created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS warehouses (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(120) NOT NULL UNIQUE,
    city       VARCHAR(120) NOT NULL,
    latitude   DECIMAL(9,6)  NOT NULL,
    longitude  DECIMAL(9,6)  NOT NULL,
    capacity   INT NOT NULL,
    active     BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS products (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    sku                 VARCHAR(64)  NOT NULL UNIQUE,
    name                VARCHAR(180) NOT NULL,
    category            VARCHAR(80),
    low_stock_threshold INT NOT NULL DEFAULT 10,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- The concurrency hotspot. Every fulfillment decrements a row here.
-- `version` drives optimistic locking; the same row can also be taken
-- with SELECT ... FOR UPDATE for the pessimistic strategy.
CREATE TABLE IF NOT EXISTS stock (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    warehouse_id BIGINT NOT NULL,
    product_id   BIGINT NOT NULL,
    quantity     INT NOT NULL DEFAULT 0,
    version      BIGINT NOT NULL DEFAULT 0,
    updated_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_stock_wh_prod UNIQUE (warehouse_id, product_id),
    CONSTRAINT fk_stock_warehouse FOREIGN KEY (warehouse_id) REFERENCES warehouses(id),
    CONSTRAINT fk_stock_product   FOREIGN KEY (product_id)   REFERENCES products(id),
    CONSTRAINT ck_stock_non_negative CHECK (quantity >= 0)
) ENGINE=InnoDB;



CREATE TABLE IF NOT EXISTS orders (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_ref      VARCHAR(40) NOT NULL UNIQUE,
    customer_name  VARCHAR(150) NOT NULL,
    dest_latitude  DECIMAL(9,6) NOT NULL,
    dest_longitude DECIMAL(9,6) NOT NULL,
    status         ENUM('PLACED','FULFILLING','FULFILLED','FAILED') NOT NULL DEFAULT 'PLACED',
    failure_reason VARCHAR(255),
    placed_by      BIGINT,
    created_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_orders_user FOREIGN KEY (placed_by) REFERENCES users(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS order_items (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id   BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity   INT NOT NULL,
    CONSTRAINT fk_items_order   FOREIGN KEY (order_id)   REFERENCES orders(id) ON DELETE CASCADE,
    CONSTRAINT fk_items_product FOREIGN KEY (product_id) REFERENCES products(id),
    CONSTRAINT ck_item_qty_positive CHECK (quantity > 0)
) ENGINE=InnoDB;

-- Audit trail: one row per candidate warehouse considered, per order item.
-- This is what the "decision trace" screen reads from.
CREATE TABLE IF NOT EXISTS fulfillment_logs (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id          BIGINT NOT NULL,
    order_item_id     BIGINT,
    warehouse_id      BIGINT,
    outcome           ENUM('SELECTED','SKIPPED_NO_STOCK','SKIPPED_INACTIVE','LOCK_CONFLICT_RETRY','FAILED_NO_WAREHOUSE') NOT NULL,
    available_qty     INT,
    requested_qty     INT,
    distance_km       DECIMAL(10,3),
    attempt_no        INT NOT NULL DEFAULT 1,
    note              VARCHAR(255),
    created_at        TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    CONSTRAINT fk_logs_order     FOREIGN KEY (order_id)     REFERENCES orders(id) ON DELETE CASCADE,
    CONSTRAINT fk_logs_item      FOREIGN KEY (order_item_id) REFERENCES order_items(id) ON DELETE CASCADE,
    CONSTRAINT fk_logs_warehouse FOREIGN KEY (warehouse_id) REFERENCES warehouses(id)
) ENGINE=InnoDB;


