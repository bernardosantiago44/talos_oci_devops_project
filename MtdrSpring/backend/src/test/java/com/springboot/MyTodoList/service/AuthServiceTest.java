package com.springboot.MyTodoList.service;

import com.springboot.MyTodoList.dto.auth.AuthResponse;
import com.springboot.MyTodoList.dto.auth.LoginRequest;
import com.springboot.MyTodoList.dto.auth.SignupRequest;
import com.springboot.MyTodoList.dto.auth.UpdateProfileRequest;
import com.springboot.MyTodoList.exception.BusinessRuleException;
import com.springboot.MyTodoList.model.AppUser;
import com.springboot.MyTodoList.repository.AppUserRepository;
import com.springboot.MyTodoList.testdata.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private JwtService jwtService;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private AuthService service;

    @BeforeEach
    void setUp() {
        service = new AuthService(appUserRepository, passwordEncoder, jwtService);
    }

    @Test
    void signupHashesPasswordAndReturnsSafeProfile() {
        SignupRequest request = signupRequest();
        when(appUserRepository.existsByEmailIgnoreCase("user@example.com")).thenReturn(false);
        when(appUserRepository.save(any(AppUser.class))).thenAnswer(invocation -> {
            AppUser user = invocation.getArgument(0);
            user.setUserId("user-1");
            return user;
        });
        when(jwtService.generateToken(any(AppUser.class))).thenReturn("jwt-token");

        AuthResponse response = service.signup(request);

        ArgumentCaptor<AppUser> userCaptor = ArgumentCaptor.forClass(AppUser.class);
        verify(appUserRepository).save(userCaptor.capture());
        AppUser savedUser = userCaptor.getValue();

        assertThat(savedUser.getEmail()).isEqualTo("user@example.com");
        assertThat(savedUser.getPasswordHash()).isNotEqualTo("plainPassword");
        assertThat(passwordEncoder.matches("plainPassword", savedUser.getPasswordHash())).isTrue();
        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.user().userId()).isEqualTo("user-1");
        assertThat(response.user().email()).isEqualTo("user@example.com");
    }

    @Test
    void signupRejectsDuplicateEmail() {
        SignupRequest request = signupRequest();
        when(appUserRepository.existsByEmailIgnoreCase("user@example.com")).thenReturn(true);

        assertThatThrownBy(() -> service.signup(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Email is already registered");
    }

    @Test
    void loginReturnsTokenForValidCredentials() {
        LoginRequest request = loginRequest();
        AppUser user = TestDataFactory.appUser("user-1");
        user.setEmail("user@example.com");
        user.setPasswordHash(passwordEncoder.encode("plainPassword"));

        when(appUserRepository.findByEmailIgnoreCase("user@example.com")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("jwt-token");

        AuthResponse response = service.login(request);

        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.user().userId()).isEqualTo("user-1");
        assertThat(response.user().email()).isEqualTo("user@example.com");
    }

    @Test
    void loginUsesGenericErrorWhenEmailDoesNotExist() {
        LoginRequest request = loginRequest();
        when(appUserRepository.findByEmailIgnoreCase("user@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.login(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid email or password");
    }

    @Test
    void loginUsesGenericErrorWhenPasswordDoesNotMatch() {
        LoginRequest request = loginRequest();
        AppUser user = TestDataFactory.appUser("user-1");
        user.setEmail("user@example.com");
        user.setPasswordHash(passwordEncoder.encode("differentPassword"));

        when(appUserRepository.findByEmailIgnoreCase("user@example.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> service.login(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid email or password");
    }

    @Test
    void updateCurrentUserUpdatesEditableFieldsAndHashesPassword() {
        AppUser user = TestDataFactory.appUser("user-1");
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setName("Updated Name");
        request.setTelegramUserId("123456");
        request.setPhoneNumber("3312345678");
        request.setPassword("newPassword");

        when(appUserRepository.findById("user-1")).thenReturn(Optional.of(user));
        when(appUserRepository.existsByTelegramUserIdAndUserIdNot("123456", "user-1")).thenReturn(false);
        when(appUserRepository.save(user)).thenReturn(user);

        var response = service.updateCurrentUser("user-1", request);

        assertThat(user.getName()).isEqualTo("Updated Name");
        assertThat(user.getTelegramUserId()).isEqualTo("123456");
        assertThat(user.getPhoneNumber()).isEqualTo("3312345678");
        assertThat(passwordEncoder.matches("newPassword", user.getPasswordHash())).isTrue();
        assertThat(response.telegramUserId()).isEqualTo("123456");
        assertThat(response.phoneNumber()).isEqualTo("3312345678");
    }

    @Test
    void updateCurrentUserRejectsDuplicateTelegramUserId() {
        AppUser user = TestDataFactory.appUser("user-1");
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setTelegramUserId("123456");

        when(appUserRepository.findById("user-1")).thenReturn(Optional.of(user));
        when(appUserRepository.existsByTelegramUserIdAndUserIdNot("123456", "user-1")).thenReturn(true);

        assertThatThrownBy(() -> service.updateCurrentUser("user-1", request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Telegram user ID is already registered");
    }

    private SignupRequest signupRequest() {
        SignupRequest request = new SignupRequest();
        request.setName("User Name");
        request.setEmail("USER@example.com");
        request.setPassword("plainPassword");
        return request;
    }

    private LoginRequest loginRequest() {
        LoginRequest request = new LoginRequest();
        request.setEmail("USER@example.com");
        request.setPassword("plainPassword");
        return request;
    }
}
