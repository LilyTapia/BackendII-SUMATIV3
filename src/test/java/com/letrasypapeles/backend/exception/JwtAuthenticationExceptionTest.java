package com.letrasypapeles.backend.exception;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.AuthenticationException;

import static org.junit.jupiter.api.Assertions.*;

class JwtAuthenticationExceptionTest {

    @Test
    void constructor_WithMessage_CreatesException() {
        // Given
        String message = "Token JWT inválido";

        // When
        JwtAuthenticationException exception = new JwtAuthenticationException(message);

        // Then
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
        assertTrue(exception instanceof AuthenticationException);
    }

    @Test
    void constructor_WithMessageAndCause_CreatesException() {
        // Given
        String message = "Token JWT expirado";
        Throwable cause = new RuntimeException("Causa del error");

        // When
        JwtAuthenticationException exception = new JwtAuthenticationException(message, cause);

        // Then
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
        assertTrue(exception instanceof AuthenticationException);
    }

    @Test
    void constructor_WithNullMessage_CreatesException() {
        // Given
        String message = null;

        // When
        JwtAuthenticationException exception = new JwtAuthenticationException(message);

        // Then
        assertNotNull(exception);
        assertNull(exception.getMessage());
        assertNull(exception.getCause());
        assertTrue(exception instanceof AuthenticationException);
    }

    @Test
    void constructor_WithNullMessageAndCause_CreatesException() {
        // Given
        String message = null;
        Throwable cause = new IllegalArgumentException("Argumento inválido");

        // When
        JwtAuthenticationException exception = new JwtAuthenticationException(message, cause);

        // Then
        assertNotNull(exception);
        assertNull(exception.getMessage());
        assertEquals(cause, exception.getCause());
        assertTrue(exception instanceof AuthenticationException);
    }

    @Test
    void constructor_WithEmptyMessage_CreatesException() {
        // Given
        String message = "";

        // When
        JwtAuthenticationException exception = new JwtAuthenticationException(message);

        // Then
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
        assertTrue(exception instanceof AuthenticationException);
    }

    @Test
    void constructor_WithEmptyMessageAndCause_CreatesException() {
        // Given
        String message = "";
        Throwable cause = new NullPointerException("Referencia nula");

        // When
        JwtAuthenticationException exception = new JwtAuthenticationException(message, cause);

        // Then
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
        assertTrue(exception instanceof AuthenticationException);
    }

    @Test
    void constructor_WithNullCause_CreatesException() {
        // Given
        String message = "Error de autenticación JWT";
        Throwable cause = null;

        // When
        JwtAuthenticationException exception = new JwtAuthenticationException(message, cause);

        // Then
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
        assertTrue(exception instanceof AuthenticationException);
    }

    @Test
    void exception_IsInstanceOfAuthenticationException() {
        // Given
        JwtAuthenticationException exception = new JwtAuthenticationException("Test message");

        // When & Then
        assertTrue(exception instanceof AuthenticationException);
        assertTrue(exception instanceof RuntimeException);
        assertTrue(exception instanceof Exception);
        assertTrue(exception instanceof Throwable);
    }

    @Test
    void exception_CanBeThrown() {
        // Given
        String message = "Token JWT malformado";

        // When & Then
        assertThrows(JwtAuthenticationException.class, () -> {
            throw new JwtAuthenticationException(message);
        });
    }

    @Test
    void exception_CanBeCaught() {
        // Given
        String message = "Token JWT no válido";
        JwtAuthenticationException thrownException = null;

        // When
        try {
            throw new JwtAuthenticationException(message);
        } catch (JwtAuthenticationException e) {
            thrownException = e;
        }

        // Then
        assertNotNull(thrownException);
        assertEquals(message, thrownException.getMessage());
    }

    @Test
    void exception_WithCause_CanBeThrown() {
        // Given
        String message = "Error de validación JWT";
        Throwable cause = new IllegalStateException("Estado inválido");

        // When & Then
        JwtAuthenticationException exception = assertThrows(JwtAuthenticationException.class, () -> {
            throw new JwtAuthenticationException(message, cause);
        });

        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void exception_WithCause_CanBeCaught() {
        // Given
        String message = "Error de procesamiento JWT";
        Throwable cause = new SecurityException("Error de seguridad");
        JwtAuthenticationException thrownException = null;

        // When
        try {
            throw new JwtAuthenticationException(message, cause);
        } catch (JwtAuthenticationException e) {
            thrownException = e;
        }

        // Then
        assertNotNull(thrownException);
        assertEquals(message, thrownException.getMessage());
        assertEquals(cause, thrownException.getCause());
    }
}
