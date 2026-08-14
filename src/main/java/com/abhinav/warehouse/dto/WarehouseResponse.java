package com.abhinav.warehouse.dto;

import com.abhinav.warehouse.entity.Warehouse;

import java.math.BigDecimal;

public record WarehouseResponse(
        Long id, String name, String city,
        BigDecimal latitude, BigDecimal longitude,
        Integer capacity, boolean active
) {
    public static WarehouseResponse from(Warehouse w) {
        return new WarehouseResponse(w.getId(), w.getName(), w.getCity(),
                w.getLatitude(), w.getLongitude(), w.getCapacity(), w.isActive());
    }
}
