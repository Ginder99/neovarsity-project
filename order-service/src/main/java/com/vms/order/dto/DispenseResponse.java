package com.vms.order.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.vms.order.entity.OrderItem;

import java.util.List;

public record DispenseResponse(
        @JsonProperty("order_id") String orderId,
        List<DispenseItem> items
) {
    public static DispenseResponse from(String orderId, List<OrderItem> orderItems) {
        var mapped = orderItems.stream().map(item -> new DispenseItem(item.getMachineInventory().getSlotId(), item.getQuantity())).toList();
        return new DispenseResponse(orderId, mapped);
    }

    public record DispenseItem(@JsonProperty("slot_id") String slotId, int quantity) {
    }
}
