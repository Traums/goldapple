package com.example.goldapple.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Schema(description = "Данные для создания или проверки заказа")
public record CreateOrderRequest(
        @NotNull(message = "обязательно")
        @Schema(description = "Идентификатор клиента", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        Long customerId,
        @NotEmpty(message = "заказ должен содержать хотя бы один товар")
        @Valid
        @Schema(description = "Позиции заказа", requiredMode = Schema.RequiredMode.REQUIRED)
        List<CreateOrderItemRequest> items
) {
}
