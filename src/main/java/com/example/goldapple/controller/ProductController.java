package com.example.goldapple.controller;

import com.example.goldapple.dto.ErrorResponse;
import com.example.goldapple.dto.ProductResponse;
import com.example.goldapple.dto.SaveProductRequest;
import com.example.goldapple.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@Tag(name = "Товары")
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @Operation(summary = "Получить каталог товаров")
    @ApiResponse(responseCode = "200", description = "Товары каталога",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = ProductResponse.class))))
    @GetMapping
    public List<ProductResponse> getProducts() {
        return productService.getProducts();
    }

    @Operation(summary = "Создать товар")
    @ApiResponse(responseCode = "201", description = "Созданный товар",
            content = @Content(schema = @Schema(implementation = ProductResponse.class)))
    @ApiResponse(responseCode = "400", description = "Некорректное тело запроса",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "Артикул уже занят",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PostMapping
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody SaveProductRequest request) {
        ProductResponse response = productService.create(request);
        return ResponseEntity.created(URI.create("/api/v1/products/" + response.id())).body(response);
    }

    @Operation(summary = "Изменить товар")
    @ApiResponse(responseCode = "200", description = "Обновлённый товар",
            content = @Content(schema = @Schema(implementation = ProductResponse.class)))
    @ApiResponse(responseCode = "400", description = "Некорректное тело запроса",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Товар не найден",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "Артикул уже занят",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PatchMapping("/{productId}")
    public ProductResponse update(
            @Parameter(description = "Идентификатор товара", example = "1")
            @PathVariable Long productId,
            @Valid @RequestBody SaveProductRequest request
    ) {
        return productService.update(productId, request);
    }

    @Operation(summary = "Получить товар")
    @ApiResponse(responseCode = "200", description = "Товар",
            content = @Content(schema = @Schema(implementation = ProductResponse.class)))
    @ApiResponse(responseCode = "404", description = "Товар не найден",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @GetMapping("/{productId}")
    public ProductResponse getProduct(
            @Parameter(description = "Идентификатор товара", example = "1")
            @PathVariable Long productId
    ) {
        return productService.getProduct(productId);
    }
}
