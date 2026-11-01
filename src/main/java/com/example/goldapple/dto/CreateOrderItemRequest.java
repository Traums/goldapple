package com.example.goldapple.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "Товар в новом заказе")
public record CreateOrderItemRequest(
        @NotNull(message = "обязательно")
        @Schema(description = "Идентификатор товара", example = "10", requiredMode = Schema.RequiredMode.REQUIRED)
        Long productId,
        @NotNull(message = "обязательно")
        @Positive(message = "должно быть больше 0")
        @Max(value = 20, message = "не больше 20")
        @Schema(description = "Количество", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer quantity
) {
}
