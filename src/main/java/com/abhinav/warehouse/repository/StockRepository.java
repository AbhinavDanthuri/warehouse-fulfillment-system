package com.abhinav.warehouse.repository;

import com.abhinav.warehouse.entity.Stock;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Both locking strategies live here on purpose — the README compares them.
 */
public interface StockRepository extends JpaRepository<Stock, Long> {

    /** No lock. Safe for reads/dashboards, NOT safe for decrementing. */
    Optional<Stock> findByWarehouseIdAndProductId(Long warehouseId, Long productId);

    /**
     * Pessimistic: issues SELECT ... FOR UPDATE. A second transaction asking
     * for the same row blocks here until the first commits or rolls back.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
           SELECT s FROM Stock s
           WHERE s.warehouse.id = :warehouseId AND s.product.id = :productId
           """)
    Optional<Stock> findForUpdate(@Param("warehouseId") Long warehouseId,
                                  @Param("productId") Long productId);

    /**
     * Candidate rows for a product, as a projection rather than entities — see
     * StockCandidate for why that distinction matters.
     */
    @Query("""
           SELECT w.id AS warehouseId, s.quantity AS quantity,
                  w.latitude AS latitude, w.longitude AS longitude
           FROM Stock s JOIN s.warehouse w
           WHERE s.product.id = :productId AND s.quantity >= :needed AND w.active = true
           """)
    List<StockCandidate> findCandidates(@Param("productId") Long productId,
                                        @Param("needed") int needed);

    /** Rows sitting at or below the product's configured threshold. */
    @Query("""
           SELECT s FROM Stock s
           JOIN FETCH s.product p
           JOIN FETCH s.warehouse w
           WHERE s.quantity <= p.lowStockThreshold
           """)
    List<Stock> findLowStock();
}