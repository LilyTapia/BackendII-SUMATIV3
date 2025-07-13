package com.letrasypapeles.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest {
    @NotBlank(message = "Email no puede estar vacío")
    @Email(message = "Email debe tener un formato válido")
    private String email;

    @NotBlank(message = "Password no puede estar vacío")
    @Size(min = 6, message = "Password debe tener al menos 6 caracteres")
    private String password;
}
