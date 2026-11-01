package com.example.goldapple.dto;

import com.example.goldapple.entity.ProductCategory;
import com.example.goldapple.entity.ProductStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Schema(description = "Данные товара")
public record SaveProductRequest(
        @NotBlank(message = "обязательно")
        @Size(max = 32)
        @Pattern(regexp = "[A-Za-z0-9-]+", message = "только латиница, цифры и дефис")
        @Schema(description = "Артикул", example = "LL-CREAM-50", requiredMode = Schema.RequiredMode.REQUIRED)
        String sku,
        @NotBlank(message = "обязательно")
        @Size(max = 200)
        @Schema(description = "Название", example = "Крем для лица, 50 мл", requiredMode = Schema.RequiredMode.REQUIRED)
        String name,
        @NotBlank(message = "обязательно")
        @Size(max = 100)
        @Schema(description = "Бренд", example = "Lumière Lab", requiredMode = Schema.RequiredMode.REQUIRED)
        String brand,
        @NotNull(message = "обязательно")
        @Schema(description = "Категория", example = "SKINCARE", requiredMode = Schema.RequiredMode.REQUIRED)
        ProductCategory category,
        @NotNull(message = "обязательно")
        @DecimalMin(value = "0.00", message = "не может быть отрицательной")
        @Digits(integer = 13, fraction = 2, message = "не больше двух знаков после запятой")
        @Schema(description = "Цена в рублях", example = "1290.00", requiredMode = Schema.RequiredMode.REQUIRED)
        BigDecimal price,
        @NotNull(message = "обязательно")
        @Min(value = 0, message = "не может быть отрицательным")
        @Max(value = 100000, message = "не больше 100000")
        @Schema(description = "Остаток на складе", example = "12", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer stockQuantity,
        @NotNull(message = "обязательно")
        @Schema(description = "Статус товара", example = "ACTIVE", requiredMode = Schema.RequiredMode.REQUIRED)
        ProductStatus status,
        @NotBlank(message = "обязательно")
        @Size(max = 500)
        @Schema(description = "Ссылка на изображение", example = "https://images.unsplash.com/photo-1556228720-195a672e8a03?auto=format&fit=crop&w=900&q=80", requiredMode = Schema.RequiredMode.REQUIRED)
        String imageUrl
) {
}
