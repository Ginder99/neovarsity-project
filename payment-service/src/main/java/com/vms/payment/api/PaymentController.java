package com.vms.payment.api;

import com.vms.payment.dto.CreatePaymentIntentRequest;
import com.vms.payment.dto.CreatePaymentIntentResponse;
import com.vms.payment.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/create-intent")
    public ResponseEntity<CreatePaymentIntentResponse> createIntent(
            @Valid @RequestBody CreatePaymentIntentRequest request,
            @AuthenticationPrincipal Long userId) {

        CreatePaymentIntentResponse response = paymentService.createPaymentIntent(
                userId, request.orderId());

        return ResponseEntity.ok(response);
    }
}
