package com.vms.order.api;

import com.vms.order.dto.CreateOrderRequest;
import com.vms.order.dto.ListOrdersResponse;
import com.vms.order.dto.OrderResponse;
import com.vms.order.dto.PaymentUpdateRequest;
import com.vms.order.dto.QrCodeResponse;
import com.vms.order.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse createOrder(@RequestBody CreateOrderRequest request,
                                     @AuthenticationPrincipal Long userId) {
        return OrderResponse.from(orderService.createOrder(request, userId));
    }

    @GetMapping("/{id}")
    public OrderResponse getOrder(@PathVariable("id") Long id,
                                  @AuthenticationPrincipal Long userId) {
        return OrderResponse.from(orderService.getOrderForUser(id, userId));
    }

    @GetMapping("/{id}/qr-code")
    public QrCodeResponse getQrCode(@PathVariable("id") Long id,
                                    @AuthenticationPrincipal Long userId) {
        return QrCodeResponse.from(orderService.getQrCode(id, userId));
    }

    @GetMapping()
    public ListOrdersResponse listOrders(@AuthenticationPrincipal Long userId) {
        return ListOrdersResponse.from(orderService.listOrders(userId));
    }

    @PostMapping("/internal/{id}/payment")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updatePayment(@PathVariable("id") Long id,
                              @RequestBody PaymentUpdateRequest request) {
        orderService.applyPaymentStatus(id, request.status());
    }
}
