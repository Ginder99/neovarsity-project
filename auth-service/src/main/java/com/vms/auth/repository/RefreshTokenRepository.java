package com.vms.auth.repository;

import com.vms.auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {
    Optional<RefreshToken> findByUserId(Long userId);
    Optional<RefreshToken> findByTokenHash(String tokenHash);
}
