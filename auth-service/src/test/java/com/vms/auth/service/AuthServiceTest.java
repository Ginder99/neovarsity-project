package com.vms.auth.service;

import com.vms.auth.dto.AuthResponse;
import com.vms.auth.dto.SignUpRequest;
import com.vms.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class AuthServiceTest {
    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
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
        assertThat(response.accessToken()).isNotBlank();
        assertThat(response.refreshToken()).isNotBlank();
        assertThat(userRepository.findByEmail("jane@example.com")).isPresent();
    }
}
