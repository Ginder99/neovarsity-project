package com.vms.auth.service;

import com.vms.auth.dto.*;
import com.vms.auth.entity.PasswordResetToken;
import com.vms.auth.entity.Role;
import com.vms.auth.entity.User;
import com.vms.auth.repository.PasswordResetTokenRepository;
import com.vms.auth.repository.RefreshTokenRepository;
import com.vms.auth.repository.UserRepository;
import com.vms.auth.service.exceptions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
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

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        passwordResetTokenRepository.deleteAll();
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
                "Existing User"
        ));
        assertThrows(EmailAlreadyInUseException.class, () -> authService.adminCreateUser(new CreateUserRequest(
                "newuser@example.com",
                "Another Name",
                Role.MACHINE_HANDLER
        )));
    }

    @Test
    void forgotPasswordSuccess() {
        authService.signUp(new SignUpRequest("jane@example.com", "S3cure!Pass", "Jane Doe"));
        authService.forgotPassword(new ForgotPasswordRequest("jane@example.com"));
        assertThat(passwordResetTokenRepository.count()).isEqualTo(1);
    }

    @Test
    void forgotPasswordFailedEmailNotFound() {
        authService.signUp(new SignUpRequest("jane@example.com", "S3cure!Pass", "Jane Doe"));
        RecordNotFoundException exception = assertThrows(RecordNotFoundException.class, () -> authService.forgotPassword(new ForgotPasswordRequest("jane@examples.com")));
        assertEquals("Couldn't find this Email", exception.getMessage());
    }

    @Test
    void resetPasswordSuccess() {
        authService.signUp(new SignUpRequest("jane@example.com", "S3cure!Pass", "Jane Doe"));
        User user = userRepository.findByEmail("jane@example.com").get();

        String rawToken = UUID.randomUUID().toString();
        String hashedToken = authService.generateSHA256Hash(rawToken);
        passwordResetTokenRepository.save(new PasswordResetToken(user, hashedToken, Instant.now().plusSeconds(3600)));

        authService.resetPassword(new ResetPasswordRequest(rawToken, "NewS3cure!Pass"));
        
        assertThat(passwordEncoder.matches("NewS3cure!Pass", userRepository.findByEmail("jane@example.com").get().getPasswordHash())).isTrue();
        assertThat(passwordResetTokenRepository.count()).isEqualTo(0);
    }

    @Test
    void resetPasswordFailedInvalidToken() {
        authService.signUp(new SignUpRequest("jane@example.com", "S3cure!Pass", "Jane Doe"));
        User user = userRepository.findByEmail("jane@example.com").get();

        String rawToken = UUID.randomUUID().toString();
        String hashedToken = authService.generateSHA256Hash(rawToken);
        passwordResetTokenRepository.save(new PasswordResetToken(user, hashedToken, Instant.now().plusSeconds(3600)));

        assertThrows(InvalidResetTokenException.class, () -> authService.resetPassword(new ResetPasswordRequest("rawToken", "NewS3cure!Pass")));
    }

    @Test
    void resetPasswordFailedExpiredToken() throws Exception {
        authService.signUp(new SignUpRequest("jane@example.com", "S3cure!Pass", "Jane Doe"));
        User user = userRepository.findByEmail("jane@example.com").get();

        String rawToken = UUID.randomUUID().toString();
        String hashedToken = authService.generateSHA256Hash(rawToken);
        passwordResetTokenRepository.save(new PasswordResetToken(user, hashedToken, Instant.now().plusSeconds(1)));

        Thread.sleep(2000);
        assertThrows(InvalidResetTokenException.class, () -> authService.resetPassword(new ResetPasswordRequest(rawToken, "NewS3cure!Pass")));
    }
}
