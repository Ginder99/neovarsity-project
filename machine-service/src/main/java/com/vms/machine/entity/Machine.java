package com.vms.machine.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import org.locationtech.jts.geom.Point;

import java.time.Instant;

@Entity
@Data
@Table(name = "vending_machines")
@Builder
public class Machine {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(length = 36)
    private Long id;
    private String name;
    private String address;
    @Column(nullable = false)
    private Double latitude;
    @Column(nullable = false)
    private Double longitude;
    @Column(nullable = false, columnDefinition = "POINT SRID 4326")
    private Point location;
    @Enumerated(EnumType.STRING)
    private MachineStatus status;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant lastHeartbeatAt;

    @PrePersist
    public void onCreate() {
        Instant now = Instant.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
    }
}
