package com.vms.order.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.vms.order.entity.Order;
import com.vms.order.entity.OrderItem;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderResponse(OrderView order) {

    public static OrderResponse from(Order order) {
        var items = order.getItems().stream().map(OrderItemView::from).toList();
        return new OrderResponse(new OrderView(
                order.getId(),
                order.getMachine().getId(),
                order.getStatus(),
                items,
                order.getTotalAmount(),
                order.getExpiresAt(),
                order.getCreatedAt()
        ));
    }

    public record OrderView(
            Long id,
            @JsonProperty("machine_id") Long machineId,
            String status,
            List<OrderItemView> items,
            @JsonProperty("total_amount") BigDecimal totalAmount,
            @JsonProperty("expires_at") Instant expiresAt,
            @JsonProperty("created_at") Instant createdAt
    ) {
    }

    public record OrderItemView(
            @JsonProperty("product_name") String productName,
            int quantity,
            @JsonProperty("unit_price") BigDecimal unitPrice
    ) {
        static OrderItemView from(OrderItem item) {
            return new OrderItemView(item.getProduct().getName(), item.getQuantity(), item.getUnitPrice());
        }
    }

    public record PaymentView(String status, @JsonProperty("payment_method") String paymentMethod) {
    }
}
