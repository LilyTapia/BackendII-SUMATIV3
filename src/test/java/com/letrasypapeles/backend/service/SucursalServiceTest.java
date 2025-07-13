package com.letrasypapeles.backend.service;

import com.letrasypapeles.backend.entity.Sucursal;
import com.letrasypapeles.backend.repository.SucursalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SucursalServiceTest {

    @Mock
    private SucursalRepository sucursalRepository;

    @InjectMocks
    private SucursalService sucursalService;

    private Sucursal sucursal;

    @BeforeEach
    void setUp() {
        // Configurar datos de prueba
        sucursal = Sucursal.builder()
                .id(1L)
                .nombre("Tienda Central")
                .direccion("Calle Principal 123")
                .region("Metropolitana")
                .build();
    }

    @Test
    void obtenerTodas() {
        // Given
        List<Sucursal> sucursales = Arrays.asList(sucursal);
        when(sucursalRepository.findAll()).thenReturn(sucursales);

        // When
        List<Sucursal> result = sucursalService.obtenerTodas();

        // Then
        assertEquals(1, result.size());
        assertEquals("Tienda Central", result.get(0).getNombre());
        verify(sucursalRepository, times(1)).findAll();
    }

    @Test
    void obtenerPorId() {
        // Given
        when(sucursalRepository.findById(1L)).thenReturn(Optional.of(sucursal));

        // When
        Optional<Sucursal> result = sucursalService.obtenerPorId(1L);

        // Then
        assertTrue(result.isPresent());
        assertEquals("Tienda Central", result.get().getNombre());
        assertEquals("Calle Principal 123", result.get().getDireccion());
        assertEquals("Metropolitana", result.get().getRegion());
        verify(sucursalRepository, times(1)).findById(1L);
    }

    @Test
    void obtenerPorIdNoExistente() {
        // Given
        when(sucursalRepository.findById(99L)).thenReturn(Optional.empty());

        // When
        Optional<Sucursal> result = sucursalService.obtenerPorId(99L);

        // Then
        assertFalse(result.isPresent());
        verify(sucursalRepository, times(1)).findById(99L);
    }

    @Test
    void guardar() {
        // Given
        Sucursal sucursalNueva = Sucursal.builder()
                .nombre("Tienda Norte")
                .direccion("Avenida Norte 456")
                .region("Norte")
                .build();

        Sucursal sucursalGuardada = Sucursal.builder()
                .id(2L)
                .nombre("Tienda Norte")
                .direccion("Avenida Norte 456")
                .region("Norte")
                .build();
        
        when(sucursalRepository.save(any(Sucursal.class))).thenReturn(sucursalGuardada);

        // When
        Sucursal result = sucursalService.guardar(sucursalNueva);

        // Then
        assertNotNull(result);
        assertEquals(2L, result.getId());
        assertEquals("Tienda Norte", result.getNombre());
        assertEquals("Avenida Norte 456", result.getDireccion());
        assertEquals("Norte", result.getRegion());
        verify(sucursalRepository, times(1)).save(any(Sucursal.class));
    }

    @Test
    void eliminar() {
        // Given
        Long idToDelete = 1L;
        doNothing().when(sucursalRepository).deleteById(idToDelete);

        // When
        sucursalService.eliminar(idToDelete);

        // Then
        verify(sucursalRepository, times(1)).deleteById(idToDelete);
    }
}
