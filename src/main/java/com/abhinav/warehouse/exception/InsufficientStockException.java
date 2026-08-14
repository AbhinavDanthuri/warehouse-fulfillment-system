package com.abhinav.warehouse.exception;

/** No active warehouse can satisfy an item. Terminal — retrying will not help. */
public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(String message) {
        super(message);
    }
}
