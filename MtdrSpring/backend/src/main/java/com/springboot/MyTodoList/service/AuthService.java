package com.springboot.MyTodoList.service;

import com.springboot.MyTodoList.dto.auth.AuthResponse;
import com.springboot.MyTodoList.dto.auth.LoginRequest;
import com.springboot.MyTodoList.dto.auth.SignupRequest;
import com.springboot.MyTodoList.dto.auth.UpdateProfileRequest;
import com.springboot.MyTodoList.dto.auth.UserProfileResponse;
import com.springboot.MyTodoList.exception.AppUserNotFoundException;
import com.springboot.MyTodoList.exception.BusinessRuleException;
import com.springboot.MyTodoList.model.AppUser;
import com.springboot.MyTodoList.repository.AppUserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private static final String INVALID_CREDENTIALS_MESSAGE = "Invalid email or password";

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            AppUserRepository appUserRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse signup(SignupRequest request) {
        String email = normalizeEmail(request.getEmail());
        if (appUserRepository.existsByEmailIgnoreCase(email)) {
            throw new BusinessRuleException("Email is already registered");
        }

        AppUser user = new AppUser();
        user.setName(request.getName().trim());
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        AppUser savedUser = appUserRepository.save(user);
        return toAuthResponse(savedUser);
    }

    public AuthResponse login(LoginRequest request) {
        String email = normalizeEmail(request.getEmail());
        AppUser user = appUserRepository
                .findByEmailIgnoreCase(email)
                .orElseThrow(() -> new BadCredentialsException(INVALID_CREDENTIALS_MESSAGE));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException(INVALID_CREDENTIALS_MESSAGE);
        }

        return toAuthResponse(user);
    }

    public UserProfileResponse getCurrentUser(String userId) {
        return appUserRepository
                .findById(userId)
                .map(this::toProfile)
                .orElseThrow(() -> new AppUserNotFoundException(userId));
    }

    @Transactional
    public UserProfileResponse updateCurrentUser(String userId, UpdateProfileRequest request) {
        AppUser user = appUserRepository
                .findById(userId)
                .orElseThrow(() -> new AppUserNotFoundException(userId));

        updateName(user, request.getName());
        updateTelegramUserId(user, request.getTelegramUserId());
        updatePhoneNumber(user, request.getPhoneNumber());
        updatePassword(user, request.getPassword());

        return toProfile(appUserRepository.save(user));
    }

    private void updateName(AppUser user, String name) {
        if (name == null) {
            return;
        }

        String normalizedName = name.trim();
        if (normalizedName.isBlank()) {
            throw new BusinessRuleException("Name cannot be blank");
        }

        user.setName(normalizedName);
    }

    private void updateTelegramUserId(AppUser user, String telegramUserId) {
        if (telegramUserId == null) {
            return;
        }

        String normalizedTelegramUserId = telegramUserId.trim();
        if (normalizedTelegramUserId.isBlank()) {
            user.setTelegramUserId(null);
            return;
        }

        boolean isUsedByAnotherUser = appUserRepository.existsByTelegramUserIdAndUserIdNot(
                normalizedTelegramUserId,
                user.getUserId()
        );
        if (isUsedByAnotherUser) {
            throw new BusinessRuleException("Telegram user ID is already registered");
        }

        user.setTelegramUserId(normalizedTelegramUserId);
    }

    private void updatePhoneNumber(AppUser user, String phoneNumber) {
        if (phoneNumber == null) {
            return;
        }

        String normalizedPhoneNumber = phoneNumber.trim();
        user.setPhoneNumber(normalizedPhoneNumber.isBlank() ? null : normalizedPhoneNumber);
    }

    private void updatePassword(AppUser user, String password) {
        if (password == null) {
            return;
        }

        user.setPasswordHash(passwordEncoder.encode(password));
    }

    private AuthResponse toAuthResponse(AppUser user) {
        return new AuthResponse(jwtService.generateToken(user), toProfile(user));
    }

    private UserProfileResponse toProfile(AppUser user) {
        return new UserProfileResponse(
                user.getUserId(),
                user.getName(),
                user.getEmail(),
                user.getTelegramUserId(),
                user.getPhoneNumber()
        );
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }
}
