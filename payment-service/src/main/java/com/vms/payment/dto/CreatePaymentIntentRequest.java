package com.vms.payment.dto;

import jakarta.validation.constraints.NotNull;

public record CreatePaymentIntentRequest(

        @NotNull(message = "OrderId is required")
        Long orderId
) {}
