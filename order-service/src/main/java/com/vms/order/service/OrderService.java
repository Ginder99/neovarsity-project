package com.vms.order.service;

import com.vms.order.dto.CreateOrderRequest;
import com.vms.order.entity.Machine;
import com.vms.order.entity.MachineInventory;
import com.vms.order.entity.Order;
import com.vms.order.entity.OrderItem;
import com.vms.order.entity.QrCode;
import com.vms.order.repository.InventoryRepository;
import com.vms.order.repository.MachineRepository;
import com.vms.order.repository.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class OrderService {

    private static final String STATUS_PENDING_PAYMENT = "pending_payment";
    private static final String STATUS_PAID = "paid";
    private static final String STATUS_DISPENSING = "dispensing";
    private static final String STATUS_COMPLETED = "completed";
    private static final String STATUS_FAILED = "failed";

    private final OrderRepository orderRepository;
    private final MachineRepository machineRepository;
    private final InventoryRepository inventoryRepository;
    private final QrPayloadService qrPayloadService;
    private final int expiryHours;

    public OrderService(OrderRepository orderRepository, MachineRepository machineRepository,
                        InventoryRepository inventoryRepository, QrPayloadService qrPayloadService,
                        @Value("${order.expiry-hours:24}") int expiryHours) {
        this.orderRepository = orderRepository;
        this.machineRepository = machineRepository;
        this.inventoryRepository = inventoryRepository;
        this.qrPayloadService = qrPayloadService;
        this.expiryHours = expiryHours;
    }

    @Transactional
    public Order createOrder(CreateOrderRequest request, Long userId) {
        if (request == null || request.machineId() == null || request.items() == null || request.items().isEmpty()) {
            throw new ApiException(400, "INVALID_ORDER", "Invalid order request");
        }

        Order order = new Order();
        order.setUserId(userId);
        Machine machine = machineRepository.findById(request.machineId())
                .orElseThrow(() -> new ApiException(404, "MACHINE_NOT_FOUND", "Machine not found"));
        order.setMachine(machine);
        order.setStatus(STATUS_PENDING_PAYMENT);
        
        List<OrderItem> items = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (var input : request.items()) {
            if (input.quantity() <= 0 || input.inventoryId() == null) {
                throw new ApiException(400, "INVALID_ORDER_ITEM", "Invalid order items or quantities");
            }

            BigDecimal unitPrice = samplePrice(input.inventoryId());
            BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(input.quantity()));
            total = total.add(subtotal);
            
            OrderItem item = new OrderItem();
            item.setOrder(order);
            MachineInventory machineInventory = inventoryRepository.findById(input.inventoryId())
                    .orElseThrow(() -> new ApiException(404, "INVENTORY_NOT_FOUND", "Inventory not found for this item."));
            if(!machineInventory.getMachine().equals(machine)) {
                throw new ApiException(404, "INVALID_ITEM", "Inventory not found on this machine.");
            }
            item.setMachineInventory(machineInventory);
            item.setProduct(machineInventory.getProduct());
            item.setQuantity(input.quantity());
            item.setUnitPrice(unitPrice);
            items.add(item);
        }

        order.setItems(items);
        order.setTotalAmount(total);
        Instant now = Instant.now();
        order.setCreatedAt(now);
        order.setUpdatedAt(now);
        order.setExpiresAt(now.plus(expiryHours, ChronoUnit.HOURS));

        return orderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public Order getOrderForUser(Long orderId, Long userId) {
        return findByIdAndUserId(orderId, userId);
    }

    @Transactional(readOnly = true)
    public List<Order> listOrders(Long userId) {
        Pageable pageable = PageRequest.of(0, 10);
        return orderRepository.findByUserId(userId, pageable);
    }

    @Transactional
    public Order getQrCode(Long orderId, Long userId) {
        Order order = findByIdAndUserId(orderId, userId);

        if (!STATUS_PAID.equals(order.getStatus()) && !STATUS_DISPENSING.equals(order.getStatus()) && !STATUS_COMPLETED.equals(order.getStatus())) {
            throw new ApiException(402, "PAYMENT_REQUIRED", "Order has not been paid");
        }

        if (order.getQrCode() != null) {
            if (order.getQrCode().isUsed()) {
                throw new ApiException(410, "QR_USED", "QR code already used");
            }
            if (order.getQrCode().getExpiresAt().isBefore(Instant.now())) {
                throw new ApiException(410, "QR_EXPIRED", "QR code expired");
            }
            return order;
        }

        String payload = qrPayloadService.generatePayload(order.getId().toString(), order.getExpiresAt());

        QrCode qrCode = new QrCode();
        qrCode.setOrder(order);
        qrCode.setPayload(payload);
        qrCode.setExpiresAt(order.getExpiresAt());
        qrCode.setCreatedAt(Instant.now());
        
        order.setQrCode(qrCode);
        return orderRepository.save(order);
    }


    @Transactional
    public DispenseResult authorizeDispense(Long machineId, String payload, String machineToken) {
        if (machineToken == null || machineToken.isBlank()) {
            throw new ApiException(401, "UNAUTHORIZED", "Missing machine credentials");
        }
        var decoded = qrPayloadService.decodeAndValidate(payload);
        Order order = findById(Long.parseLong(decoded.orderId()));
        if (!order.getMachine().getId().equals(machineId)) {
            throw new ApiException(400, "INVALID_QR", "QR payload does not belong to machine");
        }
        if (order.getQrCode() == null) {
            throw new ApiException(400, "INVALID_QR", "QR not generated for order");
        }
        if (order.getQrCode().isUsed()) {
            throw new ApiException(409, "QR_ALREADY_USED", "QR code already used");
        }
        Instant now = Instant.now();
        if (order.getQrCode().getExpiresAt().isBefore(now) || decoded.expiresAt().isBefore(now)) {
            throw new ApiException(410, "QR_EXPIRED", "QR code expired");
        }

        order.getQrCode().setUsed(true);
        order.getQrCode().setScannedAt(now);
        order.setStatus(STATUS_DISPENSING);
        order.setUpdatedAt(now);
        orderRepository.save(order);

        return new DispenseResult(order.getId().toString(), order.getItems());
    }

    @Transactional
    public DispenseResult dispense(Long orderId, boolean success) {
        Order order = findById(orderId);
        order.setStatus(success ? STATUS_COMPLETED : STATUS_FAILED);
        orderRepository.save(order);
        
        return new DispenseResult(order.getId().toString(), order.getItems());
    }

    @Transactional
    public void applyPaymentStatus(Long orderId, String paymentStatus) {
        Order order = findById(orderId);
        String normalized = paymentStatus == null ? "" : paymentStatus.trim().toLowerCase();
        String nextStatus = switch (normalized) {
            case "succeeded" -> STATUS_PAID;
            case "failed", "refunded" -> STATUS_FAILED;
            default -> throw new ApiException(400, "INVALID_PAYMENT_STATUS", "Unsupported payment status");
        };

        if (Objects.equals(order.getStatus(), nextStatus)) {
            log.info("Order {} already in status {}, treating as idempotent no-op", orderId, nextStatus);
            return;
        }
        if (!Objects.equals(order.getStatus(), STATUS_PENDING_PAYMENT)) {
            throw new InvalidOrderStateTransitionException(
                    "Order " + orderId + " is in status " + order.getStatus() +
                            ", cannot transition to " + nextStatus);
        }

        order.setStatus(nextStatus);
        order.setUpdatedAt(Instant.now());
        
        if ("succeeded".equals(normalized) && order.getQrCode() == null) {
            String payload = qrPayloadService.generatePayload(order.getId().toString(), order.getExpiresAt());
            QrCode qrCode = new QrCode();
            qrCode.setOrder(order);
            qrCode.setPayload(payload);
            qrCode.setExpiresAt(order.getExpiresAt());
            qrCode.setCreatedAt(Instant.now());
            order.setQrCode(qrCode);
        }
        orderRepository.save(order);
    }

    private Order findByIdAndUserId(Long orderId, Long userId) {
        return orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new ApiException(404, "ORDER_NOT_FOUND", "Order not found"));
    }

    private Order findById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ApiException(404, "ORDER_NOT_FOUND", "Order not found"));
    }

    private BigDecimal samplePrice(Long inventoryId) {
        int bucket = Math.floorMod(inventoryId.hashCode(), 5) + 1;
        return BigDecimal.valueOf(bucket).add(BigDecimal.valueOf(0.25));
    }

    public record DispenseResult(String orderId, List<OrderItem> items) {
    }
}
