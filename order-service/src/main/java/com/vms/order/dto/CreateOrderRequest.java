package com.vms.order.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record CreateOrderRequest(
        @JsonProperty("machine_id") Long machineId,
        List<ItemRequest> items
) {
    public record ItemRequest(
            @JsonProperty("inventory_id") Long inventoryId,
            int quantity
    ) {
    }
}
