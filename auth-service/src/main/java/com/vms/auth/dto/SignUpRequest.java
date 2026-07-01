package com.vms.auth.dto;

import com.vms.auth.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record SignUpRequest(

        @NotBlank(message = "Email is Required")
        @Email(message = "Please provide a valid email address")
        @Pattern(
                regexp = "^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$",
                message = "Please provide a valid email address"
        )
        String email,
        @NotBlank(message = "Password is Required") String password,
        String name,
        @NotNull(message = "Role is Required") Role role
) {}