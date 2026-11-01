package com.example.goldapple.controller;

import com.example.goldapple.dto.CheckOrderResponse;
import com.example.goldapple.dto.CreateOrderRequest;
import com.example.goldapple.dto.ErrorResponse;
import com.example.goldapple.dto.OrderResponse;
import com.example.goldapple.dto.UpdateOrderStatusRequest;
import com.example.goldapple.service.OrderService;
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

@Tag(name = "Заказы")
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "Получить все заказы")
    @ApiResponse(responseCode = "200", description = "Заказы",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = OrderResponse.class))))
    @GetMapping
    public List<OrderResponse> getOrders() {
        return orderService.getOrders();
    }

    @Operation(summary = "Получить заказ")
    @ApiResponse(responseCode = "200", description = "Заказ",
            content = @Content(schema = @Schema(implementation = OrderResponse.class)))
    @ApiResponse(responseCode = "404", description = "Заказ не найден",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @GetMapping("/{orderId}")
    public OrderResponse getOrder(
            @Parameter(description = "Идентификатор заказа", example = "1")
            @PathVariable Long orderId
    ) {
        return orderService.getOrder(orderId);
    }

    @Operation(summary = "Проверить, можно ли оформить заказ")
    @ApiResponse(responseCode = "200", description = "Результат проверки. allowed = false означает, что заказ оформить нельзя",
            content = @Content(schema = @Schema(implementation = CheckOrderResponse.class)))
    @ApiResponse(responseCode = "400", description = "Некорректное тело запроса",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Клиент или товар не найден",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PostMapping("/check")
    public CheckOrderResponse check(@Valid @RequestBody CreateOrderRequest request) {
        return orderService.check(request);
    }

    @Operation(summary = "Создать заказ")
    @ApiResponse(responseCode = "201", description = "Созданный заказ",
            content = @Content(schema = @Schema(implementation = OrderResponse.class)))
    @ApiResponse(responseCode = "400", description = "Некорректное тело запроса",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Клиент или товар не найден",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "Заказ нельзя оформить",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PostMapping
    public ResponseEntity<OrderResponse> create(@Valid @RequestBody CreateOrderRequest request) {
        OrderResponse response = orderService.create(request);
        return ResponseEntity.created(URI.create("/api/v1/orders/" + response.id())).body(response);
    }

    @Operation(summary = "Изменить состав заказа", description = "Доступно только для нового заказа")
    @ApiResponse(responseCode = "200", description = "Обновлённый заказ",
            content = @Content(schema = @Schema(implementation = OrderResponse.class)))
    @ApiResponse(responseCode = "400", description = "Некорректное тело запроса",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Заказ, клиент или товар не найден",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "Заказ нельзя изменить",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PatchMapping("/{orderId}")
    public OrderResponse update(
            @Parameter(description = "Идентификатор заказа", example = "3")
            @PathVariable Long orderId,
            @Valid @RequestBody CreateOrderRequest request
    ) {
        return orderService.update(orderId, request);
    }

    @Operation(summary = "Изменить статус заказа")
    @ApiResponse(responseCode = "200", description = "Заказ с новым статусом",
            content = @Content(schema = @Schema(implementation = OrderResponse.class)))
    @ApiResponse(responseCode = "400", description = "Некорректное тело запроса",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Заказ не найден",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "Такой переход статуса недоступен",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PatchMapping("/{orderId}/status")
    public OrderResponse changeStatus(
            @Parameter(description = "Идентификатор заказа", example = "3")
            @PathVariable Long orderId,
            @Valid @RequestBody UpdateOrderStatusRequest request
    ) {
        return orderService.changeStatus(orderId, request);
    }

    @Operation(summary = "Отменить заказ")
    @ApiResponse(responseCode = "200", description = "Отменённый заказ",
            content = @Content(schema = @Schema(implementation = OrderResponse.class)))
    @ApiResponse(responseCode = "404", description = "Заказ не найден",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "Заказ нельзя отменить",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PostMapping("/{orderId}/cancel")
    public OrderResponse cancel(
            @Parameter(description = "Идентификатор заказа", example = "3")
            @PathVariable Long orderId
    ) {
        return orderService.cancel(orderId);
    }
}
