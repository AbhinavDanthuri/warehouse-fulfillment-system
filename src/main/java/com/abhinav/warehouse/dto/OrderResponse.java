package com.abhinav.warehouse.dto;

import com.abhinav.warehouse.entity.Order;
import com.abhinav.warehouse.entity.OrderStatus;

public record OrderResponse(
        Long id,
        String orderRef,
        String customerName,
        OrderStatus status,
        String failureReason,
        int attemptsUsed
) {
    public static OrderResponse from(Order order, int attemptsUsed) {
        return new OrderResponse(
                order.getId(),
                order.getOrderRef(),
                order.getCustomerName(),
                order.getStatus(),
                order.getFailureReason(),
                attemptsUsed
        );
    }
}
