package com.vms.auth.service.exceptions;

import org.springframework.http.HttpStatus;

public class InvalidResetTokenException extends AuthException {
    public InvalidResetTokenException() {
        super("INVALID_RESET_TOKEN", "Invalid or expired reset token", HttpStatus.BAD_REQUEST);
    }
}
