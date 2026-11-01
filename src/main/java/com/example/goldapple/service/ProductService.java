package com.example.goldapple.service;

import com.example.goldapple.dto.ProductResponse;
import com.example.goldapple.dto.SaveProductRequest;
import com.example.goldapple.entity.Product;
import com.example.goldapple.exception.ApiException;
import com.example.goldapple.exception.ErrorCode;
import com.example.goldapple.mapper.ProductMapper;
import com.example.goldapple.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public List<ProductResponse> getProducts() {
        return productRepository.findAllByOrderByIdAsc().stream()
                .map(productMapper::toResponse)
                .toList();
    }

    public ProductResponse getProduct(Long productId) {
        return productMapper.toResponse(getExistingProduct(productId));
    }

    @Transactional
    public ProductResponse create(SaveProductRequest request) {
        String sku = request.sku().trim();
        if (productRepository.existsBySku(sku)) {
            throw skuTaken();
        }
        Product product = new Product();
        apply(product, request, sku);
        product.setCreatedAt(Instant.now());
        return productMapper.toResponse(productRepository.save(product));
    }

    @Transactional
    public ProductResponse update(Long productId, SaveProductRequest request) {
        Product product = getExistingProduct(productId);
        String sku = request.sku().trim();
        if (productRepository.existsBySkuAndIdNot(sku, productId)) {
            throw skuTaken();
        }
        apply(product, request, sku);
        return productMapper.toResponse(product);
    }

    private void apply(Product product, SaveProductRequest request, String sku) {
        product.setSku(sku);
        product.setName(request.name().trim());
        product.setBrand(request.brand().trim());
        product.setCategory(request.category());
        product.setPrice(request.price().setScale(2, RoundingMode.HALF_UP));
        product.setStockQuantity(request.stockQuantity());
        product.setStatus(request.status());
        product.setImageUrl(request.imageUrl().trim());
    }

    private ApiException skuTaken() {
        return new ApiException(HttpStatus.CONFLICT, ErrorCode.PRODUCT_SKU_TAKEN, "Артикул уже занят");
    }

    public Product getExistingProduct(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, ErrorCode.PRODUCT_NOT_FOUND, "Товар не найден"));
    }
}
