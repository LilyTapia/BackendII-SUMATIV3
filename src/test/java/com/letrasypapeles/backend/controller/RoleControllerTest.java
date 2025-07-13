package com.letrasypapeles.backend.controller;

import com.letrasypapeles.backend.entity.Role;
import com.letrasypapeles.backend.service.RoleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RoleControllerTest {

    @Mock
    private RoleService roleService;

    @InjectMocks
    private RoleController roleController;

    private Role role;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        role = new Role();
        role.setNombre("CLIENTE");
    }

    @Test
    void testObtenerTodos() {
        List<Role> roles = Arrays.asList(role);
        when(roleService.obtenerTodos()).thenReturn(roles);

        ResponseEntity<List<Role>> response = roleController.obtenerTodos();

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testObtenerPorNombre() {
        when(roleService.obtenerPorNombre("CLIENTE")).thenReturn(Optional.of(role));

        ResponseEntity<Role> response = roleController.obtenerPorNombre("CLIENTE");

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("CLIENTE", response.getBody().getNombre());
    }

    @Test
    void testObtenerPorNombreNoEncontrado() {
        when(roleService.obtenerPorNombre("ADMIN")).thenReturn(Optional.empty());

        ResponseEntity<Role> response = roleController.obtenerPorNombre("ADMIN");

        assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    void testCrearRole() {
        when(roleService.guardar(any(Role.class))).thenReturn(role);

        ResponseEntity<Role> response = roleController.crearRole(role);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("CLIENTE", response.getBody().getNombre());
    }

    @Test
    void testEliminarRole() {
        when(roleService.obtenerPorNombre("CLIENTE")).thenReturn(Optional.of(role));
        doNothing().when(roleService).eliminar("CLIENTE");

        ResponseEntity<Void> response = roleController.eliminarRole("CLIENTE");

        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void testEliminarRoleNoEncontrado() {
        when(roleService.obtenerPorNombre("ADMIN")).thenReturn(Optional.empty());

        ResponseEntity<Void> response = roleController.eliminarRole("ADMIN");

        assertEquals(404, response.getStatusCodeValue());
    }
}