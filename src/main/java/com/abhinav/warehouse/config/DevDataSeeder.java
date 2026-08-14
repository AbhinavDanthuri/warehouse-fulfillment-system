package com.abhinav.warehouse.config;

import com.abhinav.warehouse.entity.*;
import com.abhinav.warehouse.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/** Seeds three Hyderabad-area warehouses and one product so the demo runs. */
@Component
@RequiredArgsConstructor
public class DevDataSeeder implements CommandLineRunner {

    private final WarehouseRepository warehouseRepository;
    private final ProductRepository productRepository;
    private final StockRepository stockRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (warehouseRepository.count() > 0) return;

        Warehouse gachibowli = save("Gachibowli DC", "Hyderabad", "17.440800", "78.348900", 5000);
        Warehouse uppal      = save("Uppal Hub",     "Hyderabad", "17.405600", "78.559700", 3000);
        Warehouse medchal    = save("Medchal Depot", "Hyderabad", "17.629300", "78.481500", 2000);

        Product laptop = productRepository.save(Product.builder()
                .sku("SKU-LAPTOP-01").name("14-inch Laptop")
                .category("Electronics").lowStockThreshold(10).build());

        // Deliberately small numbers — the concurrency test needs the stock to
        // run out so that "no overselling" is actually being tested.
        stock(gachibowli, laptop, 10);
        stock(uppal, laptop, 5);
        stock(medchal, laptop, 0);
    }

    private Warehouse save(String name, String city, String lat, String lon, int cap) {
        return warehouseRepository.save(Warehouse.builder()
                .name(name).city(city)
                .latitude(new BigDecimal(lat)).longitude(new BigDecimal(lon))
                .capacity(cap).active(true).build());
    }

    private void stock(Warehouse w, Product p, int qty) {
        stockRepository.save(Stock.builder().warehouse(w).product(p).quantity(qty).build());
    }
}
