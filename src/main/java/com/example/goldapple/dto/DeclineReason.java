package com.example.goldapple.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Причина, по которой заказ собрать нельзя")
public enum DeclineReason {
    CUSTOMER_BLOCKED,
    PRODUCT_ARCHIVED,
    PRODUCT_OUT_OF_STOCK,
    INSUFFICIENT_STOCK
}
