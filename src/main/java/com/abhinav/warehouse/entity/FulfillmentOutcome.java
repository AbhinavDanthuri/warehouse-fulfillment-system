package com.abhinav.warehouse.entity;

public enum FulfillmentOutcome {
    SELECTED,
    SKIPPED_NO_STOCK,
    SKIPPED_INACTIVE,
    LOCK_CONFLICT_RETRY,
    FAILED_NO_WAREHOUSE
}
