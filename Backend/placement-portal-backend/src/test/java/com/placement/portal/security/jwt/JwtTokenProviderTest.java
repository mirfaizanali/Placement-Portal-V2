package com.placement.portal.security.jwt;

import com.placement.portal.domain.User;
import com.placement.portal.domain.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pure unit tests for {@link JwtTokenProvider} — no Spring context.
 */
class JwtTokenProviderTest {

    private JwtTokenProvider provider;
    private JwtProperties props;

    @BeforeEach
    void setUp() {
        props = new JwtProperties();
        // 64-byte (HS512-sized) base64 secret; safe to commit since it's only for tests
        props.setSecret("dGVzdC1zZWNyZXQta2V5LWZvci1qd3QtdW5pdC10ZXN0cy1uZWVkcy10by1iZS02NC1ieXRlcy1taW5pbXVtLWxlbmd0aC1mb3ItaHM1MTI=");
        props.setAccessTokenExpiryMs(60_000L);
        provider = new JwtTokenProvider(props);
    }

    private User testUser() {
        return User.builder()
                .id("user-1")
                .email("ada@example.com")
                .fullName("Ada")
                .role(Role.STUDENT)
                .isActive(true)
                .build();
    }

    @Test
    void generateAndValidate_roundTrip() {
        String token = provider.generateAccessToken(testUser());
        assertThat(token).isNotBlank();
        assertThat(provider.validateToken(token)).isTrue();
        assertThat(provider.getUserIdFromToken(token)).isEqualTo("user-1");
        assertThat(provider.getRoleFromToken(token)).isEqualTo("STUDENT");
    }

    @Test
    void validate_tamperedToken_returnsFalse() {
        String token = provider.generateAccessToken(testUser());
        String tampered = token.substring(0, token.length() - 5) + "XXXXX";
        assertThat(provider.validateToken(tampered)).isFalse();
    }

    @Test
    void validate_emptyOrNullToken_returnsFalse() {
        assertThat(provider.validateToken("")).isFalse();
        assertThat(provider.validateToken(null)).isFalse();
    }

    @Test
    void validate_expiredToken_returnsFalse() throws InterruptedException {
        props.setAccessTokenExpiryMs(1L);
        String token = provider.generateAccessToken(testUser());
        Thread.sleep(50);
        assertThat(provider.validateToken(token)).isFalse();
    }

    @Test
    void generateRefreshToken_returnsUuidString() {
        String token1 = provider.generateRefreshToken();
        String token2 = provider.generateRefreshToken();
        assertThat(token1).hasSize(36).contains("-");
        assertThat(token1).isNotEqualTo(token2);
    }
}
