package com.vms.auth.service.exceptions;

import org.springframework.http.HttpStatus;

public class EmailAlreadyInUseException extends AuthException {
    public EmailAlreadyInUseException(String email) {
        super("EMAIL_ALREADY_IN_USE", "Email already in use: " + email, HttpStatus.CONFLICT);
    }
}
