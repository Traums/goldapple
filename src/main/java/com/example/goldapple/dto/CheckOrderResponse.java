package com.example.goldapple.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Результат проверки заказа")
public record CheckOrderResponse(
        @Schema(description = "Можно ли оформить заказ", example = "true")
        boolean allowed,
        @Schema(description = "Причина отказа. Заполняется, когда allowed равен false", example = "INSUFFICIENT_STOCK", nullable = true)
        DeclineReason reason,
        @Schema(description = "Сумма заказа в рублях. Заполняется, когда заказ можно оформить", example = "1081.00", nullable = true)
        BigDecimal totalAmount
) {
}
