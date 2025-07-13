package com.letrasypapeles.backend.exception;

import com.letrasypapeles.backend.dto.MessageResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.context.request.WebRequest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    @Mock
    private WebRequest webRequest;

    @BeforeEach
    void setUp() {
        when(webRequest.getDescription(false)).thenReturn("uri=/api/test");
    }

    @Test
    void handleJwtAuthenticationException_ReturnsUnauthorized() {
        // Given
        String errorMessage = "Token inválido";
        JwtAuthenticationException exception = new JwtAuthenticationException(errorMessage);

        // When
        ResponseEntity<?> response = globalExceptionHandler.handleJwtAuthenticationException(exception, webRequest);

        // Then
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof MessageResponse);
        MessageResponse messageResponse = (MessageResponse) response.getBody();
        assertEquals("Error de autenticación: " + errorMessage, messageResponse.getMessage());
    }

    @Test
    void handleBadCredentialsException_ReturnsUnauthorized() {
        // Given
        BadCredentialsException exception = new BadCredentialsException("Credenciales incorrectas");

        // When
        ResponseEntity<?> response = globalExceptionHandler.handleBadCredentialsException(exception, webRequest);

        // Then
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof MessageResponse);
        MessageResponse messageResponse = (MessageResponse) response.getBody();
        assertEquals("Credenciales inválidas", messageResponse.getMessage());
    }

    @Test
    void handleAuthenticationException_ReturnsUnauthorized() {
        // Given
        String errorMessage = "Error de autenticación general";
        AuthenticationException exception = new AuthenticationException(errorMessage) {};

        // When
        ResponseEntity<?> response = globalExceptionHandler.handleAuthenticationException(exception, webRequest);

        // Then
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof MessageResponse);
        MessageResponse messageResponse = (MessageResponse) response.getBody();
        assertEquals("Error de autenticación: " + errorMessage, messageResponse.getMessage());
    }

    @Test
    void handleAccessDeniedException_ReturnsForbidden() {
        // Given
        String errorMessage = "Acceso denegado";
        AccessDeniedException exception = new AccessDeniedException(errorMessage);

        // When
        ResponseEntity<?> response = globalExceptionHandler.handleAccessDeniedException(exception, webRequest);

        // Then
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof MessageResponse);
        MessageResponse messageResponse = (MessageResponse) response.getBody();
        assertEquals("No tiene permisos para acceder a este recurso", messageResponse.getMessage());
    }

    @Test
    void handleGlobalException_ReturnsInternalServerError() {
        // Given
        String errorMessage = "Error interno del servidor";
        Exception exception = new Exception(errorMessage);

        // When
        ResponseEntity<?> response = globalExceptionHandler.handleGlobalException(exception, webRequest);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof MessageResponse);
        MessageResponse messageResponse = (MessageResponse) response.getBody();
        assertEquals("Error del servidor: " + errorMessage, messageResponse.getMessage());
    }

    @Test
    void handleGlobalException_WithNullMessage_ReturnsInternalServerError() {
        // Given
        Exception exception = new Exception((String) null);

        // When
        ResponseEntity<?> response = globalExceptionHandler.handleGlobalException(exception, webRequest);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof MessageResponse);
        MessageResponse messageResponse = (MessageResponse) response.getBody();
        assertEquals("Error del servidor: null", messageResponse.getMessage());
    }

    @Test
    void handleRuntimeException_ReturnsInternalServerError() {
        // Given
        String errorMessage = "Error de tiempo de ejecución";
        RuntimeException exception = new RuntimeException(errorMessage);

        // When
        ResponseEntity<?> response = globalExceptionHandler.handleGlobalException(exception, webRequest);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof MessageResponse);
        MessageResponse messageResponse = (MessageResponse) response.getBody();
        assertEquals("Error del servidor: " + errorMessage, messageResponse.getMessage());
    }

    @Test
    void handleIllegalArgumentException_ReturnsInternalServerError() {
        // Given
        String errorMessage = "Argumento ilegal";
        IllegalArgumentException exception = new IllegalArgumentException(errorMessage);

        // When
        ResponseEntity<?> response = globalExceptionHandler.handleGlobalException(exception, webRequest);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof MessageResponse);
        MessageResponse messageResponse = (MessageResponse) response.getBody();
        assertEquals("Error del servidor: " + errorMessage, messageResponse.getMessage());
    }

    @Test
    void handleNullPointerException_ReturnsInternalServerError() {
        // Given
        String errorMessage = "Referencia nula";
        NullPointerException exception = new NullPointerException(errorMessage);

        // When
        ResponseEntity<?> response = globalExceptionHandler.handleGlobalException(exception, webRequest);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof MessageResponse);
        MessageResponse messageResponse = (MessageResponse) response.getBody();
        assertEquals("Error del servidor: " + errorMessage, messageResponse.getMessage());
    }

    @Test
    void handleJwtAuthenticationException_WithCause_ReturnsUnauthorized() {
        // Given
        String errorMessage = "Token expirado";
        Throwable cause = new RuntimeException("Causa del error");
        JwtAuthenticationException exception = new JwtAuthenticationException(errorMessage, cause);

        // When
        ResponseEntity<?> response = globalExceptionHandler.handleJwtAuthenticationException(exception, webRequest);

        // Then
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof MessageResponse);
        MessageResponse messageResponse = (MessageResponse) response.getBody();
        assertEquals("Error de autenticación: " + errorMessage, messageResponse.getMessage());
    }

    @Test
    void handleJwtAuthenticationException_WithNullMessage_ReturnsUnauthorized() {
        // Given
        JwtAuthenticationException exception = new JwtAuthenticationException(null);

        // When
        ResponseEntity<?> response = globalExceptionHandler.handleJwtAuthenticationException(exception, webRequest);

        // Then
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof MessageResponse);
        MessageResponse messageResponse = (MessageResponse) response.getBody();
        assertEquals("Error de autenticación: null", messageResponse.getMessage());
    }

    @Test
    void handleAuthenticationException_WithNullMessage_ReturnsUnauthorized() {
        // Given
        AuthenticationException exception = new AuthenticationException(null) {};

        // When
        ResponseEntity<?> response = globalExceptionHandler.handleAuthenticationException(exception, webRequest);

        // Then
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof MessageResponse);
        MessageResponse messageResponse = (MessageResponse) response.getBody();
        assertEquals("Error de autenticación: null", messageResponse.getMessage());
    }

    @Test
    void handleAccessDeniedException_WithNullMessage_ReturnsForbidden() {
        // Given
        AccessDeniedException exception = new AccessDeniedException(null);

        // When
        ResponseEntity<?> response = globalExceptionHandler.handleAccessDeniedException(exception, webRequest);

        // Then
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof MessageResponse);
        MessageResponse messageResponse = (MessageResponse) response.getBody();
        assertEquals("No tiene permisos para acceder a este recurso", messageResponse.getMessage());
    }
}
