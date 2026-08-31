package com.openclassrooms.etudiant.service;

import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class JwtServiceTest {

    // Doit faire au moins 32 caractères (256 bits) pour HS256, comme en application.yml.
    private static final String SECRET = "test-secret-key-at-least-32-bytes-long-for-hs256";
    private static final long EXPIRATION_MS = 3600000L; // 1h
    private static final String USERNAME = "roger";

    private JwtService jwtService;

    @BeforeEach
    public void setUp() {
        jwtService = new JwtService();
        // Pas de contexte Spring ici : on injecte les @Value manuellement.
        ReflectionTestUtils.setField(jwtService, "secret", SECRET);
        ReflectionTestUtils.setField(jwtService, "expirationMs", EXPIRATION_MS);
    }

    private UserDetails buildUserDetails(String username) {
        return org.springframework.security.core.userdetails.User.builder()
                .username(username)
                .password("irrelevant")
                .authorities(Collections.emptyList())
                .build();
    }

    // ---------- generateToken ----------

    @Test
    public void test_generateToken_returns_non_empty_token() {
        // GIVEN
        UserDetails userDetails = buildUserDetails(USERNAME);

        // WHEN
        String token = jwtService.generateToken(userDetails);

        // THEN
        assertThat(token).isNotBlank();
    }

    @Test
    public void test_generateToken_null_userDetails_throws_IllegalArgumentException() {
        // THEN
        assertThatThrownBy(() -> jwtService.generateToken(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ---------- extractUsername ----------

    @Test
    public void test_extractUsername_returns_username_used_at_generation() {
        // GIVEN
        UserDetails userDetails = buildUserDetails(USERNAME);
        String token = jwtService.generateToken(userDetails);

        // WHEN
        String extracted = jwtService.extractUsername(token);

        // THEN
        assertThat(extracted).isEqualTo(USERNAME);
    }

    // ---------- isTokenValid ----------

    @Test
    public void test_isTokenValid_returns_true_for_matching_user_and_valid_token() {
        // GIVEN
        UserDetails userDetails = buildUserDetails(USERNAME);
        String token = jwtService.generateToken(userDetails);

        // WHEN
        boolean valid = jwtService.isTokenValid(token, userDetails);

        // THEN
        assertThat(valid).isTrue();
    }

    @Test
    public void test_isTokenValid_returns_false_for_different_username() {
        // GIVEN
        UserDetails userDetails = buildUserDetails(USERNAME);
        String token = jwtService.generateToken(userDetails);
        UserDetails anotherUser = buildUserDetails("someone-else");

        // WHEN
        boolean valid = jwtService.isTokenValid(token, anotherUser);

        // THEN
        assertThat(valid).isFalse();
    }

    @Test
    public void test_isTokenValid_expired_token_throws_ExpiredJwtException() {
        // GIVEN : on force une expiration déjà dépassée (-1000 ms => expiré immédiatement)
        ReflectionTestUtils.setField(jwtService, "expirationMs", -1000L);
        UserDetails userDetails = buildUserDetails(USERNAME);
        String expiredToken = jwtService.generateToken(userDetails);

        // THEN
        // La librairie jjwt lève une exception dès le parsing d'un token expiré,
        // avant même que la comparaison de date dans isTokenValid() ne soit atteinte.
        assertThatThrownBy(() -> jwtService.isTokenValid(expiredToken, userDetails))
                .isInstanceOf(ExpiredJwtException.class);
    }
}
