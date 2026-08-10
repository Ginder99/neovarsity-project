package com.vms.machine.service;

import com.vms.machine.dto.AddInventoryRequest;
import com.vms.machine.entity.MachineInventory;
import com.vms.machine.repository.InventoryRepository;
import com.vms.machine.repository.MachineRepository;
import com.vms.machine.repository.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


import java.util.List;

@Service
@Slf4j
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final MachineRepository machineRepository;
    private final ProductRepository productRepository;

    public InventoryService(InventoryRepository inventoryRepository, MachineRepository machineRepository, ProductRepository productRepository) {
        this.inventoryRepository = inventoryRepository;
        this.machineRepository = machineRepository;
        this.productRepository = productRepository;
    }

    public List<MachineInventory> getAvailableInventory(String machineId) {
        log.info("Getting available inventory for machine id: {}", machineId);
        return inventoryRepository.findByMachineIdAndQuantityGreaterThan(machineId, 0);
    }

    public void addInventory(Long machineId, AddInventoryRequest request) {
        log.info("Adding inventory to machine id: {}", machineId);
        var machine = machineRepository.findById(machineId).orElseThrow(() -> new RuntimeException("Machine not found"));
        var product = productRepository.findById(request.productId()).orElseThrow(() -> new RuntimeException("Product not found"));

        var inventory = new MachineInventory();
        inventory.setMachine(machine);
        inventory.setProduct(product);
        inventory.setSlotId(request.slotId());
        inventory.setPrice(request.price());
        inventory.setQuantity(request.quantity());

        inventoryRepository.save(inventory);
    }
}
