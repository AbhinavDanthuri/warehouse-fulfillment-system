package com.abhinav.warehouse.dto;

import com.abhinav.warehouse.entity.FulfillmentLog;

import java.math.BigDecimal;
import java.time.Instant;

public record TraceStepResponse(
        Long warehouseId,
        String warehouseName,
        String outcome,
        Integer availableQty,
        Integer requestedQty,
        BigDecimal distanceKm,
        Integer attemptNo,
        String note,
        Instant at
) {
    public static TraceStepResponse from(FulfillmentLog log) {
        return new TraceStepResponse(
                log.getWarehouse() == null ? null : log.getWarehouse().getId(),
                log.getWarehouse() == null ? null : log.getWarehouse().getName(),
                log.getOutcome().name(),
                log.getAvailableQty(),
                log.getRequestedQty(),
                log.getDistanceKm(),
                log.getAttemptNo(),
                log.getNote(),
                log.getCreatedAt()
        );
    }
}
