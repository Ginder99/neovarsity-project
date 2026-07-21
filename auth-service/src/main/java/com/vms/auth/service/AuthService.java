package com.vms.auth.service;

import com.vms.auth.dto.*;
import com.vms.auth.entity.RefreshToken;
import com.vms.auth.entity.Role;
import com.vms.auth.entity.User;
import com.vms.auth.repository.RefreshTokenRepository;
import com.vms.auth.repository.UserRepository;
import com.vms.auth.security.jwt.JwtService;
import com.vms.auth.service.exceptions.AccountInactiveException;
import com.vms.auth.service.exceptions.EmailAlreadyInUseException;
import com.vms.auth.service.exceptions.InvalidCredentialsException;
import com.vms.auth.service.exceptions.InvalidRefreshTokenException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class AuthService {

    @Value("${refresh-token.expiration}")
    private long refreshTokenExpirationSeconds;

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public AuthResponse signUp(SignUpRequest request) {
        log.info("Attempting to sign up user with email: {}", request.email());
        if (userRepository.existsByEmail(request.email())) {
            log.warn("Sign up failed: Email already in use: {}", request.email());
            throw new EmailAlreadyInUseException(request.email());
        }
        User user = new User(request.email(), request.name(),
            passwordEncoder.encode(request.password()), Role.CONSUMER, true);
        user = userRepository.save(user);

        log.info("User successfully signed up: {}", user.getEmail());
        return generateTokens(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        log.info("Attempting to login user with email: {}", request.email());
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(InvalidCredentialsException::new);
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            log.warn("Login failed: Invalid credentials for email: {}", request.email());
            throw new InvalidCredentialsException();
        }
        if (!user.getIsActive()) {
            log.warn("Login failed: Account inactive for email: {}", request.email());
            throw new AccountInactiveException();
        }
        refreshTokenRepository.findByUserId(user.getId()).ifPresent(refreshTokenRepository::delete);
        log.info("User successfully logged in: {}", user.getEmail());
        return generateTokens(user);
    }

    @Transactional
    public AccessTokenResponse refresh(RefreshRequest request) {
        log.debug("Attempting to refresh access token");
        String tokenHash = generateSHA256Hash(request.refreshToken());
        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(InvalidRefreshTokenException::new);
        if (refreshToken.getExpiresAt().isBefore(Instant.now())) {
            log.warn("Refresh failed: Token expired for user: {}", refreshToken.getUser().getEmail());
            refreshTokenRepository.delete(refreshToken);
            throw new InvalidRefreshTokenException();
        }
        User user = refreshToken.getUser();
        String accessToken = jwtService.generateToken(user);
        log.debug("Access token successfully refreshed for user: {}", user.getEmail());
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
            log.error("SHA-256 algorithm not found", e);
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }

    private UserResponse toUserResponse(User user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getName(), user.getRole(), user.getIsActive(), user.getCreatedAt());
    }

    @Transactional
    public User findUserById(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> {
            log.error("User not found: {}", userId);
            return new RuntimeException("User not found: " + userId);
        });
    }
}
