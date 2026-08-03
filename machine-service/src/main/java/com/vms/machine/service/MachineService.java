package com.vms.machine.service;

import com.vms.machine.dto.CreateMachineRequest;
import com.vms.machine.dto.MachineDistanceProjection;
import com.vms.machine.dto.MachineResponse;
import com.vms.machine.entity.Machine;
import com.vms.machine.entity.MachineStatus;
import com.vms.machine.repository.MachineRepository;
import com.vms.machine.service.exceptions.InvalidSearchRadiusException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MachineService {

    private static final int SRID = 4326;
    private static final double MAX_RADIUS_KM = 25.0;
    private static final double DEFAULT_RADIUS_KM = 5.0;
    private static final int PAGE_SIZE = 20;
    private static final double KM_PER_DEGREE = 111.0;

    private final MachineRepository machineRepository;

    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), SRID);

    public MachineService(MachineRepository machineRepository) {
        this.machineRepository = machineRepository;
    }

    @Transactional
    public MachineResponse addMachine(CreateMachineRequest request) {
        Point location = geometryFactory.createPoint(
                new Coordinate(request.longitude(), request.latitude())
        );
        location.setSRID(SRID);

        Machine machine = Machine.builder()
                .name(request.name())
                .address(request.address())
                .location(location)
                .latitude(request.latitude())
                .longitude(request.longitude())
                .status(MachineStatus.ONLINE)
                .build();

        Machine saved = machineRepository.save(machine);
        return MachineResponse.from(saved);
    }

    @Transactional
    public List<MachineResponse> findNearbyMachines(double lat, double lng, Double radiusKmParam, String cursor) {
        double radiusKm = radiusKmParam == null ? DEFAULT_RADIUS_KM : radiusKmParam;
        if (radiusKm <= 0 || radiusKm > MAX_RADIUS_KM) {
            throw InvalidSearchRadiusException.outOfRange(radiusKm, MAX_RADIUS_KM);
        }

        int offset = decodeCursor(cursor);
        double radiusMeters = radiusKm * 1000;
        double radiusDegrees = radiusKm / KM_PER_DEGREE;

        List<MachineDistanceProjection> results = machineRepository.findNearbyMachines(
                lat, lng, radiusDegrees, radiusMeters, PAGE_SIZE, offset
        );

        return results.stream()
                .map(MachineResponse::fromProjection)
                .collect(Collectors.toList());
    }

    private int decodeCursor(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return 0;
        }
        try {
            String decoded = new String(Base64.getUrlDecoder().decode(cursor));
            return Integer.parseInt(decoded);
        } catch (Exception e) {
            return 0;
        }
    }

    public Optional<MachineResponse> getMachineById(String id) {
        log.info("Getting machine details for id: {}", id);
        return machineRepository.findById(id).map(MachineResponse::from);
    }
}
