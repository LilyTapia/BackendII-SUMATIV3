package com.letrasypapeles.backend.service;

import com.letrasypapeles.backend.entity.Role;
import com.letrasypapeles.backend.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RoleServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private RoleService roleService;

    private Role role;

    @BeforeEach
    void setUp() {
        // Configurar datos de prueba
        role = Role.builder()
                .nombre("ROLE_USER")
                .clientes(new HashSet<>())
                .build();
    }

    @Test
    void obtenerTodos() {
        // Given
        List<Role> roles = Arrays.asList(role);
        when(roleRepository.findAll()).thenReturn(roles);

        // When
        List<Role> result = roleService.obtenerTodos();

        // Then
        assertEquals(1, result.size());
        assertEquals("ROLE_USER", result.get(0).getNombre());
        verify(roleRepository, times(1)).findAll();
    }

    @Test
    void obtenerPorNombre() {
        // Given
        when(roleRepository.findByNombre("ROLE_USER")).thenReturn(Optional.of(role));

        // When
        Optional<Role> result = roleService.obtenerPorNombre("ROLE_USER");

        // Then
        assertTrue(result.isPresent());
        assertEquals("ROLE_USER", result.get().getNombre());
        verify(roleRepository, times(1)).findByNombre("ROLE_USER");
    }

    @Test
    void obtenerPorNombreNoExistente() {
        // Given
        when(roleRepository.findByNombre("ROLE_NONEXISTENT")).thenReturn(Optional.empty());

        // When
        Optional<Role> result = roleService.obtenerPorNombre("ROLE_NONEXISTENT");

        // Then
        assertFalse(result.isPresent());
        verify(roleRepository, times(1)).findByNombre("ROLE_NONEXISTENT");
    }

    @Test
    void guardar() {
        // Given
        Role roleNuevo = Role.builder()
                .nombre("ROLE_ADMIN")
                .clientes(new HashSet<>())
                .build();
        
        when(roleRepository.save(any(Role.class))).thenReturn(roleNuevo);

        // When
        Role result = roleService.guardar(roleNuevo);

        // Then
        assertNotNull(result);
        assertEquals("ROLE_ADMIN", result.getNombre());
        verify(roleRepository, times(1)).save(any(Role.class));
    }

    @Test
    void eliminar() {
        // Given
        String nombreToDelete = "ROLE_USER";
        doNothing().when(roleRepository).deleteById(nombreToDelete);

        // When
        roleService.eliminar(nombreToDelete);

        // Then
        verify(roleRepository, times(1)).deleteById(nombreToDelete);
    }
}
