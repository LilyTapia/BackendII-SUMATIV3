package com.letrasypapeles.backend.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TokenRequestTest {

    @Test
    void testDefaultConstructor() {
        // When
        TokenRequest tokenRequest = new TokenRequest();

        // Then
        assertNotNull(tokenRequest);
        assertNull(tokenRequest.getToken());
    }

    @Test
    void testParameterizedConstructor() {
        // Given
        String token = "test-token-123";

        // When
        TokenRequest tokenRequest = new TokenRequest(token);

        // Then
        assertNotNull(tokenRequest);
        assertEquals(token, tokenRequest.getToken());
    }

    @Test
    void testGetToken() {
        // Given
        String token = "jwt-token-example";
        TokenRequest tokenRequest = new TokenRequest(token);

        // When
        String result = tokenRequest.getToken();

        // Then
        assertEquals(token, result);
    }

    @Test
    void testSetToken() {
        // Given
        TokenRequest tokenRequest = new TokenRequest();
        String token = "new-token-value";

        // When
        tokenRequest.setToken(token);

        // Then
        assertEquals(token, tokenRequest.getToken());
    }

    @Test
    void testSetTokenNull() {
        // Given
        TokenRequest tokenRequest = new TokenRequest("initial-token");

        // When
        tokenRequest.setToken(null);

        // Then
        assertNull(tokenRequest.getToken());
    }

    @Test
    void testSetTokenEmpty() {
        // Given
        TokenRequest tokenRequest = new TokenRequest();
        String emptyToken = "";

        // When
        tokenRequest.setToken(emptyToken);

        // Then
        assertEquals(emptyToken, tokenRequest.getToken());
    }

    @Test
    void testTokenRequestWithLongToken() {
        // Given
        String longToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";

        // When
        TokenRequest tokenRequest = new TokenRequest(longToken);

        // Then
        assertEquals(longToken, tokenRequest.getToken());
    }

    @Test
    void testTokenRequestWithSpecialCharacters() {
        // Given
        String specialToken = "token-with-special-chars!@#$%^&*()";

        // When
        TokenRequest tokenRequest = new TokenRequest();
        tokenRequest.setToken(specialToken);

        // Then
        assertEquals(specialToken, tokenRequest.getToken());
    }

    @Test
    void testTokenRequestOverwrite() {
        // Given
        String initialToken = "initial-token";
        String newToken = "new-token";
        TokenRequest tokenRequest = new TokenRequest(initialToken);

        // When
        tokenRequest.setToken(newToken);

        // Then
        assertEquals(newToken, tokenRequest.getToken());
        assertNotEquals(initialToken, tokenRequest.getToken());
    }

    @Test
    void testTokenRequestEquality() {
        // Given
        String token = "same-token";
        TokenRequest tokenRequest1 = new TokenRequest(token);
        TokenRequest tokenRequest2 = new TokenRequest();
        tokenRequest2.setToken(token);

        // Then
        assertEquals(tokenRequest1.getToken(), tokenRequest2.getToken());
    }
}
