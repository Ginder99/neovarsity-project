package com.vms.payment.api;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.PaymentIntent;
import com.vms.payment.service.PaymentService;
import com.vms.payment.service.StripeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/payments/webhook")
public class StripeWebhookController {

    private final StripeService stripeService;
    private final PaymentService paymentService;

    public StripeWebhookController(StripeService stripeService, PaymentService paymentService) {
        this.stripeService = stripeService;
        this.paymentService = paymentService;
    }

    @PostMapping("/stripe")
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        Event event;
        try {
            event = stripeService.constructVerifiedEvent(payload, sigHeader);
        } catch (SignatureVerificationException e) {
            log.warn("Rejected webhook call with invalid Stripe signature");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature");
        } catch (Exception e) {
            log.error("Failed to parse Stripe webhook payload", e);
            return ResponseEntity.badRequest().body("Malformed payload");
        }

        switch (event.getType()) {
            case "payment_intent.succeeded" -> {
                PaymentIntent intent = extractPaymentIntent(event);
                paymentService.handlePaymentIntentSucceeded(event.getId(), intent.getId());
            }
            case "payment_intent.payment_failed" -> {
                PaymentIntent intent = extractPaymentIntent(event);
                paymentService.handlePaymentIntentFailed(event.getId(), intent.getId());
            }
            default -> log.info("Ignoring unhandled Stripe event type: {}", event.getType());
        }

        // Always return 2xx quickly once verified + processed, or Stripe will
        // keep retrying (with exponential backoff) for up to ~3 days.
        return ResponseEntity.ok("received");
    }

    private PaymentIntent extractPaymentIntent(Event event) {
        EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();

        if (dataObjectDeserializer.getObject().isPresent()) {
            return (PaymentIntent) dataObjectDeserializer.getObject().get();
        }

        log.warn("API version mismatch deserializing event {} (event api_version={}); "
                        + "falling back to deserializeUnsafe()",
                event.getId(), event.getApiVersion());
        try {
            return (PaymentIntent) dataObjectDeserializer.deserializeUnsafe();
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Could not deserialize PaymentIntent for event " + event.getId()
                            + " even with deserializeUnsafe()", e);
        }
    }
}
