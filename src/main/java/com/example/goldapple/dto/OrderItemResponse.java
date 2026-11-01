package com.example.goldapple.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Позиция заказа")
public record OrderItemResponse(
        @Schema(description = "Идентификатор позиции", example = "4")
        Long id,
        @Schema(description = "Идентификатор товара", example = "6")
        Long productId,
        @Schema(description = "Название товара", example = "Бальзам для губ")
        String productName,
        @Schema(description = "Количество", example = "1")
        int quantity,
        @Schema(description = "Цена единицы на момент заказа, в рублях", example = "450.00")
        BigDecimal price,
        @Schema(description = "Сумма позиции в рублях", example = "450.00")
        BigDecimal lineAmount
) {
}
