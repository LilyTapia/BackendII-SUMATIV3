package com.letrasypapeles.backend.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LoginResponseTest {

    private LoginResponse loginResponse;
    private String token;

    @BeforeEach
    void setUp() {
        token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test.token";
        loginResponse = new LoginResponse();
    }

    @Test
    void constructor_NoArgs_CreatesEmptyLoginResponse() {
        // When
        LoginResponse response = new LoginResponse();

        // Then
        assertNotNull(response);
        assertNull(response.getToken());
    }

    @Test
    void constructor_WithToken_CreatesLoginResponseWithToken() {
        // When
        LoginResponse response = new LoginResponse(token);

        // Then
        assertNotNull(response);
        assertEquals(token, response.getToken());
    }

    @Test
    void constructor_WithNullToken_CreatesLoginResponseWithNullToken() {
        // When
        LoginResponse response = new LoginResponse(null);

        // Then
        assertNotNull(response);
        assertNull(response.getToken());
    }

    @Test
    void constructor_WithEmptyToken_CreatesLoginResponseWithEmptyToken() {
        // When
        LoginResponse response = new LoginResponse("");

        // Then
        assertNotNull(response);
        assertEquals("", response.getToken());
    }

    @Test
    void getToken_ReturnsCorrectToken() {
        // Given
        loginResponse.setToken(token);

        // When
        String retrievedToken = loginResponse.getToken();

        // Then
        assertEquals(token, retrievedToken);
    }

    @Test
    void getToken_WithNullToken_ReturnsNull() {
        // Given
        loginResponse.setToken(null);

        // When
        String retrievedToken = loginResponse.getToken();

        // Then
        assertNull(retrievedToken);
    }

    @Test
    void setToken_WithValidToken_SetsToken() {
        // When
        loginResponse.setToken(token);

        // Then
        assertEquals(token, loginResponse.getToken());
    }

    @Test
    void setToken_WithNull_SetsNull() {
        // When
        loginResponse.setToken(null);

        // Then
        assertNull(loginResponse.getToken());
    }

    @Test
    void setToken_WithEmptyString_SetsEmptyString() {
        // When
        loginResponse.setToken("");

        // Then
        assertEquals("", loginResponse.getToken());
    }

    @Test
    void setToken_WithWhitespace_SetsWhitespace() {
        // Given
        String whitespaceToken = "   ";

        // When
        loginResponse.setToken(whitespaceToken);

        // Then
        assertEquals(whitespaceToken, loginResponse.getToken());
    }

    @Test
    void setToken_WithLongToken_SetsLongToken() {
        // Given
        String longToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";

        // When
        loginResponse.setToken(longToken);

        // Then
        assertEquals(longToken, loginResponse.getToken());
    }

    @Test
    void setToken_MultipleAssignments_UpdatesToken() {
        // Given
        String firstToken = "first.token";
        String secondToken = "second.token";

        // When
        loginResponse.setToken(firstToken);
        assertEquals(firstToken, loginResponse.getToken());

        loginResponse.setToken(secondToken);

        // Then
        assertEquals(secondToken, loginResponse.getToken());
        assertNotEquals(firstToken, loginResponse.getToken());
    }

    @Test
    void setToken_FromNullToValue_UpdatesToken() {
        // Given
        loginResponse.setToken(null);
        assertNull(loginResponse.getToken());

        // When
        loginResponse.setToken(token);

        // Then
        assertEquals(token, loginResponse.getToken());
    }

    @Test
    void setToken_FromValueToNull_UpdatesToken() {
        // Given
        loginResponse.setToken(token);
        assertEquals(token, loginResponse.getToken());

        // When
        loginResponse.setToken(null);

        // Then
        assertNull(loginResponse.getToken());
    }

    @Test
    void setToken_WithSpecialCharacters_SetsSpecialCharacters() {
        // Given
        String specialToken = "token.with-special_characters@123!#$%";

        // When
        loginResponse.setToken(specialToken);

        // Then
        assertEquals(specialToken, loginResponse.getToken());
    }

    @Test
    void setToken_WithUnicodeCharacters_SetsUnicodeCharacters() {
        // Given
        String unicodeToken = "token.with.unicode.ñáéíóú.characters";

        // When
        loginResponse.setToken(unicodeToken);

        // Then
        assertEquals(unicodeToken, loginResponse.getToken());
    }

    @Test
    void setToken_WithNumbers_SetsNumbers() {
        // Given
        String numericToken = "123456789";

        // When
        loginResponse.setToken(numericToken);

        // Then
        assertEquals(numericToken, loginResponse.getToken());
    }

    @Test
    void constructor_AndGetter_Consistency() {
        // Given
        String testToken = "consistency.test.token";

        // When
        LoginResponse response = new LoginResponse(testToken);

        // Then
        assertEquals(testToken, response.getToken());
    }

    @Test
    void constructor_AndSetter_Consistency() {
        // Given
        String initialToken = "initial.token";
        String updatedToken = "updated.token";

        // When
        LoginResponse response = new LoginResponse(initialToken);
        response.setToken(updatedToken);

        // Then
        assertEquals(updatedToken, response.getToken());
        assertNotEquals(initialToken, response.getToken());
    }

    @Test
    void multipleInstances_IndependentTokens() {
        // Given
        String token1 = "token.one";
        String token2 = "token.two";

        // When
        LoginResponse response1 = new LoginResponse(token1);
        LoginResponse response2 = new LoginResponse(token2);

        // Then
        assertEquals(token1, response1.getToken());
        assertEquals(token2, response2.getToken());
        assertNotEquals(response1.getToken(), response2.getToken());
    }

    @Test
    void tokenField_IsPrivate_AccessibleThroughMethods() {
        // Given
        LoginResponse response = new LoginResponse();

        // When & Then - Should only be accessible through getter/setter
        assertDoesNotThrow(() -> response.setToken(token));
        assertDoesNotThrow(() -> response.getToken());
        assertEquals(token, response.getToken());
    }

    @Test
    void setType_SetsTypeCorrectly() {
        // Given
        LoginResponse response = new LoginResponse();
        String customType = "CustomBearer";

        // When
        response.setType(customType);

        // Then
        assertEquals(customType, response.getType());
    }

    @Test
    void setType_WithNull_SetsTypeToNull() {
        // Given
        LoginResponse response = new LoginResponse();

        // When
        response.setType(null);

        // Then
        assertNull(response.getType());
    }
}
