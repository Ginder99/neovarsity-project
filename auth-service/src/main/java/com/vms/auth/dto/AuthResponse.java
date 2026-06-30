package com.vms.auth.dto;

import org.jspecify.annotations.Nullable;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record AuthResponse(
        UserResponse user,
        @Nullable String accessToken,
        @Nullable String refreshToken,
        @Nullable String message
) {
    public AuthResponse(UserResponse user, String accessToken, String refreshToken) {
        this(user, accessToken, refreshToken, null);
    }

    public AuthResponse(UserResponse user, String message) {
        this(user, null, null, message);
    }
}
