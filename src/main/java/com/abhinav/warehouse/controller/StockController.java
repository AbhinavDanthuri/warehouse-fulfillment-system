package com.abhinav.warehouse.controller;

import com.abhinav.warehouse.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stock")
@RequiredArgsConstructor
public class StockController {

    private final StockRepository stockRepository;

    @GetMapping
    public List<Map<String, Object>> all() {
        return stockRepository.findAll().stream()
                .map(s -> Map.<String, Object>of(
                        "warehouse", s.getWarehouse().getName(),
                        "sku", s.getProduct().getSku(),
                        "quantity", s.getQuantity(),
                        "version", s.getVersion()))
                .toList();
    }

    @GetMapping("/low")
    public List<Map<String, Object>> low() {
        return stockRepository.findLowStock().stream()
                .map(s -> Map.<String, Object>of(
                        "warehouse", s.getWarehouse().getName(),
                        "sku", s.getProduct().getSku(),
                        "quantity", s.getQuantity(),
                        "threshold", s.getProduct().getLowStockThreshold()))
                .toList();
    }
}
