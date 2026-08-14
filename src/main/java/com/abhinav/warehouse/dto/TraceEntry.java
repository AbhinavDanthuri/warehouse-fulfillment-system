package com.abhinav.warehouse.dto;

import com.abhinav.warehouse.entity.FulfillmentOutcome;

import java.math.BigDecimal;

/**
 * An in-memory step of the decision trace.
 *
 * These are deliberately NOT written to the DB as they happen: an optimistic
 * retry rolls its transaction back, which would erase the very rows that
 * explain why the retry was needed. They are collected here and persisted in a
 * separate transaction once the attempt loop finishes.
 */
public record TraceEntry(
        Long orderItemId,
        Long warehouseId,
        FulfillmentOutcome outcome,
        Integer availableQty,
        Integer requestedQty,
        BigDecimal distanceKm,
        int attemptNo,
        String note
) {}
