package com.vms.order.repository;

import com.vms.order.entity.MachineInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InventoryRepository extends JpaRepository<MachineInventory, Long> {
}
