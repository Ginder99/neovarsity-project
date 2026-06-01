package com.vms.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "guest_sessions")
public class GuestSession {
    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "guest_token_hash", nullable = false, unique = true)
    private String guestTokenHash;

    @Column
    private String email;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public GuestSession(String id, String guestTokenHash, String email, Instant expiresAt) {
        this.id = id;
        this.guestTokenHash = guestTokenHash;
        this.email = email;
        this.expiresAt = expiresAt;
    }

    @PrePersist
    public void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
