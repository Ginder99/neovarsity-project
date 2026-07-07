package com.vms.machine_discovery.controller;

import com.vms.machine_discovery.entity.MachineInventory;
import com.vms.machine_discovery.service.InventoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/machines/{id}/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping
    public ResponseEntity<List<MachineInventory>> getInventory(@PathVariable String id) {
        return ResponseEntity.ok(inventoryService.getAvailableInventory(id));
    }
}
