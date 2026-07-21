package com.vms.auth.service;

import com.vms.auth.dto.*;
import com.vms.auth.entity.Role;
import com.vms.auth.repository.RefreshTokenRepository;
import com.vms.auth.repository.UserRepository;
import com.vms.auth.service.exceptions.EmailAlreadyInUseException;
import com.vms.auth.service.exceptions.InvalidCredentialsException;
import com.vms.auth.service.exceptions.InvalidRefreshTokenException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
class AuthServiceTest {
    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @BeforeEach
    void setUp() {
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void signupCreatesUserAndTokens() {
        AuthResponse response = authService.signUp(new SignUpRequest(
            "jane@example.com",
            "S3cure!Pass",
            "Jane Doe"
        ));
        assertThat(response.user().email()).isEqualTo("jane@example.com");
        assertThat(response.user().role()).isEqualTo(Role.CONSUMER);
        assertThat(response.user().isActive()).isTrue();
        assertThat(response.accessToken()).isNotBlank();
        assertThat(response.refreshToken()).isNotBlank();
        assertThat(userRepository.findByEmail("jane@example.com")).isPresent();
    }

    @Test
    void signupThrowsEmailExistsError() {
        authService.signUp(new SignUpRequest(
                "jane@example.com",
                "S3cure!Pass",
                "Jane Doe"
        ));
        assertThrows(EmailAlreadyInUseException.class, () -> authService.signUp(new SignUpRequest(
                "jane@example.com",
                "S3cure!Pass",
                "Jane Doe"
        )));
    }

    @Test
    void loginSuccess() {
        authService.signUp(new SignUpRequest(
                "jane@example.com",
                "S3cure!Pass",
                "Jane Doe"
        ));
        AuthResponse response = authService.login(new LoginRequest(
                "jane@example.com",
                "S3cure!Pass"));
        assertThat(response.user().email()).isEqualTo("jane@example.com");
        assertThat(response.accessToken()).isNotBlank();
        assertThat(response.refreshToken()).isNotBlank();
        assertThat(userRepository.findByEmail("jane@example.com")).isPresent();
    }

    @Test
    void loginFailed() {
        authService.signUp(new SignUpRequest(
                "jane@example.com",
                "S3cure!Pass",
                "Jane Doe"
        ));
        assertThrows(InvalidCredentialsException.class, () -> authService.login(new LoginRequest(
                "janine@example.com",
                "S3cure!Pass")));
        assertThrows(InvalidCredentialsException.class, () -> authService.login(new LoginRequest(
                "jane@example.com",
                "Pass")));
    }

    @Test // TODO
    void loginInactiveThrowsAccountInactiveException() {
//        authService.signUp(new SignUpRequest(
//            "admin@example.com",
//            "S3cure!Pass",
//            "Admin User",
//            Role.ADMIN
//        ));
//        assertThrows(AccountInactiveException.class, () -> authService.login(new LoginRequest(
//                "admin@example.com",
//                "S3cure!Pass"
//        )));
    }

    @Test
    void refreshSuccess() {
        AuthResponse authResponse = authService.signUp(new SignUpRequest(
                "jane@example.com",
                "S3cure!Pass",
                "Jane Doe"
        ));
        AccessTokenResponse response = authService.refresh(new RefreshRequest(authResponse.refreshToken()));
        assertThat(response.accessToken()).isNotBlank();
    }

    @Test
    void refreshFailedTokenNotFound() {
        AuthResponse authResponse = authService.signUp(new SignUpRequest(
                "jane@example.com",
                "S3cure!Pass",
                "Jane Doe"
        ));
        refreshTokenRepository.findByUserId(authResponse.user().id()).ifPresent(refreshTokenRepository::delete);
        assertThrows(InvalidRefreshTokenException.class, () -> authService.refresh(new RefreshRequest(
                authResponse.refreshToken())));
    }

    @Test
    void refreshFailedTokenExpired() {
        AuthResponse authResponse = authService.signUp(new SignUpRequest(
                "jane@example.com",
                "S3cure!Pass",
                "Jane Doe"
        ));
        refreshTokenRepository.findByUserId(authResponse.user().id()).ifPresent(refreshToken -> {
            refreshToken.setExpiresAt(Instant.now().minusSeconds(3600));
            refreshTokenRepository.save(refreshToken);
        });
        assertThrows(InvalidRefreshTokenException.class, () -> authService.refresh(new RefreshRequest(
                authResponse.refreshToken())));
    }
}
