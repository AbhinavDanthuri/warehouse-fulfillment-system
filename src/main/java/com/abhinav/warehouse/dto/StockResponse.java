package com.abhinav.warehouse.dto;

import com.abhinav.warehouse.entity.Stock;

public record StockResponse(
        Long id, Long warehouseId, String warehouseName,
        Long productId, String sku, String productName,
        Integer quantity, Integer lowStockThreshold, boolean lowStock, Long version
) {
    public static StockResponse from(Stock s) {
        int threshold = s.getProduct().getLowStockThreshold();
        return new StockResponse(
                s.getId(),
                s.getWarehouse().getId(), s.getWarehouse().getName(),
                s.getProduct().getId(), s.getProduct().getSku(), s.getProduct().getName(),
                s.getQuantity(), threshold, s.getQuantity() <= threshold, s.getVersion());
    }
}
