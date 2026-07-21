package com.vms.auth.service;

import com.vms.auth.dto.*;
import com.vms.auth.entity.Role;
import com.vms.auth.repository.RefreshTokenRepository;
import com.vms.auth.repository.UserRepository;
import com.vms.auth.service.exceptions.AccountInactiveException;
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
            "Jane Doe",
            Role.CONSUMER
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
                "Jane Doe",
                Role.CONSUMER
        ));
        assertThrows(EmailAlreadyInUseException.class, () -> authService.signUp(new SignUpRequest(
                "jane@example.com",
                "S3cure!Pass",
                "Jane Doe",
                Role.CONSUMER
        )));
    }

    @Test
    void signupAdminIsInactiveAndReturnsMessage() {
        AuthResponse response = authService.signUp(new SignUpRequest(
            "admin@example.com",
            "S3cure!Pass",
            "Admin User",
            Role.ADMIN
        ));
        assertThat(response.user().email()).isEqualTo("admin@example.com");
        assertThat(response.user().role()).isEqualTo(Role.ADMIN);
        assertThat(response.user().isActive()).isFalse();
        assertThat(response.accessToken()).isNull();
        assertThat(response.refreshToken()).isNull();
        assertThat(response.message()).isEqualTo("Your account is created but inactive. Please call support to activate your account.");
    }

    @Test
    void loginSuccess() {
        authService.signUp(new SignUpRequest(
                "jane@example.com",
                "S3cure!Pass",
                "Jane Doe",
                Role.CONSUMER
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
                "Jane Doe",
                Role.CONSUMER
        ));
        assertThrows(InvalidCredentialsException.class, () -> authService.login(new LoginRequest(
                "janine@example.com",
                "S3cure!Pass")));
        assertThrows(InvalidCredentialsException.class, () -> authService.login(new LoginRequest(
                "jane@example.com",
                "Pass")));
    }

    @Test
    void loginInactiveThrowsAccountInactiveException() {
        authService.signUp(new SignUpRequest(
            "admin@example.com",
            "S3cure!Pass",
            "Admin User",
            Role.ADMIN
        ));
        assertThrows(AccountInactiveException.class, () -> authService.login(new LoginRequest(
                "admin@example.com",
                "S3cure!Pass"
        )));
    }

    @Test
    void refreshSuccess() {
        AuthResponse authResponse = authService.signUp(new SignUpRequest(
                "jane@example.com",
                "S3cure!Pass",
                "Jane Doe",
                Role.CONSUMER
        ));
        AccessTokenResponse response = authService.refresh(new RefreshRequest(authResponse.refreshToken()));
        assertThat(response.accessToken()).isNotBlank();
    }

    @Test
    void refreshFailedTokenNotFound() {
        AuthResponse authResponse = authService.signUp(new SignUpRequest(
                "jane@example.com",
                "S3cure!Pass",
                "Jane Doe",
                Role.CONSUMER
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
                "Jane Doe",
                Role.CONSUMER
        ));
        refreshTokenRepository.findByUserId(authResponse.user().id()).ifPresent(refreshToken -> {
            refreshToken.setExpiresAt(Instant.now().minusSeconds(3600));
            refreshTokenRepository.save(refreshToken);
        });
        assertThrows(InvalidRefreshTokenException.class, () -> authService.refresh(new RefreshRequest(
                authResponse.refreshToken())));
    }

    @Test
    void adminCreateUserSuccess() {
        AdminCreateUserResponse response = authService.adminCreateUser(new CreateUserRequest(
                "newuser@example.com",
                "New User",
                Role.CONSUMER
        ));
        assertThat(response.user().email()).isEqualTo("newuser@example.com");
        assertThat(response.user().name()).isEqualTo("New User");
        assertThat(response.user().role()).isEqualTo(Role.CONSUMER);
        assertThat(response.user().isActive()).isFalse();
        assertThat(response.tempPassword()).startsWith("TEMP_");
        assertThat(userRepository.findByEmail("newuser@example.com")).isPresent();
    }

    @Test
    void adminCreateUserThrowsEmailExistsError() {
        authService.signUp(new SignUpRequest(
                "newuser@example.com",
                "S3cure!Pass",
                "Existing User",
                Role.CONSUMER
        ));
        assertThrows(EmailAlreadyInUseException.class, () -> authService.adminCreateUser(new CreateUserRequest(
                "newuser@example.com",
                "Another Name",
                Role.MACHINE_HANDLER
        )));
    }
}
