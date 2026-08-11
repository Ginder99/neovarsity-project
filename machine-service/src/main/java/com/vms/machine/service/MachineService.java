package com.vms.machine.service;

import com.vms.machine.dto.CreateMachineRequest;
import com.vms.machine.dto.MachineDistanceProjection;
import com.vms.machine.dto.MachineResponse;
import com.vms.machine.dto.NearbyMachinesResponse;
import com.vms.machine.entity.Machine;
import com.vms.machine.entity.MachineStatus;
import com.vms.machine.repository.MachineRepository;
import com.vms.machine.service.exceptions.InvalidSearchRadiusException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MachineService {

    private static final int SRID = 4326;
    private static final double MAX_RADIUS_KM = 10.0;
    private static final double DEFAULT_RADIUS_KM = 3.0;
    private static final int PAGE_SIZE = 3;
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

        Machine machine = new Machine(request.name(), request.address(), location, request.latitude(), request.longitude());

        Machine saved = machineRepository.save(machine);
        return MachineResponse.from(saved);
    }


    @Transactional
    public void enableMachine(Long machineId) {
        Machine machine = machineRepository.findById(machineId)
                .orElseThrow(() -> new RuntimeException("Machine not found with id: " +machineId));
        machine.setStatus(MachineStatus.ONLINE);
        machineRepository.save(machine);
    }

    @Transactional
    public NearbyMachinesResponse findNearbyMachines(double lat, double lng, Double radiusKmParam, String cursor) {
        double radiusKm = radiusKmParam == null ? DEFAULT_RADIUS_KM : radiusKmParam;
        if (radiusKm <= 0 || radiusKm > MAX_RADIUS_KM) {
            throw InvalidSearchRadiusException.outOfRange(radiusKm, MAX_RADIUS_KM);
        }

        int offset = decodeCursor(cursor);
        double radiusMeters = radiusKm * 1000;

        // Fetch one extra row beyond the page size purely to detect whether
        // a next page exists, without a separate COUNT(*) query.
        List<MachineDistanceProjection> fetched = machineRepository.findNearbyMachines(
                lat, lng, radiusMeters, PAGE_SIZE + 1, offset
        );

        boolean hasMore = fetched.size() > PAGE_SIZE;
        List<MachineDistanceProjection> pageResults = hasMore
                ? fetched.subList(0, PAGE_SIZE)
                : fetched;

        List<MachineResponse> machines = pageResults.stream()
                .map(MachineResponse::fromProjection)
                .collect(Collectors.toList());

        String nextCursor = hasMore ? encodeCursor(offset + PAGE_SIZE) : null;

        return new NearbyMachinesResponse(machines, nextCursor, hasMore);
    }

    private String encodeCursor(int offset) {
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(String.valueOf(offset).getBytes());
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

    public Optional<MachineResponse> getMachineById(Long id) {
        log.info("Getting machine details for id: {}", id);
        return machineRepository.findById(id).map(MachineResponse::from);
    }

    public void bulkLoad() throws RuntimeException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("test-data/vending_machines_test_data.csv");

        try (InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
             CSVParser parser = CSVFormat.DEFAULT.builder()
                     .setHeader() // reads first row as column names
                     .setSkipHeaderRecord(true)
                     .build()
                     .parse(reader)) {

            for (CSVRecord record : parser) {
                String name = record.get("name");
                String address = record.get("address");
                double latitude = Double.parseDouble(record.get("latitude"));
                double longitude = Double.parseDouble(record.get("longitude"));

                addMachine(new CreateMachineRequest(name, address, latitude, longitude));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
