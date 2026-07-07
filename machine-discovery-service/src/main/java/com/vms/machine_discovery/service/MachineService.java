package com.vms.machine_discovery.service;

import com.vms.machine_discovery.entity.Machine;
import com.vms.machine_discovery.repository.MachineRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MachineService {

    private final MachineRepository machineRepository;

    public MachineService(MachineRepository machineRepository) {
        this.machineRepository = machineRepository;
    }

    public List<Machine> findMachinesNear(double lat, double lng, double radiusKm) {
        log.info("Finding machines near lat: {}, lng: {}, radius: {}km", lat, lng, radiusKm);
        // Simplified distance calculation for implementation
        return machineRepository.findAll().stream()
                .filter(m -> calculateDistance(lat, lng, m.getLatitude(), m.getLongitude()) <= radiusKm)
                .collect(Collectors.toList());
    }

    public Optional<Machine> getMachineById(String id) {
        log.info("Getting machine details for id: {}", id);
        return machineRepository.findById(id);
    }

    private double calculateDistance(double centerLat, double centerLng, BigDecimal machineLat, BigDecimal machineLng) {
        double lat1 = machineLat.doubleValue();
        double lon1 = machineLng.doubleValue();

        // Haversine Formula
        double earthRadius = 6371; // Kilometers
        double dLat = Math.toRadians(centerLat - lat1);
        double dLng = Math.toRadians(centerLng - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(centerLat)) *
                        Math.sin(dLng / 2) * Math.sin(dLng / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadius * c;
    }
}
