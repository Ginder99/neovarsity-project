package com.vms.machine.service;

import com.vms.machine.dto.AddInventoryRequest;
import com.vms.machine.dto.CreateProductRequest;
import com.vms.machine.dto.MachineInventoryResponse;
import com.vms.machine.dto.MachineResponse;
import com.vms.machine.entity.MachineInventory;
import com.vms.machine.repository.InventoryRepository;
import com.vms.machine.repository.MachineRepository;
import com.vms.machine.repository.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

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

    public List<MachineInventoryResponse> getAvailableInventory(String machineId) {
        log.info("Getting available inventory for machine id: {}", machineId);
        List<MachineInventory> machineInventoryList = inventoryRepository.findByMachineIdAndQuantityGreaterThan(machineId, 0);
        return machineInventoryList.stream()
                .map(inventory -> new MachineInventoryResponse(
                        inventory.getId(),
                        inventory.getSlotId(),
                        MachineResponse.from(inventory.getMachine()),
                        inventory.getProduct(),
                        inventory.getPrice(),
                        inventory.getQuantity()
                ))
                .collect(Collectors.toList());
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

    public void bulkLoad() {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("test-data/machine_inventory_test_data.csv");

        try (InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
             CSVParser parser = CSVFormat.DEFAULT.builder()
                     .setHeader() // reads first row as column names
                     .setSkipHeaderRecord(true)
                     .build()
                     .parse(reader)) {

            for (CSVRecord record : parser) {
                Long machineId = Long.parseLong(record.get("machine_id"));
                String slotId = record.get("slot_id");
                Long productId = Long.parseLong(record.get("product_id"));
                String price = record.get("price");
                String quantity = record.get("quantity");

                try {
                    addInventory(machineId, new AddInventoryRequest(
                            productId,
                            slotId,
                            new BigDecimal(price),
                            Integer.parseInt(quantity)
                    ));
                } catch (Exception e) {
                    log.error("Error adding inventory for machineId: {}, slotId: {}, productId: {}. Error: {}", machineId, slotId, productId, e.getMessage());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
