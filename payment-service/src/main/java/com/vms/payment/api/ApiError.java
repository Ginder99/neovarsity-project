package com.vms.payment.api;

public record ApiError(String code, String message, int status) {
}
