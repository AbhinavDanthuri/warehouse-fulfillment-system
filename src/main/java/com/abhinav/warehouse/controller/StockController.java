package com.abhinav.warehouse.controller;

import com.abhinav.warehouse.dto.StockAdjustRequest;
import com.abhinav.warehouse.dto.StockResponse;
import com.abhinav.warehouse.service.StockService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stock")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    @GetMapping
    public List<StockResponse> list() {
        return stockService.findAll().stream().map(StockResponse::from).toList();
    }

    @GetMapping("/low")
    public List<StockResponse> low() {
        return stockService.findLowStock().stream().map(StockResponse::from).toList();
    }

    /** Creates the row if this warehouse/product pair has none yet. */
    @PutMapping
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_MANAGER')")
    public StockResponse setQuantity(@Valid @RequestBody StockAdjustRequest req) {
        return StockResponse.from(stockService.setQuantity(req));
    }
}
