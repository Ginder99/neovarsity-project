package com.vms.machine_discovery.repository;

import com.vms.machine_discovery.entity.MachineInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryRepository extends JpaRepository<MachineInventory, String> {
    List<MachineInventory> findByMachineIdAndQuantityGreaterThan(String machineId, int quantity);
}
