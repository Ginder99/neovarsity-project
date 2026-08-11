package com.vms.machine.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.vms.machine.entity.Product;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

import java.math.BigDecimal;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ProductResponse(
        Long id,
        String name,
        String description,
        String imageUrl,
        BigDecimal basePrice
) {
    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getImageUrl(),
                product.getBasePrice()
        );
    }
}