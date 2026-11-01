package com.example.goldapple.dto;

import com.example.goldapple.entity.ProductCategory;
import com.example.goldapple.entity.ProductStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;

@Schema(description = "Товар")
public record ProductResponse(
        @Schema(description = "Идентификатор товара", example = "1")
        Long id,
        @Schema(description = "Артикул", example = "LL-SERUM-30")
        String sku,
        @Schema(description = "Название", example = "Сыворотка с витамином C, 30 мл")
        String name,
        @Schema(description = "Бренд", example = "Lumière Lab")
        String brand,
        @Schema(description = "Категория", example = "SKINCARE")
        ProductCategory category,
        @Schema(description = "Цена в рублях", example = "2490.00")
        BigDecimal price,
        @Schema(description = "Остаток на складе", example = "25")
        int stockQuantity,
        @Schema(description = "Статус товара", example = "ACTIVE")
        ProductStatus status,
        @Schema(description = "Ссылка на изображение товара")
        String imageUrl,
        @Schema(description = "Время создания, UTC", example = "2024-02-01T09:00:00Z")
        Instant createdAt
) {
}
