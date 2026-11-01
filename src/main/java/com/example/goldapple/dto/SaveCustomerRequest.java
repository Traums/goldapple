package com.example.goldapple.dto;

import com.example.goldapple.entity.CustomerStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@Schema(description = "Данные клиента")
public record SaveCustomerRequest(
        @NotBlank(message = "обязательно")
        @Size(max = 100)
        @Schema(description = "Имя", example = "Maria", requiredMode = Schema.RequiredMode.REQUIRED)
        String firstName,
        @NotBlank(message = "обязательно")
        @Size(max = 100)
        @Schema(description = "Фамилия", example = "Sokolova", requiredMode = Schema.RequiredMode.REQUIRED)
        String lastName,
        @NotBlank(message = "обязательно")
        @Size(max = 20)
        @Schema(description = "Телефон", example = "+79160001122", requiredMode = Schema.RequiredMode.REQUIRED)
        String phone,
        @PastOrPresent(message = "не может быть в будущем")
        @Schema(description = "Дата рождения", example = "1994-03-12")
        LocalDate birthDate,
        @NotNull(message = "обязательно")
        @Schema(description = "Статус клиента", example = "ACTIVE", requiredMode = Schema.RequiredMode.REQUIRED)
        CustomerStatus status
) {
}
