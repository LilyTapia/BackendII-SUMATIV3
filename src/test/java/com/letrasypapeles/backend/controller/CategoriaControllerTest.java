package com.letrasypapeles.backend.controller;

import com.letrasypapeles.backend.assembler.CategoriaModelAssembler;
import com.letrasypapeles.backend.entity.Categoria;
import com.letrasypapeles.backend.service.CategoriaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
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

    @Mock
    private CategoriaModelAssembler categoriaModelAssembler;

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
        EntityModel<Categoria> categoriaModel = EntityModel.of(categoria);
        when(categoriaService.obtenerTodas()).thenReturn(categorias);
        when(categoriaModelAssembler.toModel(categoria)).thenReturn(categoriaModel);

        ResponseEntity<CollectionModel<EntityModel<Categoria>>> response = categoriaController.obtenerTodas();

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().getContent().size());
    }

    @Test
    void testObtenerPorId() {
        EntityModel<Categoria> categoriaModel = EntityModel.of(categoria);
        when(categoriaService.obtenerPorId(1L)).thenReturn(Optional.of(categoria));
        when(categoriaModelAssembler.toModel(categoria)).thenReturn(categoriaModel);

        ResponseEntity<EntityModel<Categoria>> response = categoriaController.obtenerPorId(1L);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("Papelería", response.getBody().getContent().getNombre());
    }

    @Test
    void testObtenerPorIdNoEncontrado() {
        when(categoriaService.obtenerPorId(1L)).thenReturn(Optional.empty());

        ResponseEntity<EntityModel<Categoria>> response = categoriaController.obtenerPorId(1L);

        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void testCrearCategoria() {
        EntityModel<Categoria> categoriaModel = EntityModel.of(categoria);
        when(categoriaService.guardar(any(Categoria.class))).thenReturn(categoria);
        when(categoriaModelAssembler.toModel(categoria)).thenReturn(categoriaModel);

        ResponseEntity<EntityModel<Categoria>> response = categoriaController.crearCategoria(categoria);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("Papelería", response.getBody().getContent().getNombre());
    }

    @Test
    void testActualizarCategoria() {
        EntityModel<Categoria> categoriaModel = EntityModel.of(categoria);
        when(categoriaService.obtenerPorId(1L)).thenReturn(Optional.of(categoria));
        when(categoriaService.guardar(any(Categoria.class))).thenReturn(categoria);
        when(categoriaModelAssembler.toModel(categoria)).thenReturn(categoriaModel);

        ResponseEntity<EntityModel<Categoria>> response = categoriaController.actualizarCategoria(1L, categoria);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("Papelería", response.getBody().getContent().getNombre());
    }

    @Test
    void testActualizarCategoriaNoEncontrada() {
        when(categoriaService.obtenerPorId(1L)).thenReturn(Optional.empty());

        ResponseEntity<EntityModel<Categoria>> response = categoriaController.actualizarCategoria(1L, categoria);

        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void testEliminarCategoria() {
        when(categoriaService.obtenerPorId(1L)).thenReturn(Optional.of(categoria));
        doNothing().when(categoriaService).eliminar(1L);

        ResponseEntity<Void> response = categoriaController.eliminarCategoria(1L);

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void testEliminarCategoriaNoEncontrada() {
        when(categoriaService.obtenerPorId(1L)).thenReturn(Optional.empty());

        ResponseEntity<Void> response = categoriaController.eliminarCategoria(1L);

        assertEquals(404, response.getStatusCode().value());
    }
}