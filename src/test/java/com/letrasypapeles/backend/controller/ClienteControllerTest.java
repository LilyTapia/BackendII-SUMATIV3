package com.letrasypapeles.backend.controller;

import com.letrasypapeles.backend.entity.Cliente;
import com.letrasypapeles.backend.service.ClienteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ClienteControllerTest {

    @Mock
    private ClienteService clienteService;

    @InjectMocks
    private ClienteController clienteController;

    private Cliente cliente;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        cliente = new Cliente();
        cliente.setId(1L);
        cliente.setNombre("Juan");
        cliente.setEmail("juan@test.com");
        cliente.setContraseña("password");
    }

    @Test
    void testObtenerTodos() {
        List<Cliente> clientes = Arrays.asList(cliente);
        when(clienteService.obtenerTodos()).thenReturn(clientes);

        ResponseEntity<List<Cliente>> response = clienteController.obtenerTodos();

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testObtenerPorId() {
        when(clienteService.obtenerPorId(1L)).thenReturn(Optional.of(cliente));

        ResponseEntity<Cliente> response = clienteController.obtenerPorId(1L);

        assertEquals(200, response.getStatusCodeValue());
        assertNull(response.getBody().getContraseña());
    }

    @Test
    void testObtenerPorIdNoEncontrado() {
        when(clienteService.obtenerPorId(1L)).thenReturn(Optional.empty());

        ResponseEntity<Cliente> response = clienteController.obtenerPorId(1L);

        assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    void testRegistrarCliente() {
        when(clienteService.registrarCliente(any(Cliente.class))).thenReturn(cliente);

        ResponseEntity<Cliente> response = clienteController.registrarCliente(cliente);

        assertEquals(200, response.getStatusCodeValue());
        assertNull(response.getBody().getContraseña());
    }

    @Test
    void testActualizarCliente() {
        when(clienteService.obtenerPorId(1L)).thenReturn(Optional.of(cliente));
        when(clienteService.actualizarCliente(any(Cliente.class))).thenReturn(cliente);

        ResponseEntity<Cliente> response = clienteController.actualizarCliente(1L, cliente);

        assertEquals(200, response.getStatusCodeValue());
        assertNull(response.getBody().getContraseña());
    }

    @Test
    void testActualizarClienteNoEncontrado() {
        when(clienteService.obtenerPorId(1L)).thenReturn(Optional.empty());

        ResponseEntity<Cliente> response = clienteController.actualizarCliente(1L, cliente);

        assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    void testEliminarCliente() {
        when(clienteService.obtenerPorId(1L)).thenReturn(Optional.of(cliente));
        doNothing().when(clienteService).eliminar(1L);

        ResponseEntity<Void> response = clienteController.eliminarCliente(1L);

        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void testEliminarClienteNoEncontrado() {
        when(clienteService.obtenerPorId(1L)).thenReturn(Optional.empty());

        ResponseEntity<Void> response = clienteController.eliminarCliente(1L);

        assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    void testObtenerPerfil() {
        // Given
        String email = "test@example.com";
        Cliente cliente = new Cliente();
        cliente.setId(1L);
        cliente.setEmail(email);
        cliente.setContraseña("password");

        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(email);
        SecurityContextHolder.setContext(securityContext);

        when(clienteService.obtenerPorEmail(email)).thenReturn(Optional.of(cliente));

        // When
        ResponseEntity<Cliente> response = clienteController.obtenerPerfil();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNull(response.getBody().getContraseña()); // Password should be null
        assertEquals(email, response.getBody().getEmail());

        verify(clienteService).obtenerPorEmail(email);
    }

    @Test
    void testObtenerPerfil_Exception() {
        // Given
        String email = "test@example.com";

        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(email);
        SecurityContextHolder.setContext(securityContext);

        // Simulate exception in service call
        when(clienteService.obtenerPorEmail(email)).thenThrow(new RuntimeException("Database error"));

        // When
        ResponseEntity<Cliente> response = clienteController.obtenerPerfil();

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());

        verify(clienteService).obtenerPorEmail(email);
    }

    @Test
    void testObtenerPerfil_ClienteNoEncontrado() {
        // Given
        String email = "test@example.com";

        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(email);
        SecurityContextHolder.setContext(securityContext);

        when(clienteService.obtenerPorEmail(email)).thenReturn(Optional.empty());

        // When
        ResponseEntity<Cliente> response = clienteController.obtenerPerfil();

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());

        verify(clienteService).obtenerPorEmail(email);
    }
}