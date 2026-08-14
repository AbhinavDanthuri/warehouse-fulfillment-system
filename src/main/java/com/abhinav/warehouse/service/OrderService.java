package com.abhinav.warehouse.service;

import com.abhinav.warehouse.dto.OrderItemRequest;
import com.abhinav.warehouse.dto.PlaceOrderRequest;
import com.abhinav.warehouse.entity.Order;
import com.abhinav.warehouse.entity.OrderItem;
import com.abhinav.warehouse.entity.OrderStatus;
import com.abhinav.warehouse.repository.OrderRepository;
import com.abhinav.warehouse.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    /**
     * Persisted and committed BEFORE fulfillment runs. A rolled-back attempt
     * must not take the order record down with it — a failed order still needs
     * to exist so someone can look at why.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Long placeOrder(PlaceOrderRequest req) {
        Order order = Order.builder()
                .orderRef("ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .customerName(req.customerName())
                .destLatitude(req.destLatitude())
                .destLongitude(req.destLongitude())
                .status(OrderStatus.PLACED)
                .build();

        for (OrderItemRequest itemReq : req.items()) {
            var product = productRepository.findById(itemReq.productId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "no product " + itemReq.productId()));
            order.addItem(OrderItem.builder()
                    .product(product)
                    .quantity(itemReq.quantity())
                    .build());
        }

        return orderRepository.save(order).getId();
    }
}
