package com.vms.auth.service.exceptions;

import org.springframework.http.HttpStatus;

public class AccountInactiveException extends AuthException {
    public AccountInactiveException() {
        super("ACCOUNT_INACTIVE", "Account is inactive. Please call support to activate your account.", HttpStatus.FORBIDDEN);
    }
}
