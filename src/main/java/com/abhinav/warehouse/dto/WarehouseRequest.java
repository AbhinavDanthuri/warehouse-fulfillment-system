package com.abhinav.warehouse.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record WarehouseRequest(
        @NotBlank @Size(max = 120) String name,
        @NotBlank @Size(max = 120) String city,
        @NotNull @DecimalMin("-90.0") @DecimalMax("90.0") BigDecimal latitude,
        @NotNull @DecimalMin("-180.0") @DecimalMax("180.0") BigDecimal longitude,
        @NotNull @Min(1) Integer capacity,
        Boolean active
) {}
