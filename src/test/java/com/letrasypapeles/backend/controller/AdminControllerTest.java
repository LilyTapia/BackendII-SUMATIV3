package com.letrasypapeles.backend.controller;

import com.letrasypapeles.backend.entity.Cliente;
import com.letrasypapeles.backend.entity.Role;
import com.letrasypapeles.backend.service.ClienteService;
import com.letrasypapeles.backend.service.RoleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    @InjectMocks
    private AdminController adminController;

    @Mock
    private ClienteService clienteService;

    @Mock
    private RoleService roleService;

    private Cliente cliente;
    private Role role;

    @BeforeEach
    void setUp() {
        cliente = new Cliente();
        cliente.setId(1L);
        cliente.setNombre("Admin");
        cliente.setEmail("admin@test.com");

        role = new Role();
        role.setNombre("ADMIN");
    }

    @Test
    void testObtenerTodosLosUsuarios() {
        List<Cliente> usuarios = Arrays.asList(cliente);
        when(clienteService.obtenerTodosLosClientes()).thenReturn(usuarios);

        ResponseEntity<List<Cliente>> response = adminController.obtenerTodosLosUsuarios();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(usuarios, response.getBody());
        verify(clienteService).obtenerTodosLosClientes();
    }

    @Test
    void testObtenerTodosLosUsuarios_Exception() {
        when(clienteService.obtenerTodosLosClientes()).thenThrow(new RuntimeException("Database error"));

        ResponseEntity<List<Cliente>> response = adminController.obtenerTodosLosUsuarios();

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(clienteService).obtenerTodosLosClientes();
    }

    @Test
    void testCrearUsuario() {
        when(clienteService.registrarCliente(any(Cliente.class))).thenReturn(cliente);

        ResponseEntity<?> response = adminController.crearUsuario(cliente);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(cliente, response.getBody());
        verify(clienteService).registrarCliente(any(Cliente.class));
    }

    @Test
    void testCrearUsuario_Exception() {
        when(clienteService.registrarCliente(any(Cliente.class))).thenThrow(new RuntimeException("Email already exists"));

        ResponseEntity<?> response = adminController.crearUsuario(cliente);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(clienteService).registrarCliente(any(Cliente.class));
    }

    @Test
    void testActualizarRolesUsuario() {
        List<String> roles = Arrays.asList("ADMIN", "USER");
        Map<String, List<String>> request = new HashMap<>();
        request.put("roles", roles);

        when(clienteService.obtenerClientePorId(1L)).thenReturn(cliente);
        when(roleService.obtenerRolePorNombre("ADMIN")).thenReturn(role);
        when(roleService.obtenerRolePorNombre("USER")).thenReturn(new Role());

        ResponseEntity<?> response = adminController.actualizarRolesUsuario(1L, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(clienteService).obtenerClientePorId(1L);
        verify(clienteService).actualizarCliente(any(Cliente.class));
    }

    @Test
    void testActualizarRolesUsuario_RolesNull() {
        Map<String, List<String>> request = new HashMap<>();
        request.put("roles", null);

        ResponseEntity<?> response = adminController.actualizarRolesUsuario(1L, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testActualizarRolesUsuario_RolesVacios() {
        List<String> roles = new ArrayList<>();
        Map<String, List<String>> request = new HashMap<>();
        request.put("roles", roles);

        ResponseEntity<?> response = adminController.actualizarRolesUsuario(1L, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testActualizarRolesUsuario_UsuarioNoEncontrado() {
        List<String> roles = Arrays.asList("ADMIN");
        Map<String, List<String>> request = new HashMap<>();
        request.put("roles", roles);

        when(clienteService.obtenerClientePorId(1L)).thenReturn(null);

        ResponseEntity<?> response = adminController.actualizarRolesUsuario(1L, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(clienteService).obtenerClientePorId(1L);
    }

    @Test
    void testActualizarRolesUsuario_RoleNoEncontrado() {
        List<String> roles = Arrays.asList("INVALID_ROLE");
        Map<String, List<String>> request = new HashMap<>();
        request.put("roles", roles);

        when(clienteService.obtenerClientePorId(1L)).thenReturn(cliente);
        when(roleService.obtenerRolePorNombre("INVALID_ROLE")).thenReturn(null);

        ResponseEntity<?> response = adminController.actualizarRolesUsuario(1L, request);

        // The method filters out null roles, so it should succeed with empty role set
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(clienteService).obtenerClientePorId(1L);
        verify(roleService).obtenerRolePorNombre("INVALID_ROLE");
        verify(clienteService).actualizarCliente(any(Cliente.class));
    }

    @Test
    void testActualizarRolesUsuario_Exception() {
        List<String> roles = Arrays.asList("ADMIN");
        Map<String, List<String>> request = new HashMap<>();
        request.put("roles", roles);

        when(clienteService.obtenerClientePorId(1L)).thenThrow(new RuntimeException("Database error"));

        ResponseEntity<?> response = adminController.actualizarRolesUsuario(1L, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(clienteService).obtenerClientePorId(1L);
    }
}
