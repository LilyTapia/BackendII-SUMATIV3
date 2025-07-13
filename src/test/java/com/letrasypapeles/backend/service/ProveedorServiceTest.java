package com.letrasypapeles.backend.service;

import com.letrasypapeles.backend.entity.Proveedor;
import com.letrasypapeles.backend.repository.ProveedorRepository;
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
public class ProveedorServiceTest {

    @Mock
    private ProveedorRepository proveedorRepository;

    @InjectMocks
    private ProveedorService proveedorService;

    private Proveedor proveedor;

    @BeforeEach
    void setUp() {
        // Configurar datos de prueba
        proveedor = Proveedor.builder()
                .id(1L)
                .nombre("Editorial XYZ")
                .contacto("contacto@editorialxyz.com")
                .build();
    }

    @Test
    void obtenerTodos() {
        // Given
        List<Proveedor> proveedores = Arrays.asList(proveedor);
        when(proveedorRepository.findAll()).thenReturn(proveedores);

        // When
        List<Proveedor> result = proveedorService.obtenerTodos();

        // Then
        assertEquals(1, result.size());
        assertEquals("Editorial XYZ", result.get(0).getNombre());
        verify(proveedorRepository, times(1)).findAll();
    }

    @Test
    void obtenerPorId() {
        // Given
        when(proveedorRepository.findById(1L)).thenReturn(Optional.of(proveedor));

        // When
        Optional<Proveedor> result = proveedorService.obtenerPorId(1L);

        // Then
        assertTrue(result.isPresent());
        assertEquals("Editorial XYZ", result.get().getNombre());
        assertEquals("contacto@editorialxyz.com", result.get().getContacto());
        verify(proveedorRepository, times(1)).findById(1L);
    }

    @Test
    void obtenerPorIdNoExistente() {
        // Given
        when(proveedorRepository.findById(99L)).thenReturn(Optional.empty());

        // When
        Optional<Proveedor> result = proveedorService.obtenerPorId(99L);

        // Then
        assertFalse(result.isPresent());
        verify(proveedorRepository, times(1)).findById(99L);
    }

    @Test
    void guardar() {
        // Given
        Proveedor proveedorNuevo = Proveedor.builder()
                .nombre("Editorial ABC")
                .contacto("contacto@editorialabc.com")
                .build();

        Proveedor proveedorGuardado = Proveedor.builder()
                .id(2L)
                .nombre("Editorial ABC")
                .contacto("contacto@editorialabc.com")
                .build();
        
        when(proveedorRepository.save(any(Proveedor.class))).thenReturn(proveedorGuardado);

        // When
        Proveedor result = proveedorService.guardar(proveedorNuevo);

        // Then
        assertNotNull(result);
        assertEquals(2L, result.getId());
        assertEquals("Editorial ABC", result.getNombre());
        assertEquals("contacto@editorialabc.com", result.getContacto());
        verify(proveedorRepository, times(1)).save(any(Proveedor.class));
    }

    @Test
    void eliminar() {
        // Given
        Long idToDelete = 1L;
        doNothing().when(proveedorRepository).deleteById(idToDelete);

        // When
        proveedorService.eliminar(idToDelete);

        // Then
        verify(proveedorRepository, times(1)).deleteById(idToDelete);
    }
}
