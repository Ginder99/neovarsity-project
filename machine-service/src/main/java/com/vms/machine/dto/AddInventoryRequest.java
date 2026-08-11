package com.vms.machine.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

import java.math.BigDecimal;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record AddInventoryRequest(Long productId,
                                  @NotBlank(message = "Slot Id is required")
                                  @Max(30)
                                  @Min(1)
                                  String slotId,
                                  BigDecimal price, int quantity
) {}
