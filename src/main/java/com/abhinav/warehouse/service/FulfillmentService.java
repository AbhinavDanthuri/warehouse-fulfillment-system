package com.abhinav.warehouse.service;

import com.abhinav.warehouse.config.FulfillmentConfig;
import com.abhinav.warehouse.dto.TraceEntry;
import com.abhinav.warehouse.entity.FulfillmentOutcome;
import com.abhinav.warehouse.entity.OrderStatus;
import com.abhinav.warehouse.exception.InsufficientStockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * The retry loop. Note there is NO @Transactional on fulfil() — that is the
 * whole point. Each attempt gets its own transaction from
 * FulfillmentAttemptService; when one dies on a version clash, this method is
 * still alive to start a fresh one.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FulfillmentService {

    private final FulfillmentAttemptService attemptService;
    private final TraceWriter traceWriter;
    private final OrderStatusService orderStatusService;
    private final FulfillmentConfig config;

    public record Result(OrderStatus status, int attemptsUsed, String failureReason) {}

    public Result fulfil(Long orderId) {
        List<TraceEntry> trace = new ArrayList<>();
        int maxAttempts = Math.max(1, config.getMaxRetries());

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                attemptService.attempt(orderId, attempt, trace);
                traceWriter.persist(orderId, trace);
                return new Result(OrderStatus.FULFILLED, attempt, null);

            } catch (OptimisticLockingFailureException | CannotAcquireLockException e) {
                // Another transaction won the row. Nothing is broken — this is
                // the mechanism working. Everything this attempt did has been
                // rolled back, so we simply go again.
                trace.add(new TraceEntry(null, null,
                        FulfillmentOutcome.LOCK_CONFLICT_RETRY, null, null, null, attempt,
                        truncate(e.getClass().getSimpleName() + ": " + e.getMessage())));
               log.warn("order {} lost the row on attempt {}", orderId, attempt, e);
                backoff(attempt);

            } catch (InsufficientStockException e) {
                // Terminal. Retrying cannot conjure stock.
                orderStatusService.markFailed(orderId, e.getMessage());
                traceWriter.persist(orderId, trace);
                return new Result(OrderStatus.FAILED, attempt, e.getMessage());
            }
        }

        String reason = "gave up after " + maxAttempts + " attempts under contention";
        orderStatusService.markFailed(orderId, reason);
        traceWriter.persist(orderId, trace);
        return new Result(OrderStatus.FAILED, maxAttempts, reason);
    }
     private static String truncate(String s) {
        return s == null ? null : (s.length() > 250 ? s.substring(0, 250) : s);
    }

    private void backoff(int attempt) {
        try {
            // Jittered, so retrying threads do not collide again in lockstep.
            long base = config.getRetryBackoffMs() * attempt;
            Thread.sleep(base + (long) (Math.random() * base));
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }
}
