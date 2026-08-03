package com.vms.machine.dto;

public interface MachineDistanceProjection {
    Long getId();
    String getName();
    String getAddress();
    Double getLatitude();
    Double getLongitude();
    String getStatus();
    Double getDistanceMeters();
}