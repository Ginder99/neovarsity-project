package com.vms.machine.api;

import com.vms.machine.service.InventoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PostMapping("/bulk-load")
    @PreAuthorize("hasRole('MACHINE_HANDLER')")
    public ResponseEntity<Void> bulkLoad() {
        inventoryService.bulkLoad();
        return ResponseEntity.ok(null);
    }
}
