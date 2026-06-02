package com.vms.auth.service.exceptions;

import org.springframework.http.HttpStatus;

public class InvalidCredentialsException extends AuthException {
    public InvalidCredentialsException() {
        super("INVALID_CREDENTIALS", "Invalid Credentials.", HttpStatus.UNAUTHORIZED);
    }
}
