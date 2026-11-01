package com.example.goldapple.dto;

import com.example.goldapple.entity.CustomerStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.time.LocalDate;

@Schema(description = "Клиент")
public record CustomerResponse(
        @Schema(description = "Идентификатор клиента", example = "1")
        Long id,
        @Schema(description = "Имя", example = "Anastasia")
        String firstName,
        @Schema(description = "Фамилия", example = "Cherevko")
        String lastName,
        @Schema(description = "Телефон", example = "+79031112233")
        String phone,
        @Schema(description = "Дата рождения", example = "1994-03-12")
        LocalDate birthDate,
        @Schema(description = "Статус клиента", example = "ACTIVE")
        CustomerStatus status,
        @Schema(description = "Время создания, UTC", example = "2024-01-15T10:00:00Z")
        Instant createdAt
) {
}
