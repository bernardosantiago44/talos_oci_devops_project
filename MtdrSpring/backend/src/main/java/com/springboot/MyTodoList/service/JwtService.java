package com.springboot.MyTodoList.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.springboot.MyTodoList.model.AppUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class JwtService {
    private static final Base64.Encoder BASE64_URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder BASE64_URL_DECODER = Base64.getUrlDecoder();
    private static final TypeReference<Map<String, Object>> CLAIMS_TYPE = new TypeReference<>() {};

    private final ObjectMapper objectMapper;
    private final String secret;
    private final long expirationMinutes;

    public JwtService(
            ObjectMapper objectMapper,
            @Value("${jwt.secret:}") String secret,
            @Value("${jwt.expiration-minutes:1440}") long expirationMinutes
    ) {
        this.objectMapper = objectMapper;
        this.secret = secret;
        this.expirationMinutes = expirationMinutes;
    }

    public String generateToken(AppUser user) {
        ensureSecretConfigured();

        Instant now = Instant.now();
        Map<String, Object> header = Map.of(
                "alg", "HS256",
                "typ", "JWT"
        );
        Map<String, Object> claims = new LinkedHashMap<>();
        claims.put("sub", user.getUserId());
        claims.put("iat", now.getEpochSecond());
        claims.put("exp", now.plusSeconds(expirationMinutes * 60).getEpochSecond());

        String unsignedToken = encodeJson(header) + "." + encodeJson(claims);
        return unsignedToken + "." + sign(unsignedToken);
    }

    public Optional<String> extractUserId(String token) {
        ensureSecretConfigured();

        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            return Optional.empty();
        }

        String unsignedToken = parts[0] + "." + parts[1];
        if (!hasValidSignature(unsignedToken, parts[2])) {
            return Optional.empty();
        }

        return readClaims(parts[1])
                .filter(this::hasNotExpired)
                .map(claims -> claims.get("sub"))
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .filter(userId -> !userId.isBlank());
    }

    private String encodeJson(Map<String, Object> value) {
        try {
            return BASE64_URL_ENCODER.encodeToString(objectMapper.writeValueAsBytes(value));
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to encode JWT payload", exception);
        }
    }

    private Optional<Map<String, Object>> readClaims(String encodedPayload) {
        try {
            byte[] payload = BASE64_URL_DECODER.decode(encodedPayload);
            return Optional.of(objectMapper.readValue(payload, CLAIMS_TYPE));
        } catch (Exception exception) {
            return Optional.empty();
        }
    }

    private boolean hasNotExpired(Map<String, Object> claims) {
        Object expiration = claims.get("exp");
        if (!(expiration instanceof Number expirationNumber)) {
            return false;
        }

        return Instant.now().getEpochSecond() < expirationNumber.longValue();
    }

    private boolean hasValidSignature(String unsignedToken, String providedSignature) {
        String expectedSignature = sign(unsignedToken);
        return MessageDigest.isEqual(
                expectedSignature.getBytes(StandardCharsets.UTF_8),
                providedSignature.getBytes(StandardCharsets.UTF_8)
        );
    }

    private String sign(String value) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return BASE64_URL_ENCODER.encodeToString(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to sign JWT", exception);
        }
    }

    private void ensureSecretConfigured() {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("JWT secret must be configured with JWT_SECRET.");
        }
    }
}
