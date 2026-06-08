package com.vms.auth.service.exceptions;

import org.springframework.http.HttpStatus;

public class InvalidRefreshTokenException extends AuthException {
    public InvalidRefreshTokenException() {
        super("INVALID_REFRESH_TOKEN", "Invalid refresh token.", HttpStatus.UNAUTHORIZED);
    }
}
