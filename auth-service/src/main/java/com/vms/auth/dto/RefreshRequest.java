package com.vms.auth.dto;

import jakarta.validation.constraints.NotBlank;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record RefreshRequest(@NotBlank(message = "Refresh Token is Required")
                             String refreshToken) {
}
