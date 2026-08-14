package com.abhinav.warehouse.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public record PlaceOrderRequest(
        @NotBlank String customerName,
        @NotNull BigDecimal destLatitude,
        @NotNull BigDecimal destLongitude,
        @NotEmpty @Valid List<OrderItemRequest> items
) {}
