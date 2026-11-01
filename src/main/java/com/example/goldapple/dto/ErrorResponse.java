package com.example.goldapple.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Ответ с ошибкой")
public record ErrorResponse(
        @Schema(description = "Время ошибки, UTC", example = "2026-09-23T12:00:00Z")
        Instant timestamp,
        @Schema(description = "HTTP-код", example = "409")
        int status,
        @Schema(description = "Код ошибки приложения", example = "CUSTOMER_BLOCKED")
        String code,
        @Schema(description = "Текст ошибки", example = "Клиент заблокирован")
        String message
) {
}
