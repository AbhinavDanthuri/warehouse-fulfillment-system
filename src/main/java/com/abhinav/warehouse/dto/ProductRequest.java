package com.abhinav.warehouse.dto;

import jakarta.validation.constraints.*;

public record ProductRequest(
        @NotBlank @Size(max = 64) String sku,
        @NotBlank @Size(max = 180) String name,
        @Size(max = 80) String category,
        @NotNull @Min(0) Integer lowStockThreshold
) {}
