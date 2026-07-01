package com.vms.auth.dto;

import com.vms.auth.entity.Role;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

import java.time.Instant;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record UserResponse(Long id, String email, String name, Role role, Boolean isActive, Instant createdAt) {
}
