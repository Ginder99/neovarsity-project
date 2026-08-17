package com.vms.order.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.vms.order.entity.Order;

import java.time.Instant;

public record QrCodeResponse(
        @JsonProperty("order_id") Long orderId,
        @JsonProperty("qr_payload") String qrPayload,
        @JsonProperty("expires_at") Instant expiresAt
) {
    public static QrCodeResponse from(Order order) {
        return new QrCodeResponse(order.getId(), order.getQrCode().getPayload(),
                order.getQrCode().getExpiresAt());
    }
}
