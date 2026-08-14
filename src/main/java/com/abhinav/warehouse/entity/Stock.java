package com.abhinav.warehouse.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * The contended row. Every fulfillment decrements one of these.
 *
 * Two locking strategies are possible against this entity and the repository
 * exposes both, so the README can show the difference:
 *
 *  - Optimistic: the @Version column below. Read freely, and let the UPDATE
 *    fail with an ObjectOptimisticLockingFailureException if another
 *    transaction moved the row first. Then retry.
 *  - Pessimistic: SELECT ... FOR UPDATE via @Lock(PESSIMISTIC_WRITE). The
 *    second transaction blocks at read time instead of failing at write time.
 */
@Entity
@Table(
    name = "stock",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_stock_wh_prod",
        columnNames = {"warehouse_id", "product_id"}
    )
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity = 0;

    @Version
    @Column(nullable = false)
    private Long version;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private Instant updatedAt;

    /** Guards the invariant in code as well as in the CHECK constraint. */
    public void decrement(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("decrement amount must be positive");
        }
        if (this.quantity < amount) {
            throw new IllegalStateException(
                "insufficient stock: have " + this.quantity + ", need " + amount);
        }
        this.quantity -= amount;
    }
}
