package com.vms.auth.dto;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record AdminCreateUserResponse(
        UserResponse user,
        String tempPassword
) {}
