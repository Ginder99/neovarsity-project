package com.vms.machine.dto;

import com.vms.machine.entity.Product;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

import java.math.BigDecimal;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record MachineInventoryResponse(
        Long id,
        String slotId,
        MachineResponse machine,
        Product product,
        BigDecimal price,
        int quantity
) {}
