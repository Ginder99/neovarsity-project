package com.vms.machine_discovery.controller;

import com.vms.machine_discovery.entity.Machine;
import com.vms.machine_discovery.service.MachineService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/machines")
public class MachineController {

    private final MachineService machineService;

    public MachineController(MachineService machineService) {
        this.machineService = machineService;
    }

    @GetMapping
    public ResponseEntity<List<Machine>> listMachines(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "5") double radius_km) {
        return ResponseEntity.ok(machineService.findMachinesNear(lat, lng, radius_km));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Machine> getMachine(@PathVariable String id) {
        return machineService.getMachineById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
