package com.vms.machine.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.vms.machine.entity.Machine;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record MachineResponse(
        Long id,
        String name,
        String address,
        Double latitude,
        Double longitude,
        String status,
        Double distanceMeters // only populated for nearby-search results
) {

    public static MachineResponse from(Machine machine) {
        return new MachineResponse(
                machine.getId(),
                machine.getName(),
                machine.getAddress(),
                machine.getLatitude(),
                machine.getLongitude(),
                machine.getStatus().name(),
                null
        );
    }

    public static MachineResponse fromProjection(MachineDistanceProjection projection) {
        return new MachineResponse(
                projection.getId(),
                projection.getName(),
                projection.getAddress(),
                projection.getLatitude(),
                projection.getLongitude(),
                projection.getStatus(),
                projection.getDistanceMeters()
        );
    }
}