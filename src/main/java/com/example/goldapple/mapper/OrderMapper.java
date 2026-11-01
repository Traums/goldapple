package com.example.goldapple.mapper;

import com.example.goldapple.dto.OrderItemResponse;
import com.example.goldapple.dto.OrderResponse;
import com.example.goldapple.dto.OrderSummaryResponse;
import com.example.goldapple.entity.Order;
import com.example.goldapple.entity.OrderItem;
import com.example.goldapple.entity.Product;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Component
public class OrderMapper {

    public OrderSummaryResponse toSummary(Order order) {
        return new OrderSummaryResponse(
                order.getId(),
                order.getCustomerId(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getCreatedAt()
        );
    }

    public OrderResponse toResponse(Order order, List<OrderItem> items, Map<Long, Product> products) {
        List<OrderItemResponse> itemResponses = items.stream()
                .map(item -> toItem(item, products.get(item.getProductId())))
                .toList();
        return new OrderResponse(
                order.getId(),
                order.getCustomerId(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getCreatedAt(),
                itemResponses
        );
    }

    private OrderItemResponse toItem(OrderItem item, Product product) {
        BigDecimal lineAmount = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
        return new OrderItemResponse(
                item.getId(),
                item.getProductId(),
                product.getName(),
                item.getQuantity(),
                item.getPrice(),
                lineAmount
        );
    }
}
