package com.vms.auth.api;

public record ApiError(String code, String message, int status) {
}
