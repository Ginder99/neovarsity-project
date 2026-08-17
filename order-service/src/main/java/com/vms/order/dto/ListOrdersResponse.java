package com.vms.order.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.vms.order.entity.Order;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record ListOrdersResponse(List<OrderSummary> orders, @JsonProperty("next_cursor") String nextCursor) {

    public static ListOrdersResponse from(List<Order> orders) {
        return new ListOrdersResponse(orders.stream().map(OrderSummary::from).toList(), null);
    }

    public record OrderSummary(
            Long id,
            String status,
            @JsonProperty("total_amount") BigDecimal totalAmount,
            @JsonProperty("created_at") Instant createdAt
    ) {
        static OrderSummary from(Order order) {
            return new OrderSummary(order.getId(), order.getStatus(), order.getTotalAmount(), order.getCreatedAt());
        }
    }
}
