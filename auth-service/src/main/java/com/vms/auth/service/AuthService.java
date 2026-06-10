package com.vms.auth.service;

import com.vms.auth.dto.*;
import com.vms.auth.entity.RefreshToken;
import com.vms.auth.entity.User;
import com.vms.auth.repository.RefreshTokenRepository;
import com.vms.auth.repository.UserRepository;
import com.vms.auth.security.jwt.JwtService;
import com.vms.auth.service.exceptions.EmailAlreadyInUseException;
import com.vms.auth.service.exceptions.InvalidCredentialsException;
import com.vms.auth.service.exceptions.InvalidRefreshTokenException;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    @Value("${refresh-token.expiration}")
    private long refreshTokenExpirationSeconds;

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthResponse signUp(SignUpRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyInUseException(request.email());
        }
        User user = new User(request.email(), request.name(),
            passwordEncoder.encode(request.password()), false);
        user = userRepository.save(user);
        return generateTokens(user);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(InvalidCredentialsException::new);
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }
        refreshTokenRepository.findByUserId(user.getId()).ifPresent(refreshTokenRepository::delete);
        return generateTokens(user);
    }

    public AccessTokenResponse refresh(RefreshRequest request) {
        String tokenHash = generateSHA256Hash(request.refreshToken());
        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(InvalidRefreshTokenException::new);
        if (refreshToken.getExpiresAt().isBefore(Instant.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new InvalidRefreshTokenException();
        }
        User user = refreshToken.getUser();
        String accessToken = jwtService.generateToken(user);
        return new AccessTokenResponse(accessToken);
    }

    private AuthResponse generateTokens(User user) {
        String refreshToken = generateRefreshToken(user);
        String accessToken = jwtService.generateToken(user);
        return new AuthResponse(toUserResponse(user), accessToken, refreshToken);
    }

    private String generateRefreshToken(User user) {
        String refreshToken = UUID.randomUUID().toString();
        String hashedToken;
        hashedToken = generateSHA256Hash(refreshToken);
        RefreshToken token = new RefreshToken(
                user, hashedToken,
                Instant.now().plusSeconds(refreshTokenExpirationSeconds)
        );
        refreshTokenRepository.save(token);
        return refreshToken;
    }

    private String generateSHA256Hash(String rawString) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = digest.digest(rawString.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(encodedHash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }

    private UserResponse toUserResponse(User user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getName(), user.getCreatedAt());
    }

    public User findUserById(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found: " + userId));
    }

    public AuthResponse createGuestSession() {
        String rawToken = UUID.randomUUID().toString();
        User user = new User("guest_" + rawToken + "@example.com", "Guest User", passwordEncoder.encode(rawToken), true);
        user = userRepository.save(user);
        return generateTokens(user);
    }
}
