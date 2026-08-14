package com.abhinav.warehouse.repository;

import com.abhinav.warehouse.entity.FulfillmentLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FulfillmentLogRepository extends JpaRepository<FulfillmentLog, Long> {
    List<FulfillmentLog> findByOrderIdOrderByCreatedAtAsc(Long orderId);
}
