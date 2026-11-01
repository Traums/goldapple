package com.example.goldapple.exception;

public final class ErrorCode {

    public static final String CUSTOMER_NOT_FOUND = "CUSTOMER_NOT_FOUND";
    public static final String PRODUCT_NOT_FOUND = "PRODUCT_NOT_FOUND";
    public static final String PRODUCT_SKU_TAKEN = "PRODUCT_SKU_TAKEN";
    public static final String ORDER_NOT_FOUND = "ORDER_NOT_FOUND";
    public static final String CUSTOMER_BLOCKED = "CUSTOMER_BLOCKED";
    public static final String PRODUCT_ARCHIVED = "PRODUCT_ARCHIVED";
    public static final String PRODUCT_OUT_OF_STOCK = "PRODUCT_OUT_OF_STOCK";
    public static final String INSUFFICIENT_STOCK = "INSUFFICIENT_STOCK";
    public static final String DUPLICATE_PRODUCT = "DUPLICATE_PRODUCT";
    public static final String ORDER_CANNOT_BE_CANCELLED = "ORDER_CANNOT_BE_CANCELLED";
    public static final String ORDER_CANNOT_BE_EDITED = "ORDER_CANNOT_BE_EDITED";
    public static final String ORDER_STATUS_NOT_ALLOWED = "ORDER_STATUS_NOT_ALLOWED";
    public static final String VALIDATION_ERROR = "VALIDATION_ERROR";
    public static final String INTERNAL_ERROR = "INTERNAL_ERROR";

    private ErrorCode() {
    }
}
