package com.vms.payment.service;

import com.stripe.model.PaymentIntent;
import com.vms.payment.client.OrderServiceClient;
import com.vms.payment.dto.CreatePaymentIntentResponse;
import com.vms.payment.dto.OrderPaymentUpdateRequest;
import com.vms.payment.entity.Order;
import com.vms.payment.entity.Payment;
import com.vms.payment.entity.PaymentStatus;
import com.vms.payment.entity.ProcessedWebhookEvent;
import com.vms.payment.repository.PaymentRepository;
import com.vms.payment.repository.ProcessedWebhookEventRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class PaymentService {

    private final OrderService orderService;
    private final StripeService stripeService;
    private final PaymentRepository paymentRepository;
    private final ProcessedWebhookEventRepository processedWebhookEventRepository;
    private final OrderServiceClient orderServiceClient;

    public PaymentService(
            OrderService orderService, StripeService stripeService,
            PaymentRepository paymentRepository,
            ProcessedWebhookEventRepository processedWebhookEventRepository,
            OrderServiceClient orderServiceClient) {
        this.orderService = orderService;
        this.stripeService = stripeService;
        this.paymentRepository = paymentRepository;
        this.processedWebhookEventRepository = processedWebhookEventRepository;
        this.orderServiceClient = orderServiceClient;
    }

    @Transactional
    public CreatePaymentIntentResponse createPaymentIntent(Long userId, Long orderId) {
        try {
            Order order = orderService.findByIdAndUserId(orderId, userId);
            PaymentIntent intent = stripeService.createPaymentIntent(orderId, order.getTotalAmount(), "inr");

            Payment payment = new Payment();
            payment.setOrderId(orderId);
            payment.setPaymentIntentId(intent.getId());
            payment.setAmount(order.getTotalAmount());
            payment.setCurrency("inr");
            payment.setStatus(PaymentStatus.PENDING);
            paymentRepository.save(payment);

            return new CreatePaymentIntentResponse(intent.getClientSecret(), intent.getId());
        } catch (Exception e) {
            log.error("Failed to create Stripe PaymentIntent for orderId={}", orderId, e);
            throw new IllegalStateException("Unable to initiate payment", e);
        }
    }

    @Transactional
    public void handlePaymentIntentSucceeded(String stripeEventId, String paymentIntentId) {
        if (!markEventProcessedIfNew(stripeEventId)) {
            log.info("Duplicate Stripe event {} ignored (already processed)", stripeEventId);
            return;
        }

        Payment payment = paymentRepository.findByPaymentIntentId(paymentIntentId)
                .orElseThrow(() -> new IllegalStateException(
                        "No local Payment record for Stripe PaymentIntent " + paymentIntentId));

        payment.setStatus(PaymentStatus.SUCCEEDED);
        paymentRepository.save(payment);

        orderServiceClient.notifyOrderService(
                payment.getOrderId(),
                new OrderPaymentUpdateRequest("succeeded"));

        payment.setStatus(PaymentStatus.ORDER_NOTIFIED);
        paymentRepository.save(payment);
    }

    @Transactional
    public void handlePaymentIntentFailed(String stripeEventId, String paymentIntentId) {
        if (!markEventProcessedIfNew(stripeEventId)) {
            log.info("Duplicate Stripe event {} ignored (already processed)", stripeEventId);
            return;
        }

        Payment payment = paymentRepository.findByPaymentIntentId(paymentIntentId)
                .orElseThrow(() -> new IllegalStateException(
                        "No local Payment record for Stripe PaymentIntent " + paymentIntentId));

        payment.setStatus(PaymentStatus.FAILED);
        paymentRepository.save(payment);

        orderServiceClient.notifyOrderService(
                payment.getOrderId(),
                new OrderPaymentUpdateRequest("failed"));
    }

    private boolean markEventProcessedIfNew(String stripeEventId) {
        if (processedWebhookEventRepository.existsById(stripeEventId)) {
            return false;
        }
        ProcessedWebhookEvent event = new ProcessedWebhookEvent();
        event.setStripeEventId(stripeEventId);
        try {
            processedWebhookEventRepository.saveAndFlush(event);
            return true;
        } catch (DataIntegrityViolationException e) {
            return false;
        }
    }
}
