package com.abhinav.warehouse.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")   // "order" is reserved in SQL
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_ref", nullable = false, unique = true, length = 40)
    private String orderRef;

    @Column(name = "customer_name", nullable = false, length = 150)
    private String customerName;

    @Column(name = "dest_latitude", nullable = false, precision = 9, scale = 6)
    private BigDecimal destLatitude;

    @Column(name = "dest_longitude", nullable = false, precision = 9, scale = 6)
    private BigDecimal destLongitude;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.PLACED;

    @Column(name = "failure_reason")
    private String failureReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "placed_by")
    private User placedBy;

    @Builder.Default
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }
}
