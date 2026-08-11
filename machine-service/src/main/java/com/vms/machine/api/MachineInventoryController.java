package com.vms.machine.api;

import com.vms.machine.dto.AddInventoryRequest;
import com.vms.machine.dto.MachineInventoryResponse;
import com.vms.machine.service.InventoryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/machines/{id}/inventory")
public class MachineInventoryController {

    private final InventoryService inventoryService;

    public MachineInventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping
    public ResponseEntity<List<MachineInventoryResponse>> getInventory(@PathVariable String id) {
        return ResponseEntity.ok(inventoryService.getAvailableInventory(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('MACHINE_HANDLER')")
    public ResponseEntity<Void> addInventory(@PathVariable Long id, @Valid @RequestBody AddInventoryRequest request) {
        inventoryService.addInventory(id, request);
        return ResponseEntity.ok().build();
    }
}
