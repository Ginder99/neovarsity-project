package com.vms.machine.dto;

import java.math.BigDecimal;

public record AddInventoryRequest(Long productId, String slotId, BigDecimal price, int quantity) {}
