package com.vms.auth.service.exceptions;

import org.springframework.http.HttpStatus;

public class RecordNotFoundException extends AuthException {
    public RecordNotFoundException(String field) {
        super("RECORD_NOT_FOUND", "Couldn't find this " + field, HttpStatus.NOT_FOUND);
    }
}
