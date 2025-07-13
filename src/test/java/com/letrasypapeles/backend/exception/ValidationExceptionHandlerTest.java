package com.letrasypapeles.backend.exception;

import com.letrasypapeles.backend.dto.MessageResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ValidationExceptionHandlerTest {

    @InjectMocks
    private ValidationExceptionHandler validationExceptionHandler;

    @Mock
    private MethodArgumentNotValidException methodArgumentNotValidException;

    @Mock
    private BindingResult bindingResult;

    @Mock
    private FieldError fieldError;

    @BeforeEach
    void setUp() {
        when(methodArgumentNotValidException.getBindingResult()).thenReturn(bindingResult);
    }

    @Test
    void testHandleValidationExceptions_WithFieldError() {
        // Arrange
        String customErrorMessage = "El campo nombre es obligatorio";
        when(bindingResult.getFieldError()).thenReturn(fieldError);
        when(fieldError.getDefaultMessage()).thenReturn(customErrorMessage);

        // Act
        ResponseEntity<MessageResponse> response = validationExceptionHandler
                .handleValidationExceptions(methodArgumentNotValidException);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(customErrorMessage, response.getBody().getMessage());
        
        verify(methodArgumentNotValidException, atLeastOnce()).getBindingResult();
        verify(bindingResult, atLeastOnce()).getFieldError();
        verify(fieldError, atLeastOnce()).getDefaultMessage();
    }

    @Test
    void testHandleValidationExceptions_WithFieldErrorButNullMessage() {
        // Arrange
        when(bindingResult.getFieldError()).thenReturn(fieldError);
        when(fieldError.getDefaultMessage()).thenReturn(null);

        // Act
        ResponseEntity<MessageResponse> response = validationExceptionHandler
                .handleValidationExceptions(methodArgumentNotValidException);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Datos de entrada inválidos", response.getBody().getMessage());
        
        verify(methodArgumentNotValidException, atLeastOnce()).getBindingResult();
        verify(bindingResult, atLeastOnce()).getFieldError();
        verify(fieldError, atLeastOnce()).getDefaultMessage();
    }

    @Test
    void testHandleValidationExceptions_WithNullFieldError() {
        // Arrange
        when(bindingResult.getFieldError()).thenReturn(null);

        // Act
        ResponseEntity<MessageResponse> response = validationExceptionHandler
                .handleValidationExceptions(methodArgumentNotValidException);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Datos de entrada inválidos", response.getBody().getMessage());
        
        verify(methodArgumentNotValidException).getBindingResult();
        verify(bindingResult).getFieldError();
        verify(fieldError, never()).getDefaultMessage();
    }

    @Test
    void testHandleValidationExceptions_DefaultMessage() {
        // Arrange - simulate the case where both conditions fail
        when(bindingResult.getFieldError()).thenReturn(null);

        // Act
        ResponseEntity<MessageResponse> response = validationExceptionHandler
                .handleValidationExceptions(methodArgumentNotValidException);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Datos de entrada inválidos", response.getBody().getMessage());
        
        verify(methodArgumentNotValidException).getBindingResult();
        verify(bindingResult).getFieldError();
    }
}
