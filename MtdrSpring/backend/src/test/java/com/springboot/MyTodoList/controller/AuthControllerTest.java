package com.springboot.MyTodoList.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springboot.MyTodoList.dto.auth.AuthResponse;
import com.springboot.MyTodoList.dto.auth.LoginRequest;
import com.springboot.MyTodoList.dto.auth.SignupRequest;
import com.springboot.MyTodoList.dto.auth.UpdateProfileRequest;
import com.springboot.MyTodoList.dto.auth.UserProfileResponse;
import com.springboot.MyTodoList.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @Test
    void signupReturnsTokenAndSafeProfile() throws Exception {
        SignupRequest request = signupRequest();
        when(authService.signup(any(SignupRequest.class))).thenReturn(authResponse());

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.user.userId").value("user-1"))
                .andExpect(jsonPath("$.user.email").value("user@example.com"))
                .andExpect(jsonPath("$.user.passwordHash").doesNotExist());
    }

    @Test
    void loginReturnsGenericErrorForInvalidCredentials() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("user@example.com");
        request.setPassword("wrongPassword");
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new BadCredentialsException("Invalid email or password"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    @Test
    void meReturnsCurrentUser() throws Exception {
        when(authService.getCurrentUser("user-1")).thenReturn(userProfile());

        mockMvc.perform(get("/auth/me").principal(authentication()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("user-1"))
                .andExpect(jsonPath("$.phoneNumber").value("3312345678"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void updateMeUpdatesCurrentUser() throws Exception {
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setName("Updated Name");
        when(authService.updateCurrentUser(eq("user-1"), any(UpdateProfileRequest.class))).thenReturn(userProfile());

        mockMvc.perform(patch("/auth/me")
                        .principal(authentication())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("user-1"));
    }

    private SignupRequest signupRequest() {
        SignupRequest request = new SignupRequest();
        request.setName("User Name");
        request.setEmail("user@example.com");
        request.setPassword("plainPassword");
        return request;
    }

    private AuthResponse authResponse() {
        return new AuthResponse("jwt-token", userProfile());
    }

    private UserProfileResponse userProfile() {
        return new UserProfileResponse(
                "user-1",
                "User Name",
                "user@example.com",
                "123456",
                "3312345678"
        );
    }

    private UsernamePasswordAuthenticationToken authentication() {
        return new UsernamePasswordAuthenticationToken("user-1", null);
    }
}
