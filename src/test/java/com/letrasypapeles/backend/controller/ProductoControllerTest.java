package com.letrasypapeles.backend.controller;

import com.letrasypapeles.backend.assembler.ProductoModelAssembler;
import com.letrasypapeles.backend.entity.Producto;
import com.letrasypapeles.backend.service.ProductoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ProductoControllerTest {

    @Mock
    private ProductoService productoService;

    @Mock
    private ProductoModelAssembler productoModelAssembler;

    @InjectMocks
    private ProductoController productoController;

    private Producto producto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        producto = new Producto();
        producto.setId(1L);
        producto.setNombre("Cuaderno");
        BigDecimal precioo = new BigDecimal("10.45");
        producto.setPrecio(precioo);
    }

    @Test
    void testObtenerTodos() {
        List<Producto> productos = Arrays.asList(producto);
        EntityModel<Producto> productoModel = EntityModel.of(producto);
        when(productoService.obtenerTodos()).thenReturn(productos);
        when(productoModelAssembler.toModel(producto)).thenReturn(productoModel);

        ResponseEntity<CollectionModel<EntityModel<Producto>>> response = productoController.obtenerTodos();

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().getContent().size());
    }

    @Test
    void testObtenerPorId() {
        EntityModel<Producto> productoModel = EntityModel.of(producto);
        when(productoService.obtenerPorId(1L)).thenReturn(Optional.of(producto));
        when(productoModelAssembler.toModel(producto)).thenReturn(productoModel);

        ResponseEntity<EntityModel<Producto>> response = productoController.obtenerPorId(1L);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("Cuaderno", response.getBody().getContent().getNombre());
    }

    @Test
    void testObtenerPorIdNoEncontrado() {
        when(productoService.obtenerPorId(1L)).thenReturn(Optional.empty());

        ResponseEntity<EntityModel<Producto>> response = productoController.obtenerPorId(1L);

        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void testCrearProducto() {
        EntityModel<Producto> productoModel = EntityModel.of(producto);
        when(productoService.guardar(any(Producto.class))).thenReturn(producto);
        when(productoModelAssembler.toModel(producto)).thenReturn(productoModel);

        ResponseEntity<EntityModel<Producto>> response = productoController.crearProducto(producto);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("Cuaderno", response.getBody().getContent().getNombre());
    }

    @Test
    void testActualizarProducto() {
        EntityModel<Producto> productoModel = EntityModel.of(producto);
        when(productoService.obtenerPorId(1L)).thenReturn(Optional.of(producto));
        when(productoService.guardar(any(Producto.class))).thenReturn(producto);
        when(productoModelAssembler.toModel(producto)).thenReturn(productoModel);

        ResponseEntity<EntityModel<Producto>> response = productoController.actualizarProducto(1L, producto);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("Cuaderno", response.getBody().getContent().getNombre());
    }

    @Test
    void testActualizarProductoNoEncontrado() {
        when(productoService.obtenerPorId(1L)).thenReturn(Optional.empty());

        ResponseEntity<EntityModel<Producto>> response = productoController.actualizarProducto(1L, producto);

        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void testEliminarProducto() {
        when(productoService.obtenerPorId(1L)).thenReturn(Optional.of(producto));
        doNothing().when(productoService).eliminar(1L);

        ResponseEntity<Void> response = productoController.eliminarProducto(1L);

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void testEliminarProductoNoEncontrado() {
        when(productoService.obtenerPorId(1L)).thenReturn(Optional.empty());

        ResponseEntity<Void> response = productoController.eliminarProducto(1L);

        assertEquals(404, response.getStatusCode().value());
    }
}