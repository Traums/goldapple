package com.example.goldapple.controller;

import com.example.goldapple.dto.CustomerResponse;
import com.example.goldapple.dto.ErrorResponse;
import com.example.goldapple.dto.OrderSummaryResponse;
import com.example.goldapple.dto.SaveCustomerRequest;
import com.example.goldapple.service.CustomerService;
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

@Tag(name = "Клиенты")
@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;
    private final OrderService orderService;

    @Operation(summary = "Получить список клиентов")
    @ApiResponse(responseCode = "200", description = "Клиенты",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = CustomerResponse.class))))
    @GetMapping
    public List<CustomerResponse> getCustomers() {
        return customerService.getCustomers();
    }

    @Operation(summary = "Создать клиента")
    @ApiResponse(responseCode = "201", description = "Созданный клиент",
            content = @Content(schema = @Schema(implementation = CustomerResponse.class)))
    @ApiResponse(responseCode = "400", description = "Некорректное тело запроса",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PostMapping
    public ResponseEntity<CustomerResponse> create(@Valid @RequestBody SaveCustomerRequest request) {
        CustomerResponse response = customerService.create(request);
        return ResponseEntity.created(URI.create("/api/v1/customers/" + response.id())).body(response);
    }

    @Operation(summary = "Изменить клиента")
    @ApiResponse(responseCode = "200", description = "Обновлённый клиент",
            content = @Content(schema = @Schema(implementation = CustomerResponse.class)))
    @ApiResponse(responseCode = "400", description = "Некорректное тело запроса",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Клиент не найден",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PatchMapping("/{customerId}")
    public CustomerResponse update(
            @Parameter(description = "Идентификатор клиента", example = "1")
            @PathVariable Long customerId,
            @Valid @RequestBody SaveCustomerRequest request
    ) {
        return customerService.update(customerId, request);
    }

    @Operation(summary = "Получить клиента")
    @ApiResponse(responseCode = "200", description = "Клиент",
            content = @Content(schema = @Schema(implementation = CustomerResponse.class)))
    @ApiResponse(responseCode = "404", description = "Клиент не найден",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @GetMapping("/{customerId}")
    public CustomerResponse getCustomer(
            @Parameter(description = "Идентификатор клиента", example = "1")
            @PathVariable Long customerId
    ) {
        return customerService.getCustomer(customerId);
    }

    @Operation(summary = "Получить заказы клиента")
    @ApiResponse(responseCode = "200", description = "Заказы клиента",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = OrderSummaryResponse.class))))
    @ApiResponse(responseCode = "404", description = "Клиент не найден",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @GetMapping("/{customerId}/orders")
    public List<OrderSummaryResponse> getOrders(
            @Parameter(description = "Идентификатор клиента", example = "1")
            @PathVariable Long customerId
    ) {
        return orderService.getCustomerOrders(customerId);
    }
}
