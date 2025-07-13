package com.letrasypapeles.backend.controller;

import com.letrasypapeles.backend.dto.LoginRequest;
import com.letrasypapeles.backend.dto.LoginResponse;
import com.letrasypapeles.backend.dto.RegisterRequest;
import com.letrasypapeles.backend.dto.MessageResponse;
import com.letrasypapeles.backend.dto.TokenRequest;
import com.letrasypapeles.backend.entity.Cliente;
import com.letrasypapeles.backend.service.ClienteService;
import com.letrasypapeles.backend.security.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticación", description = "Endpoints para manejo de autenticación y tokens JWT")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final ClienteService clienteService;
    private final UserDetailsService userDetailsService;

    public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil,
                          ClienteService clienteService, UserDetailsService userDetailsService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.clienteService = clienteService;
        this.userDetailsService = userDetailsService;
    }

    @PostMapping("/login")
    @Operation(summary = "Inicia sesión", description = "Valida credenciales y devuelve un token JWT en caso de éxito")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Autenticación exitosa"),
        @ApiResponse(responseCode = "401", description = "Credenciales inválidas")
    })
    public ResponseEntity<?> autenticarUsuario(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtil.generateJwtToken(authentication);
            return ResponseEntity.ok(new LoginResponse(jwt));
        } catch (org.springframework.security.core.AuthenticationException e) {
            MessageResponse response = new MessageResponse();
            response.setMessage("Credenciales inválidas");
            return ResponseEntity.status(401).body(response);
        }
    }

    @PostMapping("/register")
    @Operation(summary = "Registra un usuario", description = "Crea un nuevo cliente y retorna los datos del usuario registrado")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Registro exitoso"),
        @ApiResponse(responseCode = "500", description = "Error en el registro de usuario")
    })
    public ResponseEntity<?> registrarUsuario(@Valid @RequestBody RegisterRequest registroRequest) {
        try {
            Cliente cliente = new Cliente();
            cliente.setNombre(registroRequest.getNombre());
            cliente.setApellido(registroRequest.getApellido());
            cliente.setEmail(registroRequest.getEmail());
            cliente.setContraseña(registroRequest.getPassword());
            Cliente registrado = clienteService.registrarCliente(cliente);
            return ResponseEntity.ok(registrado);
        } catch (RuntimeException e) {
            MessageResponse response = new MessageResponse();
            response.setMessage("Error al registrar usuario: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/validate")
    @Operation(summary = "Valida token", description = "Verifica la validez de un JWT y retorna el usuario asociado")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Token válido"),
        @ApiResponse(responseCode = "400", description = "Token vacío o mal formado"),
        @ApiResponse(responseCode = "401", description = "Token inválido")
    })
    public ResponseEntity<?> validarToken(@RequestBody TokenRequest tokenRequest) {
        String token = tokenRequest.getToken();
        if (token == null || token.trim().isEmpty()) {
            MessageResponse response = new MessageResponse();
            response.setMessage("Token no puede estar vacío");
            return ResponseEntity.badRequest().body(response);
        }
        try {
            if (jwtUtil.validateJwtToken(token)) {
                String username = jwtUtil.getUsernameFromToken(token);
                MessageResponse response = new MessageResponse();
                response.setMessage("Token válido para usuario: " + username);
                return ResponseEntity.ok(response);
            } else {
                MessageResponse response = new MessageResponse();
                response.setMessage("Token inválido");
                return ResponseEntity.status(401).body(response);
            }
        } catch (Exception e) {
            MessageResponse response = new MessageResponse();
            response.setMessage("Error al validar token: " + e.getMessage());
            return ResponseEntity.status(401).body(response);
        }
    }

    @PostMapping("/logout")
    @Operation(summary = "Cierra sesión", description = "Invalidación de token JWT y limpieza del contexto de seguridad")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Logout exitoso"),
        @ApiResponse(responseCode = "401", description = "Token inválido"),
        @ApiResponse(responseCode = "500", description = "Error durante el logout")
    })
    public ResponseEntity<?> logout(@RequestBody TokenRequest tokenRequest) {
        String token = tokenRequest.getToken();
        try {
            if (jwtUtil.validateJwtToken(token)) {
                SecurityContextHolder.clearContext();
                MessageResponse response = new MessageResponse();
                response.setMessage("Logout exitoso");
                return ResponseEntity.ok(response);
            } else {
                MessageResponse response = new MessageResponse();
                response.setMessage("Token inválido");
                return ResponseEntity.status(401).body(response);
            }
        } catch (Exception e) {
            MessageResponse response = new MessageResponse();
            response.setMessage("Error en logout: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresca token", description = "Genera un nuevo JWT a partir de un token válido existente")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Refresh exitoso"),
        @ApiResponse(responseCode = "401", description = "Token inválido"),
        @ApiResponse(responseCode = "500", description = "Error al refrescar token")
    })
    public ResponseEntity<?> refreshToken(@RequestBody TokenRequest tokenRequest) {
        String token = tokenRequest.getToken();
        try {
            if (jwtUtil.validateJwtToken(token)) {
                String username = jwtUtil.getUsernameFromToken(token);
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                String newToken = jwtUtil.generateJwtToken(authentication);
                return ResponseEntity.ok(new LoginResponse(newToken));
            } else {
                MessageResponse response = new MessageResponse();
                response.setMessage("Token inválido para refresh");
                return ResponseEntity.status(401).body(response);
            }
        } catch (Exception e) {
            MessageResponse response = new MessageResponse();
            response.setMessage("Error al refrescar token: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}