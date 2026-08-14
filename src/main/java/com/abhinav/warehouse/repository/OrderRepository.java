package com.abhinav.warehouse.repository;

import com.abhinav.warehouse.entity.Order;
import com.abhinav.warehouse.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderRef(String orderRef);
    List<Order> findByStatus(OrderStatus status);
}
