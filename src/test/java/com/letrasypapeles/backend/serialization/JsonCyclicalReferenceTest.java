package com.letrasypapeles.backend.serialization;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.letrasypapeles.backend.entity.Cliente;
import com.letrasypapeles.backend.entity.Role;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class JsonCyclicalReferenceTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testClienteJsonSerialization_NoCyclicalReferences() throws Exception {
        // Given
        Role clienteRole = Role.builder()
                .nombre("ROLE_CLIENTE")
                .build();

        Cliente cliente = Cliente.builder()
                .id(1L)
                .nombre("Juan")
                .apellido("Pérez")
                .email("juan@test.com")
                .contraseña("password123")
                .puntosFidelidad(100)
                .roles(Set.of(clienteRole))
                .build();

        // Add bidirectional reference
        clienteRole.getClientes().add(cliente);

        // When - This should NOT throw StackOverflowError
        String json = objectMapper.writeValueAsString(cliente);

        // Then
        assertNotNull(json);
        assertTrue(json.contains("\"nombre\":\"Juan\""));
        assertTrue(json.contains("\"email\":\"juan@test.com\""));
        assertTrue(json.contains("\"puntosFidelidad\":100"));
        
        // Verify sensitive data is NOT included
        assertFalse(json.contains("password123"), "Password should be @JsonIgnored");
        assertFalse(json.contains("roles"), "Roles should be @JsonIgnored");
        assertFalse(json.contains("ROLE_CLIENTE"), "Role details should not appear in JSON");
        
        // Verify no cyclical structure
        assertFalse(json.contains("clientes"), "Role.clientes should be @JsonIgnored");
        
        System.out.println("✅ Clean JSON (no cycles): " + json);
    }

    @Test
    void testRoleJsonSerialization_NoCyclicalReferences() throws Exception {
        // Given
        Cliente cliente = Cliente.builder()
                .id(1L)
                .nombre("Juan")
                .email("juan@test.com")
                .build();

        Role role = Role.builder()
                .nombre("ROLE_CLIENTE")
                .clientes(Set.of(cliente))
                .build();

        // When - This should NOT throw StackOverflowError
        String json = objectMapper.writeValueAsString(role);

        // Then
        assertNotNull(json);
        assertTrue(json.contains("\"nombre\":\"ROLE_CLIENTE\""));
        
        // Verify no cyclical structure
        assertFalse(json.contains("clientes"), "Role.clientes should be @JsonIgnored");
        assertFalse(json.contains("Juan"), "Cliente details should not appear in Role JSON");
        
        System.out.println("✅ Clean Role JSON (no cycles): " + json);
    }

    @Test
    void testComplexObjectGraph_NoCyclicalReferences() throws Exception {
        // Given - Create a complex object graph with potential cycles
        Role adminRole = Role.builder().nombre("ROLE_ADMIN").build();
        Role clienteRole = Role.builder().nombre("ROLE_CLIENTE").build();

        Cliente admin = Cliente.builder()
                .id(1L)
                .nombre("Admin")
                .email("admin@test.com")
                .roles(Set.of(adminRole))
                .build();

        Cliente cliente = Cliente.builder()
                .id(2L)
                .nombre("Cliente")
                .email("cliente@test.com")
                .roles(Set.of(clienteRole))
                .build();

        // Create bidirectional references
        adminRole.getClientes().add(admin);
        clienteRole.getClientes().add(cliente);

        // When - Serialize both objects
        String adminJson = objectMapper.writeValueAsString(admin);
        String clienteJson = objectMapper.writeValueAsString(cliente);

        // Then - Both should serialize without cycles
        assertNotNull(adminJson);
        assertNotNull(clienteJson);
        
        // Verify clean structure
        assertFalse(adminJson.contains("roles"));
        assertFalse(clienteJson.contains("roles"));
        
        System.out.println("✅ Complex graph serialized successfully");
        System.out.println("Admin JSON: " + adminJson);
        System.out.println("Cliente JSON: " + clienteJson);
    }
}
