package com.abhinav.warehouse.service;

import com.abhinav.warehouse.config.FulfillmentConfig;
import com.abhinav.warehouse.config.FulfillmentConfig.LockingStrategy;
import com.abhinav.warehouse.dto.TraceEntry;
import com.abhinav.warehouse.entity.*;
import com.abhinav.warehouse.exception.InsufficientStockException;
import com.abhinav.warehouse.repository.OrderRepository;
import com.abhinav.warehouse.repository.StockCandidate;
import com.abhinav.warehouse.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

/**
 * ONE attempt at fulfilling an entire order, in ONE transaction.
 *
 * Why the whole order and not one item at a time: if item 2 cannot be
 * fulfilled, item 1's decrement has to be undone. Letting the transaction roll
 * back does that for free. The alternative — compensating writes — is how you
 * end up with phantom stock.
 *
 * REQUIRES_NEW because the caller (FulfillmentService) deliberately runs
 * OUTSIDE a transaction so it can retry. You cannot retry an optimistic
 * failure inside the transaction that failed; it is already marked
 * rollback-only.
 */
@Service
@RequiredArgsConstructor
public class FulfillmentAttemptService {

    private final OrderRepository orderRepository;
    private final StockRepository stockRepository;
    private final DistanceCalculator distanceCalculator;
    private final FulfillmentConfig config;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void attempt(Long orderId, int attemptNo, List<TraceEntry> trace) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("no order " + orderId));

        order.setStatus(OrderStatus.FULFILLING);

        for (OrderItem item : order.getItems()) {
            fulfilItem(order, item, attemptNo, trace);
        }

        order.setStatus(OrderStatus.FULFILLED);
        order.setFailureReason(null);
    }

    private void fulfilItem(Order order, OrderItem item, int attemptNo, List<TraceEntry> trace) {
        int needed = item.getQuantity();
        Long productId = item.getProduct().getId();

        // Unlocked read purely to rank candidates. The numbers here are a
        // snapshot and may already be stale — that is fine, they are re-checked
        // under lock below. Ranking does not need to be correct, only the
        // decrement does.
        //
        // These are projections, not entities, precisely so they do NOT land in
        // the persistence context and shadow the locked read further down.
        List<StockCandidate> ranked = stockRepository.findCandidates(productId, needed)
                .stream()
                .sorted(Comparator.comparing(c -> distanceKm(order, c)))
                .toList();

        if (ranked.isEmpty()) {
            trace.add(new TraceEntry(item.getId(), null,
                    FulfillmentOutcome.FAILED_NO_WAREHOUSE, 0, needed, null, attemptNo,
                    "no active warehouse holds " + needed + " units"));
            throw new InsufficientStockException(
                    "product " + productId + ": no warehouse can supply " + needed);
        }

        for (StockCandidate candidate : ranked) {
            Long warehouseId = candidate.getWarehouseId();
            BigDecimal distance = distanceKm(order, candidate);

            // Re-read the row, this time defended. Everything before this point
            // was advisory. This is the first and only managed entity.
            Stock stock = (config.getLockingStrategy() == LockingStrategy.PESSIMISTIC)
                    ? stockRepository.findForUpdate(warehouseId, productId).orElseThrow()
                    : stockRepository.findByWarehouseIdAndProductId(warehouseId, productId).orElseThrow();

            if (stock.getQuantity() < needed) {
                // Someone drained it between the ranking read and this one.
                // Exactly the race this project exists to demonstrate.
                trace.add(new TraceEntry(item.getId(), warehouseId,
                        FulfillmentOutcome.SKIPPED_NO_STOCK, stock.getQuantity(), needed,
                        distance, attemptNo, "stock dropped after ranking read"));
                continue;
            }

            stock.decrement(needed);

            trace.add(new TraceEntry(item.getId(), warehouseId,
                    FulfillmentOutcome.SELECTED, stock.getQuantity(), needed,
                    distance, attemptNo,
                    config.getLockingStrategy() + " lock held"));
            return;
        }

        trace.add(new TraceEntry(item.getId(), null,
                FulfillmentOutcome.FAILED_NO_WAREHOUSE, 0, needed, null, attemptNo,
                "all " + ranked.size() + " candidates exhausted"));
        throw new InsufficientStockException(
                "product " + productId + ": all candidates drained under contention");
    }

    private BigDecimal distanceKm(Order order, StockCandidate candidate) {
        return distanceCalculator.kmBetween(
                order.getDestLatitude(), order.getDestLongitude(),
                candidate.getLatitude(), candidate.getLongitude());
    }
}