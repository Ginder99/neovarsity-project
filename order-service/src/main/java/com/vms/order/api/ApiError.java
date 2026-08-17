package com.vms.order.api;

public record ApiError(String code, String message, int status) {
}
