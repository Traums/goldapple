package com.example.goldapple.dto;

import com.example.goldapple.entity.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Новый статус заказа")
public record UpdateOrderStatusRequest(
        @NotNull(message = "обязательно")
        @Schema(description = "Статус заказа", example = "PAID", requiredMode = Schema.RequiredMode.REQUIRED)
        OrderStatus status
) {
}
