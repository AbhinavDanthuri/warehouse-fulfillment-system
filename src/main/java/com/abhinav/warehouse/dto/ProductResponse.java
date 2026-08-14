package com.abhinav.warehouse.dto;

import com.abhinav.warehouse.entity.Product;

public record ProductResponse(
        Long id, String sku, String name, String category, Integer lowStockThreshold
) {
    public static ProductResponse from(Product p) {
        return new ProductResponse(p.getId(), p.getSku(), p.getName(),
                p.getCategory(), p.getLowStockThreshold());
    }
}
