package com.abhinav.warehouse.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Absolute set, not a delta. A delta ("add 5") is ambiguous under concurrent
 * edits — two managers each adding 5 to a screen showing 10 both think the
 * answer is 15. An absolute value makes the intent explicit and the version
 * check meaningful.
 */
public record StockAdjustRequest(
        @NotNull Long warehouseId,
        @NotNull Long productId,
        @NotNull @Min(0) Integer quantity
) {}
