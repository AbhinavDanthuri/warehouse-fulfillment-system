package com.abhinav.warehouse.service;

import com.abhinav.warehouse.dto.ProductRequest;
import com.abhinav.warehouse.entity.Product;
import com.abhinav.warehouse.exception.NotFoundException;
import com.abhinav.warehouse.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public List<Product> findAll() {
        return productRepository.findAll();
    }

    public Product findById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("product", id));
    }

    @Transactional
    public Product create(ProductRequest req) {
        if (productRepository.existsBySku(req.sku())) {
            throw new IllegalArgumentException("SKU already exists: " + req.sku());
        }
        return productRepository.save(Product.builder()
                .sku(req.sku()).name(req.name())
                .category(req.category())
                .lowStockThreshold(req.lowStockThreshold())
                .build());
    }

    @Transactional
    public Product update(Long id, ProductRequest req) {
        Product p = findById(id);
        // SKU is deliberately not editable — it is the business key that stock
        // rows, orders, and any external system are keyed on.
        p.setName(req.name());
        p.setCategory(req.category());
        p.setLowStockThreshold(req.lowStockThreshold());
        return p;
    }
}
