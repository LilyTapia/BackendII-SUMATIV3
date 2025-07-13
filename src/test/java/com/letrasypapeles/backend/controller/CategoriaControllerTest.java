package com.letrasypapeles.backend.controller;

import com.letrasypapeles.backend.entity.Categoria;
import com.letrasypapeles.backend.service.CategoriaService;
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

class CategoriaControllerTest {

    @Mock
    private CategoriaService categoriaService;

    @InjectMocks
    private CategoriaController categoriaController;

    private Categoria categoria;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        categoria = new Categoria();
        categoria.setId(1L);
        categoria.setNombre("Papelería");
    }

    @Test
    void testObtenerTodas() {
        List<Categoria> categorias = Arrays.asList(categoria);
        when(categoriaService.obtenerTodas()).thenReturn(categorias);

        ResponseEntity<List<Categoria>> response = categoriaController.obtenerTodas();

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().size());
        assertEquals("Papelería", response.getBody().get(0).getNombre());
    }

    @Test
    void testObtenerPorId() {
        when(categoriaService.obtenerPorId(1L)).thenReturn(Optional.of(categoria));

        ResponseEntity<Categoria> response = categoriaController.obtenerPorId(1L);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Papelería", response.getBody().getNombre());
    }

    @Test
    void testObtenerPorIdNoEncontrado() {
        when(categoriaService.obtenerPorId(1L)).thenReturn(Optional.empty());

        ResponseEntity<Categoria> response = categoriaController.obtenerPorId(1L);

        assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    void testCrearCategoria() {
        when(categoriaService.guardar(any(Categoria.class))).thenReturn(categoria);

        ResponseEntity<Categoria> response = categoriaController.crearCategoria(categoria);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Papelería", response.getBody().getNombre());
    }

    @Test
    void testActualizarCategoria() {
        when(categoriaService.obtenerPorId(1L)).thenReturn(Optional.of(categoria));
        when(categoriaService.guardar(any(Categoria.class))).thenReturn(categoria);

        ResponseEntity<Categoria> response = categoriaController.actualizarCategoria(1L, categoria);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Papelería", response.getBody().getNombre());
    }

    @Test
    void testActualizarCategoriaNoEncontrada() {
        when(categoriaService.obtenerPorId(1L)).thenReturn(Optional.empty());

        ResponseEntity<Categoria> response = categoriaController.actualizarCategoria(1L, categoria);

        assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    void testEliminarCategoria() {
        when(categoriaService.obtenerPorId(1L)).thenReturn(Optional.of(categoria));
        doNothing().when(categoriaService).eliminar(1L);

        ResponseEntity<Void> response = categoriaController.eliminarCategoria(1L);

        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void testEliminarCategoriaNoEncontrada() {
        when(categoriaService.obtenerPorId(1L)).thenReturn(Optional.empty());

        ResponseEntity<Void> response = categoriaController.eliminarCategoria(1L);

        assertEquals(404, response.getStatusCodeValue());
    }
}