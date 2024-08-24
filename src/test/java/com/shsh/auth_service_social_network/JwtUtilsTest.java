package com.shsh.auth_service_social_network;

import com.shsh.auth_service_social_network.security.JwtUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtUtilsTest {

    @InjectMocks
    private JwtUtils jwtUtils;

    private String jwtSecret = "testSecretKey";
    private int jwtExpirationMinutes = 60;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtUtils, "jwtSecret", jwtSecret);
        ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMinutes", jwtExpirationMinutes);
    }

    @Test
    void testGenerateJwtToken() {
        String username = "testUser";
        String email = "test@example.com";
        String id = "12345";

        String token = jwtUtils.generateJwtToken(username, email, id);

        assertNotNull(token, "Token should not be null");

        Claims claims = Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).getBody();

        assertEquals(username, claims.getSubject(), "Username should match");
        assertEquals(email, claims.get("email"), "Email should match");
        assertEquals(id, claims.getId(), "ID should match");

        Date now = new Date();
        assertTrue(claims.getExpiration().after(now), "Expiration date should be in the future");
    }

    @Test
    void testGetUsernameFromJwtToken() {
        String username = "testUser";
        String token = Jwts.builder()
                .setSubject(username)
                .signWith(SignatureAlgorithm.HS256, jwtSecret)
                .compact();

        String extractedUsername = jwtUtils.getUsernameFromJwtToken(token);

        assertEquals(username, extractedUsername, "Extracted username should match");
    }

    @Test
    void testValidateJwtToken_ValidToken() {
        String token = Jwts.builder()
                .setSubject("testUser")
                .signWith(SignatureAlgorithm.HS256, jwtSecret)
                .compact();

        boolean isValid = jwtUtils.validateJwtToken(token);

        assertTrue(isValid, "Token should be valid");
    }

    @Test
    void testValidateJwtToken_InvalidToken() {
        String invalidToken = "invalid.token.here";

        boolean isValid = jwtUtils.validateJwtToken(invalidToken);

        assertFalse(isValid, "Token should be invalid");
    }

    @Test
    void testValidateJwtToken_ExpiredToken() {
        Date now = new Date();
        Date expiredDate = new Date(now.getTime() - 1000); // 1 second in the past

        String expiredToken = Jwts.builder()
                .setSubject("testUser")
                .setExpiration(expiredDate)
                .signWith(SignatureAlgorithm.HS256, jwtSecret)
                .compact();

        boolean isValid = jwtUtils.validateJwtToken(expiredToken);

        assertFalse(isValid, "Token should be expired");
    }
}
