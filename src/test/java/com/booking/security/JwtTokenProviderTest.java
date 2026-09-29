package com.booking.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    // Base64 encoded 256-bit key for testing
    private static final String TEST_SECRET =
            "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private static final long TEST_EXPIRATION = 86400000L; // 24 hours

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(TEST_SECRET, TEST_EXPIRATION);
    }

    @Test
    @DisplayName("Should generate valid JWT token")
    void shouldGenerateToken() {
        Authentication auth = createAuthentication("testuser", "ROLE_USER");
        String token = jwtTokenProvider.generateToken(auth);

        assertThat(token).isNotNull().isNotBlank();
    }

    @Test
    @DisplayName("Should extract username from token")
    void shouldExtractUsername() {
        Authentication auth = createAuthentication("admin", "ROLE_ADMIN");
        String token = jwtTokenProvider.generateToken(auth);

        String username = jwtTokenProvider.getUsernameFromToken(token);
        assertThat(username).isEqualTo("admin");
    }

    @Test
    @DisplayName("Should validate correct token")
    void shouldValidateCorrectToken() {
        Authentication auth = createAuthentication("user", "ROLE_USER");
        String token = jwtTokenProvider.generateToken(auth);

        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
    }

    @Test
    @DisplayName("Should reject invalid token")
    void shouldRejectInvalidToken() {
        assertThat(jwtTokenProvider.validateToken("invalid.token.here")).isFalse();
    }

    @Test
    @DisplayName("Should reject null token")
    void shouldRejectNullToken() {
        assertThat(jwtTokenProvider.validateToken(null)).isFalse();
    }

    @Test
    @DisplayName("Should reject empty token")
    void shouldRejectEmptyToken() {
        assertThat(jwtTokenProvider.validateToken("")).isFalse();
    }

    @Test
    @DisplayName("Should reject expired token")
    void shouldRejectExpiredToken() {
        // Create provider with 0ms expiration
        JwtTokenProvider expiredProvider = new JwtTokenProvider(TEST_SECRET, 0L);
        Authentication auth = createAuthentication("user", "ROLE_USER");
        String token = expiredProvider.generateToken(auth);

        assertThat(expiredProvider.validateToken(token)).isFalse();
    }

    private Authentication createAuthentication(String username, String role) {
        return new UsernamePasswordAuthenticationToken(
                username, null,
                Collections.singletonList(new SimpleGrantedAuthority(role)));
    }
}
