package com.abhinav.warehouse.service;

import com.abhinav.warehouse.dto.StockAdjustRequest;
import com.abhinav.warehouse.entity.Stock;
import com.abhinav.warehouse.exception.NotFoundException;
import com.abhinav.warehouse.repository.ProductRepository;
import com.abhinav.warehouse.repository.StockRepository;
import com.abhinav.warehouse.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StockService {

    private final StockRepository stockRepository;
    private final WarehouseRepository warehouseRepository;
    private final ProductRepository productRepository;

    public List<Stock> findAll() {
        return stockRepository.findAll();
    }

    public List<Stock> findLowStock() {
        return stockRepository.findLowStock();
    }

    /**
     * Manual adjustment, and it takes the same lock the fulfillment engine does.
     *
     * A manager correcting a count while orders are being fulfilled is exactly
     * the race the engine guards against — leaving this path unlocked would open
     * the hole everywhere else closes.
     */
    @Transactional
    public Stock setQuantity(StockAdjustRequest req) {
        return stockRepository.findForUpdate(req.warehouseId(), req.productId())
                .map(existing -> {
                    existing.setQuantity(req.quantity());
                    return existing;
                })
                .orElseGet(() -> createRow(req));
    }

    private Stock createRow(StockAdjustRequest req) {
        var warehouse = warehouseRepository.findById(req.warehouseId())
                .orElseThrow(() -> new NotFoundException("warehouse", req.warehouseId()));
        var product = productRepository.findById(req.productId())
                .orElseThrow(() -> new NotFoundException("product", req.productId()));

        return stockRepository.save(Stock.builder()
                .warehouse(warehouse).product(product)
                .quantity(req.quantity())
                .build());
    }
}
