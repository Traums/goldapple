package com.example.goldapple.service;

import com.example.goldapple.dto.CheckOrderResponse;
import com.example.goldapple.dto.CreateOrderItemRequest;
import com.example.goldapple.dto.CreateOrderRequest;
import com.example.goldapple.dto.DeclineReason;
import com.example.goldapple.dto.OrderResponse;
import com.example.goldapple.dto.OrderSummaryResponse;
import com.example.goldapple.dto.UpdateOrderStatusRequest;
import com.example.goldapple.entity.Customer;
import com.example.goldapple.entity.CustomerStatus;
import com.example.goldapple.entity.Order;
import com.example.goldapple.entity.OrderItem;
import com.example.goldapple.entity.OrderStatus;
import com.example.goldapple.entity.Product;
import com.example.goldapple.entity.ProductStatus;
import com.example.goldapple.exception.ApiException;
import com.example.goldapple.exception.ErrorCode;
import com.example.goldapple.mapper.OrderMapper;
import com.example.goldapple.repository.OrderItemRepository;
import com.example.goldapple.repository.OrderRepository;
import com.example.goldapple.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final CustomerService customerService;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderMapper orderMapper;

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrders() {
        return orderRepository.findAllByOrderByIdDesc().stream()
                .map(this::toDetailed)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OrderSummaryResponse> getCustomerOrders(Long customerId) {
        customerService.getExistingCustomer(customerId);
        return orderRepository.findByCustomerIdOrderByIdAsc(customerId).stream()
                .map(orderMapper::toSummary)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(Long orderId) {
        return toDetailed(getExistingOrder(orderId));
    }

    @Transactional(readOnly = true)
    public CheckOrderResponse check(CreateOrderRequest request) {
        Prepared prepared = prepare(request);
        if (!prepared.allowed()) {
            return new CheckOrderResponse(false, prepared.reason(), null);
        }
        return new CheckOrderResponse(true, null, prepared.totalAmount());
    }

    @Transactional
    public OrderResponse create(CreateOrderRequest request) {
        Prepared prepared = prepare(request);
        if (!prepared.allowed()) {
            throw declineException(prepared.reason());
        }

        Order order = new Order();
        order.setCustomerId(request.customerId());
        order.setStatus(OrderStatus.NEW);
        order.setTotalAmount(prepared.totalAmount());
        order.setCreatedAt(Instant.now());
        order = orderRepository.save(order);

        List<OrderItem> items = new ArrayList<>();
        for (PreparedLine line : prepared.lines()) {
            Product product = line.product();
            product.setStockQuantity(product.getStockQuantity() - line.quantity());
            if (product.getStockQuantity() == 0 && product.getStatus() == ProductStatus.ACTIVE) {
                product.setStatus(ProductStatus.OUT_OF_STOCK);
            }
            OrderItem item = new OrderItem();
            item.setOrderId(order.getId());
            item.setProductId(product.getId());
            item.setQuantity(line.quantity());
            item.setPrice(product.getPrice());
            items.add(item);
        }
        orderItemRepository.saveAll(items);
        return orderMapper.toResponse(order, items, productMap(prepared.lines()));
    }

    @Transactional
    public OrderResponse cancel(Long orderId) {
        Order order = getExistingOrder(orderId);
        if (order.getStatus() != OrderStatus.NEW) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    ErrorCode.ORDER_CANNOT_BE_CANCELLED,
                    "Заказ в статусе " + order.getStatus() + " нельзя отменить"
            );
        }

        restoreStock(orderItemRepository.findByOrderIdOrderByIdAsc(orderId));
        order.setStatus(OrderStatus.CANCELLED);
        return toDetailed(order);
    }

    @Transactional
    public OrderResponse update(Long orderId, CreateOrderRequest request) {
        Order order = getExistingOrder(orderId);
        if (order.getStatus() != OrderStatus.NEW) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    ErrorCode.ORDER_CANNOT_BE_EDITED,
                    "Редактировать можно только новый заказ"
            );
        }
        restoreStock(orderItemRepository.findByOrderIdOrderByIdAsc(orderId));
        orderItemRepository.deleteByOrderId(orderId);
        orderItemRepository.flush();

        Prepared prepared = prepare(request);
        if (!prepared.allowed()) {
            throw declineException(prepared.reason());
        }

        order.setCustomerId(request.customerId());
        order.setTotalAmount(prepared.totalAmount());
        List<OrderItem> items = new ArrayList<>();
        for (PreparedLine line : prepared.lines()) {
            Product product = line.product();
            product.setStockQuantity(product.getStockQuantity() - line.quantity());
            if (product.getStockQuantity() == 0 && product.getStatus() == ProductStatus.ACTIVE) {
                product.setStatus(ProductStatus.OUT_OF_STOCK);
            }
            OrderItem item = new OrderItem();
            item.setOrderId(order.getId());
            item.setProductId(product.getId());
            item.setQuantity(line.quantity());
            item.setPrice(product.getPrice());
            items.add(item);
        }
        orderItemRepository.saveAll(items);
        return orderMapper.toResponse(order, items, productMap(prepared.lines()));
    }

    private void restoreStock(List<OrderItem> items) {
        for (OrderItem item : items) {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, ErrorCode.PRODUCT_NOT_FOUND, "Товар не найден"));
            product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
            if (product.getStatus() == ProductStatus.OUT_OF_STOCK && product.getStockQuantity() > 0) {
                product.setStatus(ProductStatus.ACTIVE);
            }
        }
    }

    @Transactional
    public OrderResponse changeStatus(Long orderId, UpdateOrderStatusRequest request) {
        Order order = getExistingOrder(orderId);
        OrderStatus target = request.status();
        if (order.getStatus() == target) {
            return toDetailed(order);
        }
        if (target == OrderStatus.CANCELLED) {
            return cancel(orderId);
        }
        if (order.getStatus() == OrderStatus.NEW && target == OrderStatus.PAID
                || order.getStatus() == OrderStatus.PAID && target == OrderStatus.COMPLETED) {
            order.setStatus(target);
            return toDetailed(order);
        }
        throw new ApiException(
                HttpStatus.CONFLICT,
                ErrorCode.ORDER_STATUS_NOT_ALLOWED,
                "Статус заказа нельзя изменить с " + order.getStatus() + " на " + target
        );
    }

    private Prepared prepare(CreateOrderRequest request) {
        Customer customer = customerService.getExistingCustomer(request.customerId());
        if (customer.getStatus() == CustomerStatus.BLOCKED) {
            return Prepared.declined(DeclineReason.CUSTOMER_BLOCKED);
        }
        ensureUniqueProducts(request.items());

        List<PreparedLine> lines = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (CreateOrderItemRequest line : request.items()) {
            Product product = productRepository.findById(line.productId())
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, ErrorCode.PRODUCT_NOT_FOUND, "Товар не найден"));
            DeclineReason reason = declineReason(product, line.quantity());
            if (reason != null) {
                return Prepared.declined(reason);
            }
            lines.add(new PreparedLine(product, line.quantity()));
            total = total.add(product.getPrice().multiply(BigDecimal.valueOf(line.quantity())));
        }
        return Prepared.ok(total.setScale(2, RoundingMode.HALF_UP), lines);
    }

    private DeclineReason declineReason(Product product, int quantity) {
        if (product.getStatus() == ProductStatus.ARCHIVED) {
            return DeclineReason.PRODUCT_ARCHIVED;
        }
        if (product.getStatus() == ProductStatus.OUT_OF_STOCK || product.getStockQuantity() == 0) {
            return DeclineReason.PRODUCT_OUT_OF_STOCK;
        }
        if (quantity > product.getStockQuantity()) {
            return DeclineReason.INSUFFICIENT_STOCK;
        }
        return null;
    }

    private void ensureUniqueProducts(List<CreateOrderItemRequest> items) {
        Set<Long> productIds = new HashSet<>();
        for (CreateOrderItemRequest item : items) {
            if (!productIds.add(item.productId())) {
                throw new ApiException(HttpStatus.BAD_REQUEST, ErrorCode.DUPLICATE_PRODUCT, "В заказе повторяется товар");
            }
        }
    }

    private ApiException declineException(DeclineReason reason) {
        return switch (reason) {
            case CUSTOMER_BLOCKED -> new ApiException(HttpStatus.CONFLICT, ErrorCode.CUSTOMER_BLOCKED, "Клиент заблокирован");
            case PRODUCT_ARCHIVED -> new ApiException(HttpStatus.CONFLICT, ErrorCode.PRODUCT_ARCHIVED, "Товар снят с продажи");
            case PRODUCT_OUT_OF_STOCK -> new ApiException(HttpStatus.CONFLICT, ErrorCode.PRODUCT_OUT_OF_STOCK, "Товара нет в наличии");
            case INSUFFICIENT_STOCK -> new ApiException(HttpStatus.CONFLICT, ErrorCode.INSUFFICIENT_STOCK, "Недостаточно товара на складе");
        };
    }

    private Order getExistingOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, ErrorCode.ORDER_NOT_FOUND, "Заказ не найден"));
    }

    private OrderResponse toDetailed(Order order) {
        List<OrderItem> items = orderItemRepository.findByOrderIdOrderByIdAsc(order.getId());
        Map<Long, Product> products = productRepository.findAllById(
                items.stream().map(OrderItem::getProductId).collect(Collectors.toSet())
        ).stream().collect(Collectors.toMap(Product::getId, Function.identity()));
        return orderMapper.toResponse(order, items, products);
    }

    private Map<Long, Product> productMap(List<PreparedLine> lines) {
        return lines.stream().collect(Collectors.toMap(line -> line.product().getId(), PreparedLine::product));
    }

    private record PreparedLine(Product product, int quantity) {
    }

    private record Prepared(boolean allowed, DeclineReason reason, BigDecimal totalAmount, List<PreparedLine> lines) {

        private static Prepared ok(BigDecimal totalAmount, List<PreparedLine> lines) {
            return new Prepared(true, null, totalAmount, lines);
        }

        private static Prepared declined(DeclineReason reason) {
            return new Prepared(false, reason, null, List.of());
        }
    }
}
