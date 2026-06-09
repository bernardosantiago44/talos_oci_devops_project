package com.springboot.MyTodoList.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springboot.MyTodoList.dto.auth.AuthResponse;
import com.springboot.MyTodoList.dto.auth.SignupRequest;
import com.springboot.MyTodoList.dto.auth.UserProfileResponse;
import com.springboot.MyTodoList.model.AppUserSummary;
import com.springboot.MyTodoList.repository.AppUserRepository;
import com.springboot.MyTodoList.service.AppUserService;
import com.springboot.MyTodoList.service.AuthService;
import com.springboot.MyTodoList.service.JwtService;
import com.springboot.MyTodoList.testdata.TestDataFactory;
import jakarta.servlet.DispatcherType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({
        com.springboot.MyTodoList.controller.AppUserController.class,
        com.springboot.MyTodoList.controller.AuthController.class
})
@Import(WebSecurityConfiguration.class)
class WebSecurityConfigurationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AppUserService appUserService;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private AppUserRepository appUserRepository;

    @Test
    void signupEndpointStaysPublicEvenWithInvalidBearerHeader() throws Exception {
        SignupRequest request = new SignupRequest();
        request.setName("Ada Lovelace");
        request.setEmail("ada@example.com");
        request.setPassword("plainPassword");
        when(authService.signup(any(SignupRequest.class))).thenReturn(new AuthResponse(
                "jwt-token",
                new UserProfileResponse("user-1", "Ada Lovelace", "ada@example.com", null, null)
        ));

        mockMvc.perform(post("/api/auth/signup")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer bad-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("jwt-token"));

        verify(jwtService, never()).extractUserId("bad-token");
    }

    @Test
    void authProfileEndpointRemainsProtected() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"));
    }

    @Test
    void errorDispatchIsNotMaskedAsAuthenticationFailure() throws Exception {
        mockMvc.perform(get("/error").with(request -> {
                    request.setDispatcherType(DispatcherType.ERROR);
                    return request;
                }))
                .andExpect(result -> assertThat(result.getResponse().getStatus()).isNotEqualTo(401));
    }

    @Test
    void protectedEndpointRejectsMissingToken() throws Exception {
        mockMvc.perform(get("/api/appusers"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"));
    }

    @Test
    void protectedEndpointRejectsInvalidToken() throws Exception {
        when(jwtService.extractUserId("bad-token")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/appusers")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer bad-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"));
    }

    @Test
    void protectedEndpointAllowsValidToken() throws Exception {
        when(jwtService.extractUserId("valid-token")).thenReturn(Optional.of("user-1"));
        when(appUserRepository.findById("user-1")).thenReturn(Optional.of(TestDataFactory.appUser("user-1")));
        when(appUserService.findAll()).thenReturn(List.of(
                new AppUserSummary("user-1", "Ada Lovelace", "ada@example.com", "telegram-user-1")
        ));

        mockMvc.perform(get("/api/appusers")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value("user-1"));
    }
}
