package com.vms.auth.service;

import com.vms.auth.dto.AuthResponse;
import com.vms.auth.dto.LoginRequest;
import com.vms.auth.dto.SignUpRequest;
import com.vms.auth.dto.UserResponse;
import com.vms.auth.entity.RefreshToken;
import com.vms.auth.entity.User;
import com.vms.auth.repository.RefreshTokenRepository;
import com.vms.auth.repository.UserRepository;
import com.vms.auth.security.jwt.JwtService;
import com.vms.auth.service.exceptions.EmailAlreadyInUseException;
import com.vms.auth.service.exceptions.InvalidCredentialsException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
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
        User user = new User(UUID.randomUUID().toString(), request.email(), request.name(),
            passwordEncoder.encode(request.password()));
        user = userRepository.save(user);
        return generateTokens(user);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(InvalidCredentialsException::new);
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }
        return generateTokens(user);
    }

    private AuthResponse generateTokens(User user) {
        String refreshToken = generateRefreshToken(user);
        String accessToken = jwtService.generateToken(user);
        return new AuthResponse(toUserResponse(user), accessToken, refreshToken);
    }

    private String generateRefreshToken(User user) {
        String refreshToken = UUID.randomUUID().toString();
        RefreshToken token = new RefreshToken(
            UUID.randomUUID().toString(),
            user,
            passwordEncoder.encode(refreshToken),
            Instant.now().plusSeconds(refreshTokenExpirationSeconds)
        );
        refreshTokenRepository.save(token);
        return refreshToken;
    }

    private UserResponse toUserResponse(User user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getName(), user.getCreatedAt());
    }

    public User findUserById(String userId) {
        return userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found: " + userId));
    }
}
