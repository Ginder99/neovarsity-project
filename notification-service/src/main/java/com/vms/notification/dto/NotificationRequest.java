package com.vms.notification.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record NotificationRequest(
        @NotBlank String notificationType,
        @NotBlank @Email String recipientEmail) {
}
