package com.vms.machine.api;

import com.vms.machine.dto.CreateMachineRequest;
import com.vms.machine.dto.MachineResponse;
import com.vms.machine.entity.Machine;
import com.vms.machine.service.MachineService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/machines")
public class MachineController {

    private final MachineService machineService;

    public MachineController(MachineService machineService) {
        this.machineService = machineService;
    }

    @PostMapping
    @PreAuthorize("hasRole('MACHINE_HANDLER')")
    public ResponseEntity<MachineResponse> addMachine(@Valid @RequestBody CreateMachineRequest request) {
        MachineResponse response = machineService.addMachine(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/nearby")
    public ResponseEntity<List<MachineResponse>> findNearby(
            @RequestHeader double lat,
            @RequestHeader double lng,
            @RequestParam(required = false) Double radiusKm,
            @RequestParam(required = false) String cursor) {

        List<MachineResponse> results = machineService.findNearbyMachines(lat, lng, radiusKm, cursor);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MachineResponse> getMachine(@PathVariable String id) {
        return machineService.getMachineById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
