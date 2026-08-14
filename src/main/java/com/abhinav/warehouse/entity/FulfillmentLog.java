package com.abhinav.warehouse.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * One row per candidate warehouse considered, per order item — including the
 * ones that were skipped and the retries caused by lock conflicts. The
 * "decision trace" screen is just this table, ordered by created_at.
 */
@Entity
@Table(name = "fulfillment_logs")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FulfillmentLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id")
    private OrderItem orderItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id")
    private Warehouse warehouse;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FulfillmentOutcome outcome;

    @Column(name = "available_qty")
    private Integer availableQty;

    @Column(name = "requested_qty")
    private Integer requestedQty;

    @Column(name = "distance_km", precision = 10, scale = 3)
    private BigDecimal distanceKm;

    @Column(name = "attempt_no", nullable = false)
    private Integer attemptNo = 1;

    @Column(length = 255)
    private String note;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;
}
