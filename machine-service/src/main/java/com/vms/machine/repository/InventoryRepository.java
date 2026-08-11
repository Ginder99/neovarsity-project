package com.vms.machine.repository;

import com.vms.machine.entity.MachineInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryRepository extends JpaRepository<MachineInventory, Long> {
    List<MachineInventory> findByMachineIdAndQuantityGreaterThan(String machineId, int quantity);
}
