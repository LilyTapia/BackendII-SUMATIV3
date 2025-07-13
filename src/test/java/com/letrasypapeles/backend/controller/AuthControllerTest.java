package com.letrasypapeles.backend.controller;

import com.letrasypapeles.backend.dto.LoginRequest;
import com.letrasypapeles.backend.dto.LoginResponse;
import com.letrasypapeles.backend.dto.MessageResponse;
import com.letrasypapeles.backend.dto.RegisterRequest;
import com.letrasypapeles.backend.dto.TokenRequest;
import com.letrasypapeles.backend.entity.Cliente;
import com.letrasypapeles.backend.service.ClienteService;
import com.letrasypapeles.backend.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @InjectMocks
    private AuthController authController;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private ClienteService clienteService;

    @Mock
    private UserDetailsService userDetailsService;

    @Test
    void testAutenticarUsuario() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test@test.com");
        loginRequest.setPassword("password");

        Authentication mockAuth = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mockAuth);
        when(jwtUtil.generateJwtToken(mockAuth)).thenReturn("jwt-token");

        ResponseEntity<?> response = authController.autenticarUsuario(loginRequest);

        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody() instanceof LoginResponse);
        LoginResponse loginResponse = (LoginResponse) response.getBody();
        assertNotNull(loginResponse);
        assertEquals("jwt-token", loginResponse.getToken());
        assertEquals("Bearer", loginResponse.getType());
    }

    @Test
    void testRegistrarUsuario() {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setNombre("Juan");
        registerRequest.setApellido("Pérez");
        registerRequest.setEmail("juan@test.com");
        registerRequest.setPassword("password");

        Cliente cliente = new Cliente();
        cliente.setId(1L);
        cliente.setNombre("Juan");
        cliente.setApellido("Pérez");
        cliente.setEmail("juan@test.com");

        when(clienteService.registrarCliente(any(Cliente.class))).thenReturn(cliente);

        ResponseEntity<?> response = authController.registrarUsuario(registerRequest);

        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody() instanceof Cliente);
        Cliente responseCliente = (Cliente) response.getBody();
        assertNotNull(responseCliente);
        assertEquals(1L, responseCliente.getId());
        assertEquals("Juan", responseCliente.getNombre());
        assertEquals("juan@test.com", responseCliente.getEmail());
    }

    @Test
    void testRegistrarUsuarioError() {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setNombre("Juan");
        registerRequest.setApellido("Pérez");
        registerRequest.setEmail("juan@test.com");
        registerRequest.setPassword("password");

        when(clienteService.registrarCliente(any(Cliente.class)))
                .thenThrow(new RuntimeException("Error de prueba"));

        ResponseEntity<?> response = authController.registrarUsuario(registerRequest);

        assertEquals(500, response.getStatusCode().value());
        assertTrue(response.getBody() instanceof MessageResponse);
        MessageResponse messageResponse = (MessageResponse) response.getBody();
        assertNotNull(messageResponse);
        assertEquals("Error al registrar usuario: Error de prueba", messageResponse.getMessage());
    }

    @Test
    void testAutenticarUsuario_CredencialesInvalidas() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test@test.com");
        loginRequest.setPassword("passwordIncorrecto");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new org.springframework.security.core.AuthenticationException("Credenciales inválidas") {});

        ResponseEntity<?> response = authController.autenticarUsuario(loginRequest);

        assertEquals(401, response.getStatusCode().value());
        assertTrue(response.getBody() instanceof MessageResponse);
        MessageResponse messageResponse = (MessageResponse) response.getBody();
        assertNotNull(messageResponse);
        assertEquals("Credenciales inválidas", messageResponse.getMessage());
    }

    @Test
    void testAutenticarUsuario_EmailVacio() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("");
        loginRequest.setPassword("password");

        // In pure unit testing, validation is not automatically triggered
        // This test focuses on the authentication logic with empty email
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new org.springframework.security.core.AuthenticationException("Bad credentials") {});

        ResponseEntity<?> response = authController.autenticarUsuario(loginRequest);

        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void testAutenticarUsuario_PasswordVacio() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test@test.com");
        loginRequest.setPassword("");

        // In pure unit testing, validation is not automatically triggered
        // This test focuses on the authentication logic with empty password
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new org.springframework.security.core.AuthenticationException("Bad credentials") {});

        ResponseEntity<?> response = authController.autenticarUsuario(loginRequest);

        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void testRegistrarUsuario_CamposObligatoriosVacios() {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setNombre("");
        registerRequest.setApellido("");
        registerRequest.setEmail("");
        registerRequest.setPassword("");

        // In pure unit testing, validation is not automatically triggered
        // This test focuses on the registration logic with empty fields
        when(clienteService.registrarCliente(any(Cliente.class)))
                .thenThrow(new RuntimeException("Invalid data"));

        ResponseEntity<?> response = authController.registrarUsuario(registerRequest);

        assertEquals(500, response.getStatusCode().value());
    }

    @Test
    void testRegistrarUsuario_EmailInvalido() {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setNombre("Juan");
        registerRequest.setApellido("Pérez");
        registerRequest.setEmail("email-invalido");
        registerRequest.setPassword("password");

        // In pure unit testing, validation is not automatically triggered
        // This test focuses on the registration logic with invalid email
        when(clienteService.registrarCliente(any(Cliente.class)))
                .thenThrow(new RuntimeException("Invalid email format"));

        ResponseEntity<?> response = authController.registrarUsuario(registerRequest);

        assertEquals(500, response.getStatusCode().value());
    }

    @Test
    void testRegistrarUsuario_PasswordMuyCorto() {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setNombre("Juan");
        registerRequest.setApellido("Pérez");
        registerRequest.setEmail("juan@test.com");
        registerRequest.setPassword("123");

        // In pure unit testing, validation is not automatically triggered
        // This test focuses on the registration logic with short password
        when(clienteService.registrarCliente(any(Cliente.class)))
                .thenThrow(new RuntimeException("Password too short"));

        ResponseEntity<?> response = authController.registrarUsuario(registerRequest);

        assertEquals(500, response.getStatusCode().value());
    }

    @Test
    void testValidacionToken_TokenValido() {
        String validToken = "valid.jwt.token";
        TokenRequest tokenRequest = new TokenRequest();
        tokenRequest.setToken(validToken);

        when(jwtUtil.validateJwtToken(validToken)).thenReturn(true);
        when(jwtUtil.getUsernameFromToken(validToken)).thenReturn("test@test.com");

        ResponseEntity<?> response = authController.validarToken(tokenRequest);

        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody() instanceof MessageResponse);
        MessageResponse messageResponse = (MessageResponse) response.getBody();
        assertNotNull(messageResponse);
        assertEquals("Token válido para usuario: test@test.com", messageResponse.getMessage());

        verify(jwtUtil, times(1)).validateJwtToken(validToken);
        verify(jwtUtil, times(1)).getUsernameFromToken(validToken);
    }

    @Test
    void testValidacionToken_TokenInvalido() {
        String invalidToken = "invalid.jwt.token";
        TokenRequest tokenRequest = new TokenRequest();
        tokenRequest.setToken(invalidToken);

        when(jwtUtil.validateJwtToken(invalidToken)).thenReturn(false);

        ResponseEntity<?> response = authController.validarToken(tokenRequest);

        assertEquals(401, response.getStatusCode().value());

        verify(jwtUtil, times(1)).validateJwtToken(invalidToken);
        verify(jwtUtil, never()).getUsernameFromToken(invalidToken);
    }

    @Test
    void testValidacionToken_TokenNulo() {
        TokenRequest tokenRequest = new TokenRequest();
        tokenRequest.setToken(null);

        ResponseEntity<?> response = authController.validarToken(tokenRequest);

        assertEquals(400, response.getStatusCode().value());

        verify(jwtUtil, never()).validateJwtToken(any());
    }

    @Test
    void testValidacionToken_TokenVacio() {
        TokenRequest tokenRequest = new TokenRequest();
        tokenRequest.setToken("");

        ResponseEntity<?> response = authController.validarToken(tokenRequest);

        assertEquals(400, response.getStatusCode().value());

        verify(jwtUtil, never()).validateJwtToken(any());
    }

    @Test
    void testLogout_Exitoso() {
        String token = "valid.jwt.token";
        TokenRequest tokenRequest = new TokenRequest();
        tokenRequest.setToken(token);

        when(jwtUtil.validateJwtToken(token)).thenReturn(true);

        ResponseEntity<?> response = authController.logout(tokenRequest);

        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody() instanceof MessageResponse);
        MessageResponse messageResponse = (MessageResponse) response.getBody();
        assertNotNull(messageResponse);
        assertEquals("Logout exitoso", messageResponse.getMessage());

        verify(jwtUtil, times(1)).validateJwtToken(token);
    }

    @Test
    void testRefreshToken_TokenValido() {
        String oldToken = "old.jwt.token";
        String newToken = "new.jwt.token";
        String username = "test@test.com";

        // Mock UserDetails
        UserDetails mockUserDetails = mock(UserDetails.class);
        when(mockUserDetails.getAuthorities()).thenReturn(java.util.Collections.emptyList());

        when(jwtUtil.validateJwtToken(oldToken)).thenReturn(true);
        when(jwtUtil.getUsernameFromToken(oldToken)).thenReturn(username);
        when(userDetailsService.loadUserByUsername(username)).thenReturn(mockUserDetails);
        when(jwtUtil.generateJwtToken(any(Authentication.class))).thenReturn(newToken);

        // Create request
        TokenRequest tokenRequest = new TokenRequest();
        tokenRequest.setToken(oldToken);

        // Call the method directly
        ResponseEntity<?> response = authController.refreshToken(tokenRequest);

        // Verify response
        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody() instanceof LoginResponse);
        LoginResponse loginResponse = (LoginResponse) response.getBody();
        assertNotNull(loginResponse);
        assertEquals(newToken, loginResponse.getToken());
        assertEquals("Bearer", loginResponse.getType());

        verify(jwtUtil, times(1)).validateJwtToken(oldToken);
        verify(jwtUtil, times(1)).getUsernameFromToken(oldToken);
        verify(userDetailsService, times(1)).loadUserByUsername(username);
        verify(jwtUtil, times(1)).generateJwtToken(any(Authentication.class));
    }

    @Test
    void testValidarToken_Exception() {
        // Test exception handling in validarToken (lines 93-96)
        TokenRequest tokenRequest = new TokenRequest();
        tokenRequest.setToken("valid-token");

        when(jwtUtil.validateJwtToken("valid-token")).thenThrow(new RuntimeException("JWT processing error"));

        ResponseEntity<?> response = authController.validarToken(tokenRequest);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(jwtUtil).validateJwtToken("valid-token");
    }

    @Test
    void testLogout_TokenInvalido() {
        // Test invalid token in logout (lines 110-112)
        TokenRequest tokenRequest = new TokenRequest();
        tokenRequest.setToken("invalid-token");

        when(jwtUtil.validateJwtToken("invalid-token")).thenReturn(false);

        ResponseEntity<?> response = authController.logout(tokenRequest);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(jwtUtil).validateJwtToken("invalid-token");
    }

    @Test
    void testLogout_Exception() {
        // Test exception handling in logout (lines 114-117)
        TokenRequest tokenRequest = new TokenRequest();
        tokenRequest.setToken("valid-token");

        when(jwtUtil.validateJwtToken("valid-token")).thenThrow(new RuntimeException("JWT processing error"));

        ResponseEntity<?> response = authController.logout(tokenRequest);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(jwtUtil).validateJwtToken("valid-token");
    }

    @Test
    void testRefreshToken_TokenInvalido() {
        // Test invalid token in refreshToken (lines 135-137)
        TokenRequest tokenRequest = new TokenRequest();
        tokenRequest.setToken("invalid-token");

        when(jwtUtil.validateJwtToken("invalid-token")).thenReturn(false);

        ResponseEntity<?> response = authController.refreshToken(tokenRequest);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(jwtUtil).validateJwtToken("invalid-token");
    }

    @Test
    void testRefreshToken_Exception() {
        // Test exception handling in refreshToken (lines 139-142)
        TokenRequest tokenRequest = new TokenRequest();
        tokenRequest.setToken("valid-token");

        when(jwtUtil.validateJwtToken("valid-token")).thenThrow(new RuntimeException("JWT processing error"));

        ResponseEntity<?> response = authController.refreshToken(tokenRequest);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(jwtUtil).validateJwtToken("valid-token");
    }
}