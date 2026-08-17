package com.vms.order.dto;

public record DispenseCompletedRequest(Long orderId, boolean success) {}
