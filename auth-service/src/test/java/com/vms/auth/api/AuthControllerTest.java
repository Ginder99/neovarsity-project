package com.vms.auth.api;

import com.vms.auth.dto.*;
import com.vms.auth.entity.PasswordResetToken;
import com.vms.auth.entity.Role;
import com.vms.auth.entity.User;
import com.vms.auth.repository.PasswordResetTokenRepository;
import com.vms.auth.repository.RefreshTokenRepository;
import com.vms.auth.repository.UserRepository;
import com.vms.auth.service.AuthService;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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

    private ResultActions signUpSuccess() throws Exception {
        SignUpRequest request = new SignUpRequest("jane@example.com", "S3cure!Pass", "Jane Doe");
        return mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void signupReturnsCreatedResponse() throws Exception {
        SignUpRequest request = new SignUpRequest("jane@example.com", "S3cure!Pass", "Jane Doe");

        mockMvc.perform(post("/api/v1/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.user.id", not(emptyString())))
                .andExpect(jsonPath("$.user.name", is("Jane Doe")))
                .andExpect(jsonPath("$.user.email", is("jane@example.com")))
                .andExpect(jsonPath("$.user.role", is("CONSUMER")))
                .andExpect(jsonPath("$.user.is_active", is(true)))
                .andExpect(jsonPath("$.user.created_at", notNullValue()))
                .andExpect(jsonPath("$.access_token", not(emptyString())))
                .andExpect(jsonPath("$.refresh_token", not(emptyString())));
    }

    @Test
    void signupReturnsEmailExistsResponse() throws Exception {
        signUpSuccess();

        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new SignUpRequest("jane@example.com", "S3cure!Pass", "Jane Doe"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code", is("EMAIL_ALREADY_IN_USE")))
                .andExpect(jsonPath("$.message", is("Email already in use: jane@example.com")));
    }

    @Test
    void loginReturnsAccessToken() throws Exception {
        signUpSuccess();

        LoginRequest loginRequest = new LoginRequest("jane@example.com", "S3cure!Pass");
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.id", not(emptyString())))
                .andExpect(jsonPath("$.user.name", is("Jane Doe")))
                .andExpect(jsonPath("$.user.email", is("jane@example.com")))
                .andExpect(jsonPath("$.user.role", is("CONSUMER")))
                .andExpect(jsonPath("$.user.is_active", is(true)))
                .andExpect(jsonPath("$.user.created_at", notNullValue()))
                .andExpect(jsonPath("$.access_token", not(emptyString())))
                .andExpect(jsonPath("$.refresh_token", not(emptyString())));
    }

    @Test
    void loginReturnsInvalidCredentials() throws Exception {
        signUpSuccess();
        LoginRequest loginRequest = new LoginRequest("janet@example.com", "S3cure!Pass");

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code", is("INVALID_CREDENTIALS")))
                .andExpect(jsonPath("$.message", is("Invalid Credentials.")));

        loginRequest = new LoginRequest("jane@example.com", "S3cure");
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code", is("INVALID_CREDENTIALS")))
                .andExpect(jsonPath("$.message", is("Invalid Credentials.")));
    }

    @Test // TODO
    void loginInactiveReturnsForbidden() throws Exception {
//        SignUpRequest request = new SignUpRequest("admin@example.com", "S3cure!Pass", "Admin User");
//        mockMvc.perform(post("/api/v1/auth/signup")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isCreated());
//
//        LoginRequest loginRequest = new LoginRequest("admin@example.com", "S3cure!Pass");
//        mockMvc.perform(post("/api/v1/auth/login")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(loginRequest)))
//                .andExpect(status().isForbidden())
//                .andExpect(jsonPath("$.code", is("ACCOUNT_INACTIVE")))
//                .andExpect(jsonPath("$.message", is("Account is inactive. Please call support to activate your account.")));
    }

    @ParameterizedTest()
    @CsvSource({
            ", S3cure!Pass, Email is Required",
            "abc@bbc.com, , Password is Required",
            "abc@bbc, S3cure!Pass, Please provide a valid email address"})
    void signUpFailedDueToInvalidInputs(String email, String password, String errorMessage) throws Exception {
        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new SignUpRequest(email, password, "Jane Doe"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("INVALID_INPUT")))
                .andExpect(jsonPath("$.message", is(errorMessage)))
                .andExpect(jsonPath("$.status", is(400)));
    }

    @Test
    void refreshSuccess() throws Exception {
        ResultActions resultActions = signUpSuccess();
        resultActions.andExpect(jsonPath("$.refresh_token", not(emptyString())));
        String refreshToken = objectMapper.readTree(resultActions.andReturn().getResponse().getContentAsString()).get("refresh_token").asString();
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RefreshRequest(refreshToken))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token", not(emptyString())));
    }

    @Test
    void refreshFailed() throws Exception {
        ResultActions resultActions = signUpSuccess();
        resultActions.andExpect(jsonPath("$.refresh_token", not(emptyString())));
        String refreshToken = objectMapper.readTree(resultActions.andReturn().getResponse().getContentAsString()).get("refresh_token").asString();
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RefreshRequest(refreshToken + "invalid"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code", is("INVALID_REFRESH_TOKEN")))
                .andExpect(jsonPath("$.message", is("Invalid refresh token.")))
                .andExpect(jsonPath("$.status", is(401)));
    }


    @Test
    void testToken() throws Exception {
        ResultActions resultActions = signUpSuccess();
        resultActions.andExpect(jsonPath("$.access_token", not(emptyString())));
        String token = objectMapper.readTree(resultActions.andReturn().getResponse().getContentAsString()).get("access_token").asString();
        mockMvc.perform(get("/api/v1/auth/test-token")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is("Jane Doe")));
    }

    @Test
    void adminCreateUserSuccess() throws Exception {
        userRepository.save(new User("admin@example.com", "Admin User", passwordEncoder.encode("S3cure!Pass"), Role.ADMIN, true));

        LoginRequest loginReq = new LoginRequest("admin@example.com", "S3cure!Pass");
        String loginRes = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String adminToken = objectMapper.readTree(loginRes).get("access_token").asString();

        CreateUserRequest createReq = new CreateUserRequest("newconsumer@example.com", "New Consumer", Role.CONSUMER);
        mockMvc.perform(post("/api/v1/auth/admin/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isNoContent());
        Optional<User> userOpt = userRepository.findByEmail("newconsumer@example.com");
        assertThat(userOpt).isPresent();
        PasswordResetToken resetToken = passwordResetTokenRepository.findByUser(userOpt.get());
        assertNotNull(resetToken);
        assertEquals("NEW_ACCOUNT_ACTIVATION", resetToken.getPurpose());
    }

    @Test
    void adminCreateUserForbiddenForNonAdmin() throws Exception {
        ResultActions resultActions = signUpSuccess();
        String userToken = objectMapper.readTree(resultActions.andReturn().getResponse().getContentAsString()).get("access_token").asString();

        CreateUserRequest createReq = new CreateUserRequest("newconsumer@example.com", "New Consumer", Role.CONSUMER);
        mockMvc.perform(post("/api/v1/auth/admin/users")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCreateUserUnauthorized() throws Exception {
        CreateUserRequest createReq = new CreateUserRequest("newconsumer@example.com", "New Consumer", Role.CONSUMER);
        mockMvc.perform(post("/api/v1/auth/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void forgotPasswordReturnsNoContent() throws Exception {
        ResultActions resultActions = signUpSuccess();
        AuthResponse response = objectMapper.readValue(resultActions.andReturn().getResponse().getContentAsString(), AuthResponse.class);
        assertNotNull(response);
        assertNotNull(response.user());
        ForgotPasswordRequest request = new ForgotPasswordRequest("jane@example.com");

        mockMvc.perform(post("/api/v1/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());
        assertThat(passwordResetTokenRepository.count()).isEqualTo(1);
        Optional<User> userOpt = userRepository.findById(response.user().id());
        assertThat(userOpt).isPresent();
        PasswordResetToken resetToken = passwordResetTokenRepository.findByUser(userOpt.get());
        assertNotNull(resetToken);
        assertEquals("FORGOT_PASSWORD", resetToken.getPurpose());
    }

    @Test
    void forgotPasswordReturnsNotFound() throws Exception {
        signUpSuccess();
        ForgotPasswordRequest request = new ForgotPasswordRequest("jane@examples.com");

        mockMvc.perform(post("/api/v1/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code", is("RECORD_NOT_FOUND")))
                .andExpect(jsonPath("$.message", is("Couldn't find this Email")));
    }

    @Test
    void resetPasswordReturnsNoContent() throws Exception {
        signUpSuccess();
        User user = userRepository.findByEmail("jane@example.com").get();

        String rawToken = UUID.randomUUID().toString();
        String hashedToken = authService.generateSHA256Hash(rawToken);
        passwordResetTokenRepository.save(new PasswordResetToken(user, hashedToken, Instant.now().plusSeconds(3600), "FORGOT_PASSWORD"));

        ResetPasswordRequest request = new ResetPasswordRequest(rawToken, "NewS3cure!Pass");

        mockMvc.perform(post("/api/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());
    }

    @Test
    void resetPasswordReturnsBadRequestForInvalidToken() throws Exception {
        ResetPasswordRequest request = new ResetPasswordRequest("invalid-token", "NewS3cure!Pass");

        mockMvc.perform(post("/api/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("INVALID_RESET_TOKEN")))
                .andExpect(jsonPath("$.message", is("Invalid or expired reset token")));
    }
}
