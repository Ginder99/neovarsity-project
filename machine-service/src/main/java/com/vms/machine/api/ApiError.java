package com.vms.machine.api;

public record ApiError(String code, String message, int status) {
}
