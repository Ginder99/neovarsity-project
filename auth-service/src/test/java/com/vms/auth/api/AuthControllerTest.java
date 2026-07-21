package com.vms.auth.api;

import com.vms.auth.dto.LoginRequest;
import com.vms.auth.dto.RefreshRequest;
import com.vms.auth.dto.SignUpRequest;
import com.vms.auth.dto.CreateUserRequest;
import com.vms.auth.entity.Role;
import com.vms.auth.entity.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.vms.auth.repository.RefreshTokenRepository;
import com.vms.auth.repository.UserRepository;
import com.vms.auth.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import tools.jackson.databind.ObjectMapper;

import static org.hamcrest.Matchers.*;
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
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    private ResultActions signUpSuccess() throws Exception {
        SignUpRequest request = new SignUpRequest("jane@example.com", "S3cure!Pass", "Jane Doe", Role.CONSUMER);
        return mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void signupReturnsCreatedResponse() throws Exception {
        SignUpRequest request = new SignUpRequest("jane@example.com", "S3cure!Pass", "Jane Doe", Role.CONSUMER);

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
                        .content(objectMapper.writeValueAsString(new SignUpRequest("jane@example.com", "S3cure!Pass", "Jane Doe", Role.CONSUMER))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code", is("EMAIL_ALREADY_IN_USE")))
                .andExpect(jsonPath("$.message", is("Email already in use: jane@example.com")));
    }

    @Test
    void signupAdminReturnsCreatedWithoutTokens() throws Exception {
        SignUpRequest request = new SignUpRequest("admin@example.com", "S3cure!Pass", "Admin User", Role.ADMIN);

        mockMvc.perform(post("/api/v1/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.user.id", not(emptyString())))
                .andExpect(jsonPath("$.user.name", is("Admin User")))
                .andExpect(jsonPath("$.user.email", is("admin@example.com")))
                .andExpect(jsonPath("$.user.role", is("ADMIN")))
                .andExpect(jsonPath("$.user.is_active", is(false)))
                .andExpect(jsonPath("$.access_token", nullValue()))
                .andExpect(jsonPath("$.refresh_token", nullValue()))
                .andExpect(jsonPath("$.message", is("Your account is created but inactive. Please call support to activate your account.")));
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

    @Test
    void loginInactiveReturnsForbidden() throws Exception {
        SignUpRequest request = new SignUpRequest("admin@example.com", "S3cure!Pass", "Admin User", Role.ADMIN);
        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        LoginRequest loginRequest = new LoginRequest("admin@example.com", "S3cure!Pass");
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code", is("ACCOUNT_INACTIVE")))
                .andExpect(jsonPath("$.message", is("Account is inactive. Please call support to activate your account.")));
    }

    @ParameterizedTest()
    @CsvSource({
            ", S3cure!Pass, CONSUMER, Email is Required",
            "abc@bbc.com, , CONSUMER, Password is Required",
            "abc@bbc, S3cure!Pass, CONSUMER, Please provide a valid email address",
            "abc@bbc.com, S3cure!Pass, , Role is Required"})
    void signUpFailedDueToInvalidInputs(String email, String password, Role role, String errorMessage) throws Exception {
        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new SignUpRequest(email, password, "Jane Doe", role))))
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
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.user.email", is("newconsumer@example.com")))
                .andExpect(jsonPath("$.user.name", is("New Consumer")))
                .andExpect(jsonPath("$.user.role", is("CONSUMER")))
                .andExpect(jsonPath("$.user.is_active", is(false)))
                .andExpect(jsonPath("$.temp_password", startsWith("TEMP_")));
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
}
