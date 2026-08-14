package com.abhinav.warehouse.service;

import com.abhinav.warehouse.entity.Order;
import com.abhinav.warehouse.entity.OrderStatus;
import com.abhinav.warehouse.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Separate bean on purpose. @Transactional works through a Spring proxy, so a
 * method calling another method on `this` bypasses it entirely — the annotation
 * would be silently ignored and the status update would run with no
 * transaction. Crossing a bean boundary is what makes REQUIRES_NEW real.
 */
@Service
@RequiredArgsConstructor
public class OrderStatusService {

    private final OrderRepository orderRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailed(Long orderId, String reason) {
        Order order = orderRepository.findById(orderId).orElseThrow();
        order.setStatus(OrderStatus.FAILED);
        order.setFailureReason(reason != null && reason.length() > 250
                ? reason.substring(0, 250) : reason);
    }
}
