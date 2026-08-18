package com.vms.payment.client;

import com.vms.payment.dto.OrderPaymentUpdateRequest;
import com.vms.payment.security.HmacSignerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;

@Slf4j
@Component
public class OrderServiceClient {

    private final WebClient orderServiceWebClient;
    private final HmacSignerService hmacSignerService;
    private final ObjectMapper objectMapper;
    private final String paymentCallbackPathTemplate;

    public OrderServiceClient(
            WebClient orderServiceWebClient,
            HmacSignerService hmacSignerService,
            ObjectMapper objectMapper,
            @Value("${order-service.payment-callback-path}") String paymentCallbackPathTemplate) {
        this.orderServiceWebClient = orderServiceWebClient;
        this.hmacSignerService = hmacSignerService;
        this.objectMapper = objectMapper;
        this.paymentCallbackPathTemplate = paymentCallbackPathTemplate;
    }

    public void notifyOrderService(Long orderId, OrderPaymentUpdateRequest payload) {
        String path = paymentCallbackPathTemplate.replace("{orderId}", String.valueOf(orderId));

        String body;
        try {
            body = objectMapper.writeValueAsString(payload);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize payload for order-service call", e);
        }

        String timestamp = String.valueOf(Instant.now().getEpochSecond());
        String canonical = hmacSignerService.buildCanonicalString("POST", path, timestamp, body);
        String signature = hmacSignerService.sign(canonical);

        log.info("Notifying order-service: orderId={}, status={}", orderId, payload.status());

        orderServiceWebClient.post()
                .uri(path)
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Internal-Timestamp", timestamp)
                .header("X-Internal-Signature", signature)
                .header("X-Service-Name", hmacSignerService.serviceName())
                .bodyValue(body)
                .retrieve()
                .toBodilessEntity()
                .block(); // simple synchronous call from a webhook handler; fine at this scale

        log.info("order-service acknowledged payment update for orderId={}", orderId);
    }
}
