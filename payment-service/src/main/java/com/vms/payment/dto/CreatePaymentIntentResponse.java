package com.vms.payment.dto;

public record CreatePaymentIntentResponse(
        String clientSecret,
        String paymentIntentId
) {}
