package com.vms.machine.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

import java.math.BigDecimal;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record CreateProductRequest(
        @NotBlank(message = "Name is required")
        @Size(max = 100)
        String name,

        @NotBlank(message = "Description is required")
        @Size(max = 255)
        String description,

        @NotBlank(message = "Category is required")
        @Size(max = 50)
        String category,

        @Size(max = 512)
        String imageUrl,

        @NotNull(message = "Base price is required")
        @DecimalMin(value = "0.01", message = "Base price must be greater than 0")
        @Digits(integer = 8, fraction = 2, message = "Base price allows up to 2 decimal places")
        BigDecimal basePrice
) {}