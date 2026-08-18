package com.vms.payment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(name = "processed_webhook_event")
@Getter
@Setter
@NoArgsConstructor
public class ProcessedWebhookEvent {

    @Id
    @Column(length = 255)
    private String stripeEventId;

    @Column(nullable = false)
    private Instant processedAt;

    @PrePersist
    void onCreate() {
        processedAt = Instant.now();
    }
}
