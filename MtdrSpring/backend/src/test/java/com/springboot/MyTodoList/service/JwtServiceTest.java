package com.springboot.MyTodoList.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springboot.MyTodoList.model.AppUser;
import com.springboot.MyTodoList.testdata.TestDataFactory;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {
    private final JwtService jwtService = new JwtService(
            new ObjectMapper(),
            "test-secret-with-enough-length",
            60
    );

    @Test
    void generatedTokenCanBeValidated() {
        AppUser user = TestDataFactory.appUser("user-1");

        String token = jwtService.generateToken(user);

        assertThat(jwtService.extractUserId(token)).contains("user-1");
    }

    @Test
    void tamperedTokenIsRejected() {
        AppUser user = TestDataFactory.appUser("user-1");
        String token = jwtService.generateToken(user);

        String tamperedToken = token.substring(0, token.length() - 2) + "xx";

        assertThat(jwtService.extractUserId(tamperedToken)).isEqualTo(Optional.empty());
    }

    @Test
    void tokenGenerationFailsClearlyWhenSecretIsMissing() {
        JwtService misconfiguredJwtService = new JwtService(new ObjectMapper(), "", 60);

        assertThatThrownBy(() -> misconfiguredJwtService.generateToken(TestDataFactory.appUser("user-1")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("JWT secret must be configured with JWT_SECRET.");
    }
}
