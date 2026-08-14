package com.abhinav.warehouse.repository;

import java.math.BigDecimal;

/**
 * Read-only projection for ranking candidates.
 *
 * Deliberately NOT the Stock entity. Loading entities here puts them in the
 * persistence context, and a later findForUpdate() would then return that
 * cached instance instead of the freshly locked row — you would hold a real
 * database lock over stale in-memory state, and the flush would fail the
 * version check. Projections never enter the identity map, so the locked read
 * stays authoritative.
 */
public interface StockCandidate {
    Long getWarehouseId();
    Integer getQuantity();
    BigDecimal getLatitude();
    BigDecimal getLongitude();
}