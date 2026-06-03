package com.vms.auth.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "guest_sessions")
public class GuestSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(length = 36)
    private Long id;

    @Column(name = "guest_token_hash", nullable = false, unique = true)
    private String guestTokenHash;

    @Column
    private String email;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public GuestSession(Long id, String guestTokenHash, String email, Instant expiresAt) {
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
