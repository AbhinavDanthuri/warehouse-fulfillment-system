package com.abhinav.warehouse.service;

import com.abhinav.warehouse.dto.TraceEntry;
import com.abhinav.warehouse.entity.*;
import com.abhinav.warehouse.repository.FulfillmentLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import jakarta.persistence.EntityManager;

/**
 * Persists the collected trace in its OWN transaction, so it survives even when
 * the fulfillment attempt it describes was rolled back.
 */
@Service
@RequiredArgsConstructor
public class TraceWriter {

    private final FulfillmentLogRepository logRepository;
    private final EntityManager em;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void persist(Long orderId, List<TraceEntry> entries) {
        for (TraceEntry e : entries) {
            FulfillmentLog log = FulfillmentLog.builder()
                    .order(em.getReference(Order.class, orderId))
                    .orderItem(e.orderItemId() == null
                            ? null : em.getReference(OrderItem.class, e.orderItemId()))
                    .warehouse(e.warehouseId() == null
                            ? null : em.getReference(Warehouse.class, e.warehouseId()))
                    .outcome(e.outcome())
                    .availableQty(e.availableQty())
                    .requestedQty(e.requestedQty())
                    .distanceKm(e.distanceKm())
                    .attemptNo(e.attemptNo())
                    .note(e.note())
                    .build();
            logRepository.save(log);
        }
    }
}
