package com.letrasypapeles.backend.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ClienteTest {

    private Cliente cliente;
    private Role roleCliente;
    private Role roleAdmin;

    @BeforeEach
    void setUp() {
        roleCliente = Role.builder()
                .nombre("CLIENTE")
                .clientes(new HashSet<>())
                .build();

        roleAdmin = Role.builder()
                .nombre("ADMIN")
                .clientes(new HashSet<>())
                .build();

        cliente = Cliente.builder()
                .id(1L)
                .nombre("Juan")
                .apellido("Pérez")
                .email("juan.perez@example.com")
                .contraseña("password123")
                .puntosFidelidad(100)
                .roles(new HashSet<>())
                .build();
    }

    @Test
    void constructor_NoArgs_CreatesEmptyCliente() {
        // When
        Cliente clienteVacio = new Cliente();

        // Then
        assertNotNull(clienteVacio);
        assertNull(clienteVacio.getId());
        assertNull(clienteVacio.getNombre());
        assertNull(clienteVacio.getApellido());
        assertNull(clienteVacio.getEmail());
        assertNull(clienteVacio.getContraseña());
        assertNull(clienteVacio.getPuntosFidelidad());
        assertNotNull(clienteVacio.getRoles());
        assertTrue(clienteVacio.getRoles().isEmpty());
    }

    @Test
    void constructor_AllArgs_CreatesClienteWithAllFields() {
        // Given
        Set<Role> roles = new HashSet<>();
        roles.add(roleCliente);

        // When
        Cliente clienteCompleto = new Cliente(1L, "Ana", "García", "ana@example.com", 
                "password456", 50, roles);

        // Then
        assertNotNull(clienteCompleto);
        assertEquals(1L, clienteCompleto.getId());
        assertEquals("Ana", clienteCompleto.getNombre());
        assertEquals("García", clienteCompleto.getApellido());
        assertEquals("ana@example.com", clienteCompleto.getEmail());
        assertEquals("password456", clienteCompleto.getContraseña());
        assertEquals(50, clienteCompleto.getPuntosFidelidad());
        assertEquals(1, clienteCompleto.getRoles().size());
        assertTrue(clienteCompleto.getRoles().contains(roleCliente));
    }

    @Test
    void builder_CreatesClienteCorrectly() {
        // When
        Cliente clienteBuilder = Cliente.builder()
                .id(2L)
                .nombre("Carlos")
                .apellido("López")
                .email("carlos@example.com")
                .contraseña("password789")
                .puntosFidelidad(200)
                .build();

        // Then
        assertNotNull(clienteBuilder);
        assertEquals(2L, clienteBuilder.getId());
        assertEquals("Carlos", clienteBuilder.getNombre());
        assertEquals("López", clienteBuilder.getApellido());
        assertEquals("carlos@example.com", clienteBuilder.getEmail());
        assertEquals("password789", clienteBuilder.getContraseña());
        assertEquals(200, clienteBuilder.getPuntosFidelidad());
        assertNotNull(clienteBuilder.getRoles());
    }

    @Test
    void addRole_WithNullRoles_InitializesAndAddsRole() {
        // Given
        Cliente clienteNullRoles = new Cliente();
        clienteNullRoles.setRoles(null);

        // When
        clienteNullRoles.addRole(roleCliente);

        // Then
        assertNotNull(clienteNullRoles.getRoles());
        assertEquals(1, clienteNullRoles.getRoles().size());
        assertTrue(clienteNullRoles.getRoles().contains(roleCliente));
    }

    @Test
    void addRole_WithExistingRoles_AddsNewRole() {
        // Given
        cliente.addRole(roleCliente);

        // When
        cliente.addRole(roleAdmin);

        // Then
        assertEquals(2, cliente.getRoles().size());
        assertTrue(cliente.getRoles().contains(roleCliente));
        assertTrue(cliente.getRoles().contains(roleAdmin));
    }

    @Test
    void addRole_WithSameRole_DoesNotDuplicate() {
        // Given
        cliente.addRole(roleCliente);

        // When
        cliente.addRole(roleCliente);

        // Then
        assertEquals(1, cliente.getRoles().size());
        assertTrue(cliente.getRoles().contains(roleCliente));
    }

    @Test
    void removeRole_WithExistingRole_RemovesRole() {
        // Given
        cliente.addRole(roleCliente);
        cliente.addRole(roleAdmin);

        // When
        cliente.removeRole(roleCliente);

        // Then
        assertEquals(1, cliente.getRoles().size());
        assertFalse(cliente.getRoles().contains(roleCliente));
        assertTrue(cliente.getRoles().contains(roleAdmin));
    }

    @Test
    void removeRole_WithNonExistingRole_DoesNothing() {
        // Given
        cliente.addRole(roleCliente);

        // When
        cliente.removeRole(roleAdmin);

        // Then
        assertEquals(1, cliente.getRoles().size());
        assertTrue(cliente.getRoles().contains(roleCliente));
    }

    @Test
    void removeRole_WithNullRoles_DoesNothing() {
        // Given
        Cliente clienteNullRoles = new Cliente();
        clienteNullRoles.setRoles(null);

        // When & Then (should not throw exception)
        assertDoesNotThrow(() -> clienteNullRoles.removeRole(roleCliente));
    }

    @Test
    void setApellido_SetsApellidoCorrectly() {
        // Given
        String nuevoApellido = "Nuevo Apellido";

        // When
        cliente.setApellido(nuevoApellido);

        // Then
        assertEquals(nuevoApellido, cliente.getApellido());
    }

    @Test
    void setApellido_WithNull_SetsNull() {
        // When
        cliente.setApellido(null);

        // Then
        assertNull(cliente.getApellido());
    }

    @Test
    void setApellido_WithEmptyString_SetsEmptyString() {
        // When
        cliente.setApellido("");

        // Then
        assertEquals("", cliente.getApellido());
    }

    @Test
    void gettersAndSetters_WorkCorrectly() {
        // Given
        Cliente clienteTest = new Cliente();

        // When & Then
        clienteTest.setId(10L);
        assertEquals(10L, clienteTest.getId());

        clienteTest.setNombre("Test Nombre");
        assertEquals("Test Nombre", clienteTest.getNombre());

        clienteTest.setEmail("test@test.com");
        assertEquals("test@test.com", clienteTest.getEmail());

        clienteTest.setContraseña("testPassword");
        assertEquals("testPassword", clienteTest.getContraseña());

        clienteTest.setPuntosFidelidad(500);
        assertEquals(500, clienteTest.getPuntosFidelidad());
    }

    @Test
    void toString_ContainsExpectedFields() {
        // When
        String toStringResult = cliente.toString();

        // Then
        assertNotNull(toStringResult);
        assertTrue(toStringResult.contains("Juan"));
        assertTrue(toStringResult.contains("Pérez"));
        assertTrue(toStringResult.contains("juan.perez@example.com"));
        assertTrue(toStringResult.contains("100"));
    }

    @Test
    void roles_DefaultInitialization_CreatesEmptySet() {
        // When
        Cliente clienteBuilder = Cliente.builder().build();

        // Then
        assertNotNull(clienteBuilder.getRoles());
        assertTrue(clienteBuilder.getRoles().isEmpty());
    }

    @Test
    void roles_CanBeSetAndRetrieved() {
        // Given
        Set<Role> newRoles = new HashSet<>();
        newRoles.add(roleCliente);
        newRoles.add(roleAdmin);

        // When
        cliente.setRoles(newRoles);

        // Then
        assertEquals(2, cliente.getRoles().size());
        assertTrue(cliente.getRoles().contains(roleCliente));
        assertTrue(cliente.getRoles().contains(roleAdmin));
    }

    @Test
    void puntosFidelidad_CanBeNull() {
        // Given
        Cliente clienteTest = new Cliente();

        // When
        clienteTest.setPuntosFidelidad(null);

        // Then
        assertNull(clienteTest.getPuntosFidelidad());
    }

    @Test
    void puntosFidelidad_CanBeZero() {
        // When
        cliente.setPuntosFidelidad(0);

        // Then
        assertEquals(0, cliente.getPuntosFidelidad());
    }

    @Test
    void puntosFidelidad_CanBeNegative() {
        // When
        cliente.setPuntosFidelidad(-10);

        // Then
        assertEquals(-10, cliente.getPuntosFidelidad());
    }
}
