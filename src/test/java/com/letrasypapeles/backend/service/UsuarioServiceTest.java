package com.letrasypapeles.backend.service;

import com.letrasypapeles.backend.entity.Cliente;
import com.letrasypapeles.backend.entity.Role;
import com.letrasypapeles.backend.repository.ClienteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UsuarioServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private UsuarioService usuarioService;

    private Cliente cliente;
    private Set<Role> roles;

    @BeforeEach
    void setUp() {
        // Configurar datos de prueba
        roles = new HashSet<>();
        roles.add(Role.builder().nombre("ROLE_USER").build());
        roles.add(Role.builder().nombre("ROLE_ADMIN").build());

        cliente = Cliente.builder()
                .id(1L)
                .nombre("Test")
                .apellido("Usuario")
                .email("test@example.com")
                .contraseña("password123")
                .roles(roles)
                .build();
    }

    @Test
    void loadUserByUsername_ClienteExistente_RetornaUserDetails() {
        // Given
        when(clienteRepository.findByEmail("test@example.com")).thenReturn(Optional.of(cliente));

        // When
        UserDetails userDetails = usuarioService.loadUserByUsername("test@example.com");

        // Then
        assertNotNull(userDetails);
        assertEquals("test@example.com", userDetails.getUsername());
        assertEquals("password123", userDetails.getPassword());
        assertEquals(2, userDetails.getAuthorities().size());
        
        boolean hasUserRole = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority -> authority.equals("ROLE_USER"));
        boolean hasAdminRole = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority -> authority.equals("ROLE_ADMIN"));
        
        assertTrue(hasUserRole);
        assertTrue(hasAdminRole);
        
        verify(clienteRepository, times(1)).findByEmail("test@example.com");
    }

    @Test
    void loadUserByUsername_ClienteNoExistente_LanzaExcepcion() {
        // Given
        when(clienteRepository.findByEmail("noexiste@example.com")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UsernameNotFoundException.class, () -> {
            usuarioService.loadUserByUsername("noexiste@example.com");
        });
        
        verify(clienteRepository, times(1)).findByEmail("noexiste@example.com");
    }

    @Test
    void loadUserByUsername_ClienteSinRoles_RetornaUserDetailsSinAutorizaciones() {
        // Given
        Cliente clienteSinRoles = Cliente.builder()
                .id(2L)
                .nombre("Sin")
                .apellido("Roles")
                .email("sinroles@example.com")
                .contraseña("password123")
                .roles(new HashSet<>())
                .build();
        
        when(clienteRepository.findByEmail("sinroles@example.com")).thenReturn(Optional.of(clienteSinRoles));

        // When
        UserDetails userDetails = usuarioService.loadUserByUsername("sinroles@example.com");

        // Then
        assertNotNull(userDetails);
        assertEquals("sinroles@example.com", userDetails.getUsername());
        assertEquals(0, userDetails.getAuthorities().size());
        
        verify(clienteRepository, times(1)).findByEmail("sinroles@example.com");
    }
}
