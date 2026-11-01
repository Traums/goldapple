package com.example.goldapple.mapper;

import com.example.goldapple.dto.ProductResponse;
import com.example.goldapple.entity.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getSku(),
                product.getName(),
                product.getBrand(),
                product.getCategory(),
                product.getPrice(),
                product.getStockQuantity(),
                product.getStatus(),
                product.getImageUrl(),
                product.getCreatedAt()
        );
    }
}
