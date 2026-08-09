package com.vms.machine.service;

import com.vms.machine.dto.CreateMachineRequest;
import com.vms.machine.dto.MachineDistanceProjection;
import com.vms.machine.dto.MachineResponse;
import com.vms.machine.dto.NearbyMachinesResponse;
import com.vms.machine.entity.Machine;
import com.vms.machine.entity.MachineStatus;
import com.vms.machine.repository.MachineRepository;
import com.vms.machine.service.exceptions.InvalidSearchRadiusException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Base64;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MachineServiceTest {

    @Mock
    private MachineRepository machineRepository;

    @InjectMocks
    private MachineService machineService;

    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    @Test
    void addMachine_Success() {
        // Arrange
        CreateMachineRequest request = new CreateMachineRequest(
                "Vending Machine 1",
                "123 Tech Street",
                37.7749,
                -122.4194
        );

        Point location = geometryFactory.createPoint(new Coordinate(request.longitude(), request.latitude()));
        location.setSRID(4326);

        Machine savedMachine = Machine.builder()
                .id(100L)
                .name(request.name())
                .address(request.address())
                .location(location)
                .latitude(request.latitude())
                .longitude(request.longitude())
                .status(MachineStatus.ONLINE)
                .build();

        when(machineRepository.save(any(Machine.class))).thenReturn(savedMachine);

        // Act
        MachineResponse response = machineService.addMachine(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(100L);
        assertThat(response.name()).isEqualTo("Vending Machine 1");
        assertThat(response.address()).isEqualTo("123 Tech Street");
        assertThat(response.latitude()).isEqualTo(37.7749);
        assertThat(response.longitude()).isEqualTo(-122.4194);
        assertThat(response.status()).isEqualTo("ONLINE");
        assertThat(response.distanceMeters()).isNull();

        ArgumentCaptor<Machine> machineCaptor = ArgumentCaptor.forClass(Machine.class);
        verify(machineRepository, times(1)).save(machineCaptor.capture());
        
        Machine captured = machineCaptor.getValue();
        assertThat(captured.getName()).isEqualTo(request.name());
        assertThat(captured.getAddress()).isEqualTo(request.address());
        assertThat(captured.getLatitude()).isEqualTo(request.latitude());
        assertThat(captured.getLongitude()).isEqualTo(request.longitude());
        assertThat(captured.getStatus()).isEqualTo(MachineStatus.ONLINE);
        assertThat(captured.getLocation().getSRID()).isEqualTo(4326);
    }

    @Test
    void getMachineById_Found() {
        // Arrange
        Long machineId = 1L;
        Machine machine = Machine.builder()
                .id(machineId)
                .name("Machine 1")
                .address("Location 1")
                .latitude(12.34)
                .longitude(56.78)
                .status(MachineStatus.ONLINE)
                .build();

        when(machineRepository.findById(String.valueOf(machineId))).thenReturn(Optional.of(machine));

        // Act
        Optional<MachineResponse> result = machineService.getMachineById(String.valueOf(machineId));

        // Assert
        assertThat(result).isPresent();
        MachineResponse response = result.get();
        assertThat(response.id()).isEqualTo(machineId);
        assertThat(response.name()).isEqualTo("Machine 1");
        assertThat(response.address()).isEqualTo("Location 1");
        assertThat(response.status()).isEqualTo("ONLINE");
    }

    @Test
    void getMachineById_NotFound() {
        // Arrange
        String nonExistentId = "999";
        when(machineRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act
        Optional<MachineResponse> result = machineService.getMachineById(nonExistentId);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void findNearbyMachines_InvalidRadius_TooSmall_ThrowsException() {
        // Act & Assert
        assertThrows(InvalidSearchRadiusException.class, () ->
                machineService.findNearbyMachines(37.7749, -122.4194, 0.0, null)
        );

        assertThrows(InvalidSearchRadiusException.class, () ->
                machineService.findNearbyMachines(37.7749, -122.4194, -1.0, null)
        );
    }

    @Test
    void findNearbyMachines_InvalidRadius_TooLarge_ThrowsException() {
        // Act & Assert
        assertThrows(InvalidSearchRadiusException.class, () ->
                machineService.findNearbyMachines(37.7749, -122.4194, 10.01, null)
        );
    }

    @Test
    void findNearbyMachines_DefaultRadius_WhenNull() {
        // Arrange
        double lat = 37.7749;
        double lng = -122.4194;
        
        // Default radius is 3.0 km.
        // radiusMeters = 3.0 * 1000 = 3000.0
        double expectedRadiusMeters = 3000.0;

        when(machineRepository.findNearbyMachines(
                eq(lat), eq(lng), eq(expectedRadiusMeters), eq(4), eq(0)
        )).thenReturn(List.of());

        // Act
        NearbyMachinesResponse response = machineService.findNearbyMachines(lat, lng, null, null);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.machines()).isEmpty();
        assertThat(response.hasMore()).isFalse();
        assertThat(response.nextCursor()).isNull();
    }

    @Test
    void findNearbyMachines_FewerThanPageSize_NoPagination() {
        // Arrange
        double lat = 37.7749;
        double lng = -122.4194;
        double radiusKm = 5.0;
        double expectedRadiusMeters = 5000.0;

        List<MachineDistanceProjection> projections = List.of(
                createMockProjection(1L, "Machine A", "Addr A", 37.7, -122.4, "ONLINE", 120.5),
                createMockProjection(2L, "Machine B", "Addr B", 37.8, -122.5, "ONLINE", 450.0)
        );

        when(machineRepository.findNearbyMachines(
                eq(lat), eq(lng), eq(expectedRadiusMeters), eq(4), eq(0)
        )).thenReturn(projections);

        // Act
        NearbyMachinesResponse response = machineService.findNearbyMachines(lat, lng, radiusKm, null);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.machines()).hasSize(2);
        assertThat(response.hasMore()).isFalse();
        assertThat(response.nextCursor()).isNull();

        MachineResponse r1 = response.machines().get(0);
        assertThat(r1.id()).isEqualTo(1L);
        assertThat(r1.name()).isEqualTo("Machine A");
        assertThat(r1.distanceMeters()).isEqualTo(120.5);

        MachineResponse r2 = response.machines().get(1);
        assertThat(r2.id()).isEqualTo(2L);
        assertThat(r2.name()).isEqualTo("Machine B");
        assertThat(r2.distanceMeters()).isEqualTo(450.0);
    }

    @Test
    void findNearbyMachines_ExactlyPageSize_NoPagination() {
        // Arrange
        double lat = 37.7749;
        double lng = -122.4194;
        double radiusKm = 5.0;
        double expectedRadiusMeters = 5000.0;

        // PAGE_SIZE is 3
        List<MachineDistanceProjection> projections = List.of(
                createMockProjection(1L, "Machine A", "Addr A", 37.7, -122.4, "ONLINE", 100.0),
                createMockProjection(2L, "Machine B", "Addr B", 37.8, -122.5, "ONLINE", 200.0),
                createMockProjection(3L, "Machine C", "Addr C", 37.9, -122.6, "ONLINE", 300.0)
        );

        when(machineRepository.findNearbyMachines(
                eq(lat), eq(lng), eq(expectedRadiusMeters), eq(4), eq(0)
        )).thenReturn(projections);

        // Act
        NearbyMachinesResponse response = machineService.findNearbyMachines(lat, lng, radiusKm, null);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.machines()).hasSize(3);
        assertThat(response.hasMore()).isFalse();
        assertThat(response.nextCursor()).isNull();
    }

    @Test
    void findNearbyMachines_HasMoreThanPageSize_WithPagination() {
        // Arrange
        double lat = 37.7749;
        double lng = -122.4194;
        double radiusKm = 5.0;
        double expectedRadiusMeters = 5000.0;

        // Repository returns 4 rows (PAGE_SIZE + 1)
        List<MachineDistanceProjection> projections = List.of(
                createMockProjection(1L, "Machine A", "Addr A", 37.7, -122.4, "ONLINE", 100.0),
                createMockProjection(2L, "Machine B", "Addr B", 37.8, -122.5, "ONLINE", 200.0),
                createMockProjection(3L, "Machine C", "Addr C", 37.9, -122.6, "ONLINE", 300.0),
                Mockito.mock(MachineDistanceProjection.class) // Only needed to make size > PAGE_SIZE (4 > 3), methods not invoked
        );

        when(machineRepository.findNearbyMachines(
                eq(lat), eq(lng), eq(expectedRadiusMeters), eq(4), eq(0)
        )).thenReturn(projections);

        // Act
        NearbyMachinesResponse response = machineService.findNearbyMachines(lat, lng, radiusKm, null);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.machines()).hasSize(3); // PAGE_SIZE limit
        assertThat(response.hasMore()).isTrue();
        
        // Decode cursor "Mw" (base64 URL encoded of "3" since offset=0 and PAGE_SIZE=3, next cursor offset is 3)
        String expectedCursor = Base64.getUrlEncoder().withoutPadding().encodeToString("3".getBytes());
        assertThat(response.nextCursor()).isEqualTo(expectedCursor);

        // Let's verify results inside response are from the first 3 elements
        assertThat(response.machines().stream().map(MachineResponse::id))
                .containsExactly(1L, 2L, 3L);
    }

    @Test
    void findNearbyMachines_WithValidCursorOffset() {
        // Arrange
        double lat = 37.7749;
        double lng = -122.4194;
        double radiusKm = 5.0;
        double expectedRadiusMeters = 5000.0;

        // Encoded cursor of "3"
        String cursor = Base64.getUrlEncoder().withoutPadding().encodeToString("3".getBytes());

        List<MachineDistanceProjection> projections = List.of(
                createMockProjection(4L, "Machine D", "Addr D", 38.0, -122.7, "ONLINE", 400.0)
        );

        when(machineRepository.findNearbyMachines(
                eq(lat), eq(lng), eq(expectedRadiusMeters), eq(4), eq(3)
        )).thenReturn(projections);

        // Act
        NearbyMachinesResponse response = machineService.findNearbyMachines(lat, lng, radiusKm, cursor);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.machines()).hasSize(1);
        assertThat(response.hasMore()).isFalse();
        assertThat(response.nextCursor()).isNull();
        assertThat(response.machines().get(0).id()).isEqualTo(4L);
    }

    @Test
    void findNearbyMachines_WithInvalidCursor_FallsBackToOffsetZero() {
        // Arrange
        double lat = 37.7749;
        double lng = -122.4194;
        double radiusKm = 5.0;
        double expectedRadiusMeters = 5000.0;

        String invalidCursor = "invalid_not_base_64_or_number!";

        when(machineRepository.findNearbyMachines(
                eq(lat), eq(lng), eq(expectedRadiusMeters), eq(4), eq(0)
        )).thenReturn(List.of());

        // Act
        NearbyMachinesResponse response = machineService.findNearbyMachines(lat, lng, radiusKm, invalidCursor);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.machines()).isEmpty();
        assertThat(response.hasMore()).isFalse();
        assertThat(response.nextCursor()).isNull();
    }

    private MachineDistanceProjection createMockProjection(
            Long id, String name, String address, Double latitude, Double longitude, String status, Double distanceMeters) {
        MachineDistanceProjection projection = Mockito.mock(MachineDistanceProjection.class);
        when(projection.getId()).thenReturn(id);
        when(projection.getName()).thenReturn(name);
        when(projection.getAddress()).thenReturn(address);
        when(projection.getLatitude()).thenReturn(latitude);
        when(projection.getLongitude()).thenReturn(longitude);
        when(projection.getStatus()).thenReturn(status);
        when(projection.getDistanceMeters()).thenReturn(distanceMeters);
        return projection;
    }
}
