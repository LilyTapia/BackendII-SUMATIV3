package com.letrasypapeles.backend.exception;

import com.letrasypapeles.backend.dto.MessageResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ValidationExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<MessageResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        MessageResponse response = new MessageResponse();
        
        String errorMessage = "Datos de entrada inválidos";
        if (ex.getBindingResult().getFieldError() != null && 
            ex.getBindingResult().getFieldError().getDefaultMessage() != null) {
            errorMessage = ex.getBindingResult().getFieldError().getDefaultMessage();
        }
        
        response.setMessage(errorMessage);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}
