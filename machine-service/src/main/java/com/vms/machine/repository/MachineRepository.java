package com.vms.machine.repository;

import com.vms.machine.dto.MachineDistanceProjection;
import com.vms.machine.entity.Machine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MachineRepository extends JpaRepository<Machine, Long> {
    @Query(value = """
                SELECT vm.id AS id, vm.name AS name, vm.address AS address, vm.latitude AS latitude, vm.longitude AS longitude,
                    vm.status AS status, ST_Distance_Sphere(vm.location, ST_SRID(POINT(:lng, :lat), 4326)) AS distanceMeters
                FROM vending_machines vm
                WHERE MBRContains(ST_Buffer(ST_SRID(POINT(:lng, :lat), 4326), :radiusMeters), vm.location)
                    AND vm.status = 'ONLINE'
                HAVING distanceMeters <= :radiusMeters
                ORDER BY distanceMeters ASC
                LIMIT :limit OFFSET :offset
            """, nativeQuery = true)
    List<MachineDistanceProjection> findNearbyMachines(
            @Param("lat") double lat,
            @Param("lng") double lng,
            @Param("radiusMeters") double radiusMeters,
            @Param("limit") int limit,
            @Param("offset") int offset
    );
}
