package com.abhinav.warehouse;

import com.abhinav.warehouse.dto.OrderItemRequest;
import com.abhinav.warehouse.dto.PlaceOrderRequest;
import com.abhinav.warehouse.entity.*;
import com.abhinav.warehouse.repository.*;
import com.abhinav.warehouse.service.FulfillmentService;
import com.abhinav.warehouse.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The demo. 30 threads try to buy the last 12 units of one product at the same
 * instant. Exactly 12 must succeed and the stock must land on 0 — never below.
 *
 * Run it once with locking-strategy: PESSIMISTIC and once with OPTIMISTIC.
 * Both should pass. Then delete the locking from StockRepository and watch it
 * fail — that failure is the thing worth screenshotting for the README.
 *
 * Requires the MySQL container to be up.
 */
@SpringBootTest
class ConcurrentFulfillmentTest {

    private static final int THREADS = 30;
    private static final int UNITS_AVAILABLE = 12;

    @Autowired OrderService orderService;
    @Autowired FulfillmentService fulfillmentService;
    @Autowired WarehouseRepository warehouseRepository;
    @Autowired ProductRepository productRepository;
    @Autowired StockRepository stockRepository;
    @Autowired OrderRepository orderRepository;
    @Autowired FulfillmentLogRepository logRepository;

    Long productId;
    Long stockId;

    @BeforeEach
    void setUp() {
        // Fresh, isolated fixtures so the seeded demo data cannot interfere.
        logRepository.deleteAll();
        orderRepository.deleteAll();

        Warehouse wh = warehouseRepository.save(Warehouse.builder()
                .name("Contention Test WH " + System.nanoTime())
                .city("Hyderabad")
                .latitude(new BigDecimal("17.385000"))
                .longitude(new BigDecimal("78.486700"))
                .capacity(1000).active(true).build());

        Product product = productRepository.save(Product.builder()
                .sku("SKU-CONTENTION-" + System.nanoTime())
                .name("Contested Widget")
                .category("Test").lowStockThreshold(2).build());

        Stock stock = stockRepository.save(Stock.builder()
                .warehouse(wh).product(product).quantity(UNITS_AVAILABLE).build());

        this.productId = product.getId();
        this.stockId = stock.getId();
    }

    @Test
    @DisplayName("30 simultaneous orders against 12 units: 12 succeed, 18 fail, nothing oversells")
    void doesNotOversell() throws Exception {
        var pool = Executors.newFixedThreadPool(THREADS);
        // Every thread parks here, then all are released together. Without this
        // the threads trickle in and never actually contend.
        var startGate = new CountDownLatch(1);
        var doneGate = new CountDownLatch(THREADS);

        AtomicInteger fulfilled = new AtomicInteger();
        AtomicInteger failed = new AtomicInteger();
        AtomicInteger totalAttempts = new AtomicInteger();

        for (int i = 0; i < THREADS; i++) {
            final int n = i;
            pool.submit(() -> {
                try {
                    startGate.await();
                    Long orderId = orderService.placeOrder(new PlaceOrderRequest(
                            "Buyer " + n,
                            new BigDecimal("17.385000"),
                            new BigDecimal("78.486700"),
                            List.of(new OrderItemRequest(productId, 1))));

                    var result = fulfillmentService.fulfil(orderId);
                    totalAttempts.addAndGet(result.attemptsUsed());
                    if (result.status() == OrderStatus.FULFILLED) fulfilled.incrementAndGet();
                    else failed.incrementAndGet();

                } catch (Exception e) {
                    failed.incrementAndGet();
                } finally {
                    doneGate.countDown();
                }
            });
        }

        startGate.countDown();
        assertThat(doneGate.await(90, TimeUnit.SECONDS)).isTrue();
        pool.shutdown();

        int remaining = stockRepository.findById(stockId).orElseThrow().getQuantity();

        System.out.printf("""
                
                ==== CONCURRENCY RESULT ====
                threads fired      : %d
                units available    : %d
                orders fulfilled   : %d
                orders failed      : %d
                stock remaining    : %d
                total attempts used: %d  (> %d means retries happened)
                ============================
                %n""",
                THREADS, UNITS_AVAILABLE, fulfilled.get(), failed.get(),
                remaining, totalAttempts.get(), THREADS);

        // The invariant. If this ever goes negative, the product oversold.
        assertThat(remaining).isZero();
        assertThat(fulfilled.get()).isEqualTo(UNITS_AVAILABLE);
        assertThat(failed.get()).isEqualTo(THREADS - UNITS_AVAILABLE);
    }
}
