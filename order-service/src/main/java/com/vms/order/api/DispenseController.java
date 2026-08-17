package com.vms.order.api;

import com.vms.order.dto.DispenseCompletedRequest;
import com.vms.order.dto.DispenseRequest;
import com.vms.order.dto.DispenseResponse;
import com.vms.order.service.OrderService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class DispenseController {

    private final OrderService orderService;

    public DispenseController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/machines/{id}/dispense/authorize")
    public DispenseResponse authorizeDispense(@PathVariable("id") Long machineId,
                                     @RequestBody DispenseRequest request,
                                     @RequestHeader(value = "X-Machine-Token", required = false) String machineToken) {
        var result = orderService.authorizeDispense(machineId, request.qrPayload(), machineToken);
        return DispenseResponse.from(result.orderId(), result.items());
    }

    @PostMapping("/machines/{id}/dispense")
    public DispenseResponse dispense(@RequestBody DispenseCompletedRequest request) {
        var result = orderService.dispense(request.orderId(), request.success());
        return DispenseResponse.from(result.orderId(), result.items());
    }
}
