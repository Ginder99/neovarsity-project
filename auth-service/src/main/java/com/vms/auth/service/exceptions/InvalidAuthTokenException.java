package com.vms.auth.service.exceptions;

import org.springframework.http.HttpStatus;

public class InvalidAuthTokenException extends AuthException {
    public InvalidAuthTokenException() {
        super("INVALID_AUTH_TOKEN", "Invalid auth token.", HttpStatus.UNAUTHORIZED);
    }
}
