package com.vms.machine.dto;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

import java.util.List;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record NearbyMachinesResponse(
        List<MachineResponse> machines,
        String nextCursor,
        boolean hasMore
) {}