package com.vms.machine.service.exceptions;

public class InvalidSearchRadiusException extends RuntimeException {

    private InvalidSearchRadiusException(String message) {
        super(message);
    }

    public static InvalidSearchRadiusException outOfRange(double requestedKm, double maxKm) {
        return new InvalidSearchRadiusException(
                "Search radius must be between 0 and " + maxKm + " km, got: " + requestedKm + " km"
        );
    }
}
