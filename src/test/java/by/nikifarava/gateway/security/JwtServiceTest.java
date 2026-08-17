package by.nikifarava.gateway.security;

import by.nikifarava.gateway.config.GatewayProperties;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private static final String SECRET = "test-secret-key-test-secret-key-test-secret-key";

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        GatewayProperties properties = new GatewayProperties();
        properties.getJwt().setSecret(SECRET);
        jwtService = new JwtService(properties);
    }

    @Test
    @DisplayName("parse and validate - success")
    void parseWhenTokenValidShouldReturnClaims() {
        String token = token(SECRET, Instant.now().plusSeconds(60));

        assertTrue(jwtService.isValid(token));
    }

    @Test
    @DisplayName("validate token - false, bad token")
    void validateWhenTokenInvalidShouldReturnFalse() {
        assertFalse(jwtService.isValid("not-a-token"));
    }

    @Test
    @DisplayName("validate token - false, wrong secret")
    void validateWhenWrongSecretShouldReturnFalse() {
        String token = token("another-wrong-secret-another-wrong-secret-another-wrong-secret",
                Instant.now().plusSeconds(60));

        assertFalse(jwtService.isValid(token));
    }

    @Test
    @DisplayName("validate token - false, expired token")
    void validateWhenExpiredTokenShouldReturnFalse() {
        String token = token(SECRET, Instant.now().minusSeconds(60));

        assertFalse(jwtService.isValid(token));
    }

    private String token(String secret, Instant expiration) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .expiration(Date.from(expiration))
                .signWith(key)
                .compact();
    }
}
