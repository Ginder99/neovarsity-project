package com.vms.machine.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Point;

import java.time.Instant;

@Entity
@Data
@NoArgsConstructor
@Table(name = "vending_machines")
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

    public Machine(String name,
                   String address,
                   Point location,
                   Double latitude,
                   Double longitude) {
        this.name = name;
        this.address = address;
        this.location = location;
        this.latitude = latitude;
        this.longitude = longitude;
        this.status = MachineStatus.OFFLINE;
    }

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
