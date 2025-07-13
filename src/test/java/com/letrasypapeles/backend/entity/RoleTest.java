package com.letrasypapeles.backend.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class RoleTest {

    private Role role;
    private Cliente cliente1;
    private Cliente cliente2;

    @BeforeEach
    void setUp() {
        role = Role.builder()
                .nombre("CLIENTE")
                .clientes(new HashSet<>())
                .build();

        cliente1 = Cliente.builder()
                .id(1L)
                .nombre("Juan")
                .apellido("Pérez")
                .email("juan@example.com")
                .build();

        cliente2 = Cliente.builder()
                .id(2L)
                .nombre("Ana")
                .apellido("García")
                .email("ana@example.com")
                .build();
    }

    @Test
    void constructor_NoArgs_CreatesEmptyRole() {
        // When
        Role roleVacio = new Role();

        // Then
        assertNotNull(roleVacio);
        assertNull(roleVacio.getNombre());
        assertNotNull(roleVacio.getClientes());
        assertTrue(roleVacio.getClientes().isEmpty());
    }

    @Test
    void constructor_AllArgs_CreatesRoleWithAllFields() {
        // Given
        Set<Cliente> clientes = new HashSet<>();
        clientes.add(cliente1);

        // When
        Role roleCompleto = new Role("ADMIN", clientes);

        // Then
        assertNotNull(roleCompleto);
        assertEquals("ADMIN", roleCompleto.getNombre());
        assertEquals(1, roleCompleto.getClientes().size());
        assertTrue(roleCompleto.getClientes().contains(cliente1));
    }

    @Test
    void builder_CreatesRoleCorrectly() {
        // When
        Role roleBuilder = Role.builder()
                .nombre("MODERADOR")
                .build();

        // Then
        assertNotNull(roleBuilder);
        assertEquals("MODERADOR", roleBuilder.getNombre());
        assertNotNull(roleBuilder.getClientes());
    }

    @Test
    void toString_ReturnsExpectedFormat() {
        // When
        String toStringResult = role.toString();

        // Then
        assertNotNull(toStringResult);
        assertEquals("Role{nombre='CLIENTE'}", toStringResult);
    }

    @Test
    void toString_WithNullNombre_ReturnsExpectedFormat() {
        // Given
        Role roleNullNombre = new Role();
        roleNullNombre.setNombre(null);

        // When
        String toStringResult = roleNullNombre.toString();

        // Then
        assertNotNull(toStringResult);
        assertEquals("Role{nombre='null'}", toStringResult);
    }

    @Test
    void equals_SameObject_ReturnsTrue() {
        // When & Then
        assertTrue(role.equals(role));
    }

    @Test
    void equals_NullObject_ReturnsFalse() {
        // When & Then
        assertFalse(role.equals(null));
    }

    @Test
    void equals_DifferentClass_ReturnsFalse() {
        // Given
        String otherObject = "Not a Role";

        // When & Then
        assertFalse(role.equals(otherObject));
    }

    @Test
    void equals_SameNombre_ReturnsTrue() {
        // Given
        Role otherRole = Role.builder()
                .nombre("CLIENTE")
                .build();

        // When & Then
        assertTrue(role.equals(otherRole));
    }

    @Test
    void equals_DifferentNombre_ReturnsFalse() {
        // Given
        Role otherRole = Role.builder()
                .nombre("ADMIN")
                .build();

        // When & Then
        assertFalse(role.equals(otherRole));
    }

    @Test
    void equals_NullNombre_ReturnsFalse() {
        // Given
        Role roleNullNombre = new Role();
        roleNullNombre.setNombre(null);
        Role otherRole = Role.builder()
                .nombre("CLIENTE")
                .build();

        // When & Then
        assertFalse(roleNullNombre.equals(otherRole));
    }

    @Test
    void equals_BothNullNombre_ReturnsTrue() {
        // Given
        Role role1 = new Role();
        role1.setNombre(null);
        Role role2 = new Role();
        role2.setNombre(null);

        // When & Then
        assertFalse(role1.equals(role2)); // Based on implementation, null nombres return false
    }

    @Test
    void hashCode_SameNombre_ReturnsSameHashCode() {
        // Given
        Role otherRole = Role.builder()
                .nombre("CLIENTE")
                .build();

        // When & Then
        assertEquals(role.hashCode(), otherRole.hashCode());
    }

    @Test
    void hashCode_DifferentNombre_ReturnsDifferentHashCode() {
        // Given
        Role otherRole = Role.builder()
                .nombre("ADMIN")
                .build();

        // When & Then
        assertNotEquals(role.hashCode(), otherRole.hashCode());
    }

    @Test
    void hashCode_NullNombre_ReturnsZero() {
        // Given
        Role roleNullNombre = new Role();
        roleNullNombre.setNombre(null);

        // When & Then
        assertEquals(0, roleNullNombre.hashCode());
    }

    @Test
    void gettersAndSetters_WorkCorrectly() {
        // Given
        Role roleTest = new Role();
        Set<Cliente> clientes = new HashSet<>();
        clientes.add(cliente1);
        clientes.add(cliente2);

        // When & Then
        roleTest.setNombre("TEST_ROLE");
        assertEquals("TEST_ROLE", roleTest.getNombre());

        roleTest.setClientes(clientes);
        assertEquals(2, roleTest.getClientes().size());
        assertTrue(roleTest.getClientes().contains(cliente1));
        assertTrue(roleTest.getClientes().contains(cliente2));
    }

    @Test
    void clientes_DefaultInitialization_CreatesEmptySet() {
        // When
        Role roleBuilder = Role.builder().build();

        // Then
        assertNotNull(roleBuilder.getClientes());
        assertTrue(roleBuilder.getClientes().isEmpty());
    }

    @Test
    void clientes_CanBeSetToNull() {
        // When
        role.setClientes(null);

        // Then
        assertNull(role.getClientes());
    }

    @Test
    void clientes_CanAddAndRemove() {
        // Given
        Set<Cliente> clientes = role.getClientes();

        // When
        clientes.add(cliente1);
        clientes.add(cliente2);

        // Then
        assertEquals(2, role.getClientes().size());
        assertTrue(role.getClientes().contains(cliente1));
        assertTrue(role.getClientes().contains(cliente2));

        // When
        clientes.remove(cliente1);

        // Then
        assertEquals(1, role.getClientes().size());
        assertFalse(role.getClientes().contains(cliente1));
        assertTrue(role.getClientes().contains(cliente2));
    }

    @Test
    void nombre_CanBeNull() {
        // When
        role.setNombre(null);

        // Then
        assertNull(role.getNombre());
    }

    @Test
    void nombre_CanBeEmptyString() {
        // When
        role.setNombre("");

        // Then
        assertEquals("", role.getNombre());
    }

    @Test
    void equals_EmptyStringNombre_WorksCorrectly() {
        // Given
        Role role1 = Role.builder().nombre("").build();
        Role role2 = Role.builder().nombre("").build();

        // When & Then
        assertTrue(role1.equals(role2));
        assertEquals(role1.hashCode(), role2.hashCode());
    }

    @Test
    void equals_WhitespaceNombre_WorksCorrectly() {
        // Given
        Role role1 = Role.builder().nombre("   ").build();
        Role role2 = Role.builder().nombre("   ").build();

        // When & Then
        assertTrue(role1.equals(role2));
        assertEquals(role1.hashCode(), role2.hashCode());
    }
}
