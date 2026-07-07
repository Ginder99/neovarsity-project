package com.vms.machine_discovery.service;

import com.vms.machine_discovery.entity.MachineInventory;
import com.vms.machine_discovery.repository.InventoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    public InventoryService(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    public List<MachineInventory> getAvailableInventory(String machineId) {
        log.info("Getting available inventory for machine id: {}", machineId);
        return inventoryRepository.findByMachineIdAndQuantityGreaterThan(machineId, 0);
    }
}
