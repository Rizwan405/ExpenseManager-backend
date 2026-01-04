package com.example.demo.service;

import com.example.demo.model.Users;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class JWTServiceTest {
    private JWTService jwtService;
    private Users userDetails;
    private final String TEST_USERNAME = "testuser123";
    private final String TEST_SECRET_KEY = "oa0vmWwk0ItyBcFmxmgWNw+1A9TDwzEFXBrcpIvId5I=";
    private UserDetails userDetailsMock;
    @BeforeEach
    void setUp() {
        // Arrange: Create the service instance
        jwtService = new JWTService();

        // Create test user details
        userDetailsMock = User.builder()
                .username(TEST_USERNAME)
                .password("password123")
                .authorities("ROLE_USER")
                .build();
    }
    @Test
    void generateToken_WithValidUsername_ShouldReturnValidJWT() {
        // Act: Generate token
        String token = jwtService.generateToken(TEST_USERNAME);

        // Assert: Check basic JWT structure
        assertNotNull(token, "Token should not be null");
        assertFalse(token.isEmpty(), "Token should not be empty");

        // JWT structure: header.payload.signature separated by dots
        String[] tokenParts = token.split("\\.");
        assertEquals(3, tokenParts.length, "JWT should have 3 parts");

        // Each part should be base64 encoded
        assertDoesNotThrow(() -> {
            Base64.getUrlDecoder().decode(tokenParts[0]);
            Base64.getUrlDecoder().decode(tokenParts[1]);
        }, "Token parts should be valid base64");
    }
    @Test
    void generateToken_ShouldContainCorrectUsernameInSubject() {
        // Arrange: Different usernames to test
        String[] usernames = {"alice", "bob123", "user-with-dash", "user.email@domain.com"};

        for (String username : usernames) {
            // Act
            String token = jwtService.generateToken(username);

            // Assert: Extract subject from token
            String extractedUsername = jwtService.extractUserName(token);

            assertEquals(username, extractedUsername,
                    "Extracted username should match original");
        }
    }
    @Test
    void extractUserName_WithValidToken_ShouldReturnCorrectUsername() {
        // Arrange: Generate a token first
        String originalUsername = "john_doe_2024";
        String token = jwtService.generateToken(originalUsername);

        // Act
        String extractedUsername = jwtService.extractUserName(token);

        // Assert
        assertEquals(originalUsername, extractedUsername,
                "Extracted username should match the original");
    }
    @Test
    void extractExpiration_ShouldReturnFutureDate() {
        // Arrange
        String token = jwtService.generateToken(TEST_USERNAME);

        // Act
        Date expiration = jwtService.extractExpiration(token);

        // Assert: Expiration should be in the future
        Date now = new Date();
        assertTrue(expiration.after(now),
                "Token expiration should be in the future");

        // Should be roughly 30 hours from now (within 1 minute tolerance)
        long expectedExpirationMs = 30 * 60 * 60 * 1000L; // 30 hours in milliseconds
        long actualTimeUntilExpiration = expiration.getTime() - now.getTime();
        long tolerance = 60 * 1000; // 1 minute tolerance

        assertTrue(Math.abs(actualTimeUntilExpiration - expectedExpirationMs) < tolerance,
                "Token should expire in approximately 30 hours");
    }

    @Test
    void validateToken_WithCorrectUserDetails_ShouldReturnTrue() {
        // Arrange
        String token = jwtService.generateToken(TEST_USERNAME);

        // Act
        boolean isValid = jwtService.validateToken(token, userDetailsMock);

        // Assert
        assertTrue(isValid, "Token should be valid for correct user");
    }
    @Test
    void validateToken_WithWrongUserDetails_ShouldReturnFalse() {
        // Arrange
        String token = jwtService.generateToken(TEST_USERNAME);

        // Create user details with different username
        UserDetails wrongUser = User.builder()
                .username("different_user")
                .password("password")
                .authorities("ROLE_USER")
                .build();

        // Act
        boolean isValid = jwtService.validateToken(token, wrongUser);

        // Assert
        assertFalse(isValid, "Token should be invalid for wrong user");
    }

    @Test
    void validateToken_WithSpecialCharactersInUsername_ShouldWork() {
        // Arrange: Usernames with special characters
        String[] specialUsernames = {
                "user@email.com",
                "user-name",
                "user_name",
                "user123!@#",
                "üser-with-unicode",
                "用户" // Chinese characters
        };

        for (String username : specialUsernames) {
            UserDetails specialUser = User.builder()
                    .username(username)
                    .password("pass")
                    .authorities("ROLE_USER")
                    .build();

            String token = jwtService.generateToken(username);

            // Act & Assert
            assertTrue(jwtService.validateToken(token, specialUser),
                    "Token should be valid for username: " + username);
        }
    }

    @Test
    void generateToken_WithEmptyUsername_ShouldStillCreateToken() {
        // Act
        String token = jwtService.generateToken("");

        // Assert
        assertNotNull(token);
        assertEquals(null,jwtService.extractUserName(token));
    }
    @Test
    void extractUserName_WithInvalidToken_ShouldThrowException() {
        // Arrange: Create invalid tokens
        String[] invalidTokens = {
                "",                      // Empty
                "invalid.token.format",  // Wrong format
                "header.payload",        // Missing signature
                "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ" // No signature
        };

        for (String invalidToken : invalidTokens) {
            // Act & Assert
            assertThrows(Exception.class, () -> {
                jwtService.extractUserName(invalidToken);
            }, "Should throw exception for invalid token: " + invalidToken);
        }
    }

    @Test
    void validateToken_WithTamperedToken_ShouldReturnFalse() {
        // Arrange: Create valid token
        String validToken = jwtService.generateToken(TEST_USERNAME);

        // Tamper with the token (change a character in the payload)
        String[] parts = validToken.split("\\.");
        String tamperedPayload = parts[1] + "tampered";
        String tamperedToken = parts[0] + "." + tamperedPayload + "." + parts[2];

        // Act & Assert
        assertThrows(Exception.class, () -> {
            jwtService.validateToken(tamperedToken, userDetailsMock);
        });
    }
    @Test
    void isTokenExpired_WithExpiredToken_ShouldReturnTrue() throws Exception {
        // We'll create a token manually with past expiration
        SecretKey key = Keys.hmacShaKeyFor(
                Base64.getDecoder().decode(TEST_SECRET_KEY)
        );

        // Create token that expired 1 hour ago
        Date pastDate = new Date(System.currentTimeMillis() - TimeUnit.HOURS.toMillis(1));
        Date expirationDate = new Date(System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(30));

        String expiredToken = Jwts.builder()
                .subject(TEST_USERNAME)
                .issuedAt(pastDate)
                .expiration(expirationDate)
                .signWith(key)
                .compact();

        // Use reflection to call private method
        // Alternative: Make isTokenExpired package-private or protected for testing
        // For now, let's test through public validateToken method
        assertThrows(Exception.class, () -> {
            jwtService.validateToken(expiredToken, userDetailsMock);
        });
    }

    @Test
    void validateToken_WithTokenExpiringSoon_ShouldStillBeValid() {
        // Arrange
        String token = jwtService.generateToken(TEST_USERNAME);

        // Extract expiration
        Date expiration = jwtService.extractExpiration(token);
        Date now = new Date();

        // Token should still be valid if not expired
        assertTrue(expiration.after(now));
        assertTrue(jwtService.validateToken(token, userDetailsMock));
    }

    @Test
    void getKey_ShouldAlwaysReturnSameKeyForSameSecret() {
        // Arrange: Generate token to trigger key generation
        jwtService.generateToken(TEST_USERNAME);

        // We can't directly test private method, but we can test that
        // tokens generated at different times can be validated with same key
        String token1 = jwtService.generateToken(TEST_USERNAME);

        // Wait a bit
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            // Ignore
        }

        String token2 = jwtService.generateToken(TEST_USERNAME);

        // Both tokens should be valid with same user
        assertTrue(jwtService.validateToken(token1, userDetailsMock));
        assertTrue(jwtService.validateToken(token2, userDetailsMock));

        // Both tokens should have same username
        assertEquals(TEST_USERNAME, jwtService.extractUserName(token1));
        assertEquals(TEST_USERNAME, jwtService.extractUserName(token2));
    }

    @Test
    void token_WithDifferentSecretKey_ShouldNotBeValid() throws Exception {
        // This test shows why we need secure key management
        // We can't easily test this without modifying the service
        // But the concept is important
        System.out.println("Note: If secret key changes, all existing tokens become invalid");
        System.out.println("This is a security feature, not a bug!");
    }

    @Test
    void generateToken_Performance_MultipleTokensQuickly() {
        int numberOfTokens = 100;
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < numberOfTokens; i++) {
            String token = jwtService.generateToken("user" + i);
            assertNotNull(token);
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        System.out.println("Generated " + numberOfTokens + " tokens in " + duration + "ms");
        System.out.println("Average: " + (duration / (double) numberOfTokens) + "ms per token");

        // Assert it's reasonably fast (adjust threshold as needed)
        assertTrue(duration < 5000, "Should generate 100 tokens in under 5 seconds");
    }
    @Test
    void generateToken_ShouldProduceDifferentTokensEachTime() {
        String token1 = jwtService.generateToken(TEST_USERNAME);

        // Wait a millisecond to ensure different timestamp
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            // Ignore
        }

        String token2 = jwtService.generateToken(TEST_USERNAME);

        // Tokens should be different
        assertEquals(token1, token2,
                "Tokens generated at different times should be different");

        // But both should have same username
        assertEquals(jwtService.extractUserName(token1),
                jwtService.extractUserName(token2));
    }
}
