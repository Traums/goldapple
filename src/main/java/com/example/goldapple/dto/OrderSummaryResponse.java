package com.example.goldapple.dto;

import com.example.goldapple.entity.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;

@Schema(description = "Заказ без состава")
public record OrderSummaryResponse(
        @Schema(description = "Идентификатор заказа", example = "3")
        Long id,
        @Schema(description = "Идентификатор клиента", example = "1")
        Long customerId,
        @Schema(description = "Статус заказа", example = "NEW")
        OrderStatus status,
        @Schema(description = "Сумма заказа в рублях", example = "1630.00")
        BigDecimal totalAmount,
        @Schema(description = "Время создания, UTC", example = "2025-09-18T13:05:00Z")
        Instant createdAt
) {
}
