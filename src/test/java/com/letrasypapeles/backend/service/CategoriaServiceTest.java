package com.letrasypapeles.backend.service;

import com.letrasypapeles.backend.entity.Categoria;
import com.letrasypapeles.backend.repository.CategoriaRepository;
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
public class CategoriaServiceTest {

    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private CategoriaService categoriaService;

    private Categoria categoria;

    @BeforeEach
    void setUp() {
        // Configurar datos de prueba
        categoria = Categoria.builder()
                .id(1L)
                .nombre("Ficción")
                .build();
    }

    @Test
    void obtenerTodas() {
        // Given
        List<Categoria> categorias = Arrays.asList(categoria);
        when(categoriaRepository.findAll()).thenReturn(categorias);

        // When
        List<Categoria> result = categoriaService.obtenerTodas();

        // Then
        assertEquals(1, result.size());
        assertEquals("Ficción", result.get(0).getNombre());
        verify(categoriaRepository, times(1)).findAll();
    }

    @Test
    void obtenerPorId() {
        // Given
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));

        // When
        Optional<Categoria> result = categoriaService.obtenerPorId(1L);

        // Then
        assertTrue(result.isPresent());
        assertEquals("Ficción", result.get().getNombre());
        verify(categoriaRepository, times(1)).findById(1L);
    }

    @Test
    void obtenerPorIdNoExistente() {
        // Given
        when(categoriaRepository.findById(99L)).thenReturn(Optional.empty());

        // When
        Optional<Categoria> result = categoriaService.obtenerPorId(99L);

        // Then
        assertFalse(result.isPresent());
        verify(categoriaRepository, times(1)).findById(99L);
    }

    @Test
    void guardar() {
        // Given
        Categoria categoriaNueva = Categoria.builder()
                .nombre("No Ficción")
                .build();
        Categoria categoriaGuardada = Categoria.builder()
                .id(2L)
                .nombre("No Ficción")
                .build();
        
        when(categoriaRepository.save(any(Categoria.class))).thenReturn(categoriaGuardada);

        // When
        Categoria result = categoriaService.guardar(categoriaNueva);

        // Then
        assertNotNull(result);
        assertEquals(2L, result.getId());
        assertEquals("No Ficción", result.getNombre());
        verify(categoriaRepository, times(1)).save(any(Categoria.class));
    }

    @Test
    void eliminar() {
        // Given
        Long idToDelete = 1L;
        doNothing().when(categoriaRepository).deleteById(idToDelete);

        // When
        categoriaService.eliminar(idToDelete);

        // Then
        verify(categoriaRepository, times(1)).deleteById(idToDelete);
    }
}
