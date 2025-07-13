package com.letrasypapeles.backend.controller;

import com.letrasypapeles.backend.entity.Inventario;
import com.letrasypapeles.backend.entity.Producto;
import com.letrasypapeles.backend.service.InventarioService;
import com.letrasypapeles.backend.service.ProductoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventarioControllerTest {

    @Mock
    private InventarioService inventarioService;

    @Mock
    private ProductoService productoService;

    @InjectMocks
    private InventarioController inventarioController;

    private Inventario inventario;
    private Producto producto;

    @BeforeEach
    void setUp() {
        producto = new Producto();
        producto.setId(1L);
        producto.setNombre("Producto Test");

        inventario = new Inventario();
        inventario.setId(1L);
        inventario.setProducto(producto);
        inventario.setCantidad(100);
    }

    @Test
    void testObtenerTodos() {
        List<Inventario> inventarios = Arrays.asList(inventario);
        when(inventarioService.obtenerTodos()).thenReturn(inventarios);

        ResponseEntity<List<Inventario>> response = inventarioController.obtenerTodos();

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testObtenerPorId() {
        when(inventarioService.obtenerPorId(1L)).thenReturn(Optional.of(inventario));

        ResponseEntity<Inventario> response = inventarioController.obtenerPorId(1L);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(100, response.getBody().getCantidad());
    }

    @Test
    void testObtenerPorIdNoEncontrado() {
        when(inventarioService.obtenerPorId(1L)).thenReturn(Optional.empty());

        ResponseEntity<Inventario> response = inventarioController.obtenerPorId(1L);

        assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    void testObtenerPorProductoId() {
        List<Inventario> inventarios = Arrays.asList(inventario);
        when(inventarioService.obtenerPorProductoId(1L)).thenReturn(inventarios);

        ResponseEntity<List<Inventario>> response = inventarioController.obtenerPorProductoId(1L);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testObtenerPorSucursalId() {
        List<Inventario> inventarios = Arrays.asList(inventario);
        when(inventarioService.obtenerPorSucursalId(1L)).thenReturn(inventarios);

        ResponseEntity<List<Inventario>> response = inventarioController.obtenerPorSucursalId(1L);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testCrearInventario() {
        when(inventarioService.guardar(any(Inventario.class))).thenReturn(inventario);

        ResponseEntity<Inventario> response = inventarioController.crearInventario(inventario);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(100, response.getBody().getCantidad());
    }

    @Test
    void testActualizarInventario() {
        when(inventarioService.obtenerPorId(1L)).thenReturn(Optional.of(inventario));
        when(inventarioService.guardar(any(Inventario.class))).thenReturn(inventario);

        ResponseEntity<Inventario> response = inventarioController.actualizarInventario(1L, inventario);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(100, response.getBody().getCantidad());
    }

    @Test
    void testActualizarInventarioNoEncontrado() {
        when(inventarioService.obtenerPorId(1L)).thenReturn(Optional.empty());

        ResponseEntity<Inventario> response = inventarioController.actualizarInventario(1L, inventario);

        assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    void testEliminarInventario() {
        when(inventarioService.obtenerPorId(1L)).thenReturn(Optional.of(inventario));
        doNothing().when(inventarioService).eliminar(1L);

        ResponseEntity<Void> response = inventarioController.eliminarInventario(1L);

        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void testEliminarInventarioNoEncontrado() {
        when(inventarioService.obtenerPorId(1L)).thenReturn(Optional.empty());

        ResponseEntity<Void> response = inventarioController.eliminarInventario(1L);

        assertEquals(404, response.getStatusCodeValue());
    }

    // Tests for singular endpoints to achieve 100% coverage
    @Test
    void testCrearInventarioSingular() {
        Map<String, Object> request = new HashMap<>();
        request.put("productoId", 1L);
        request.put("cantidad", 50);

        when(productoService.obtenerPorId(1L)).thenReturn(Optional.of(producto));
        when(inventarioService.guardar(any(Inventario.class))).thenReturn(inventario);

        ResponseEntity<?> response = inventarioController.crearInventarioSingular(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(productoService).obtenerPorId(1L);
        verify(inventarioService).guardar(any(Inventario.class));
    }

    @Test
    void testCrearInventarioSingular_ProductoNoEncontrado() {
        Map<String, Object> request = new HashMap<>();
        request.put("productoId", 1L);
        request.put("cantidad", 50);

        when(productoService.obtenerPorId(1L)).thenReturn(Optional.empty());

        ResponseEntity<?> response = inventarioController.crearInventarioSingular(request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(productoService).obtenerPorId(1L);
        verify(inventarioService, never()).guardar(any(Inventario.class));
    }

    @Test
    void testCrearInventarioSingular_Exception() {
        Map<String, Object> request = new HashMap<>();
        request.put("productoId", 1L);
        request.put("cantidad", 50);

        when(productoService.obtenerPorId(1L)).thenReturn(Optional.of(producto));
        when(inventarioService.guardar(any(Inventario.class))).thenThrow(new RuntimeException("Database error"));

        ResponseEntity<?> response = inventarioController.crearInventarioSingular(request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(productoService).obtenerPorId(1L);
        verify(inventarioService).guardar(any(Inventario.class));
    }

    @Test
    void testActualizarInventarioSingular() {
        Map<String, Object> request = new HashMap<>();
        request.put("cantidad", 75);

        when(inventarioService.obtenerPorId(1L)).thenReturn(Optional.of(inventario));
        when(inventarioService.guardar(any(Inventario.class))).thenReturn(inventario);

        ResponseEntity<?> response = inventarioController.actualizarInventarioSingular(1L, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(inventarioService).obtenerPorId(1L);
        verify(inventarioService).guardar(any(Inventario.class));
    }

    @Test
    void testActualizarInventarioSingular_NoEncontrado() {
        Map<String, Object> request = new HashMap<>();
        request.put("cantidad", 75);

        when(inventarioService.obtenerPorId(1L)).thenReturn(Optional.empty());

        ResponseEntity<?> response = inventarioController.actualizarInventarioSingular(1L, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(inventarioService).obtenerPorId(1L);
        verify(inventarioService, never()).guardar(any(Inventario.class));
    }

    @Test
    void testActualizarInventarioSingular_Exception() {
        Map<String, Object> request = new HashMap<>();
        request.put("cantidad", 75);

        when(inventarioService.obtenerPorId(1L)).thenReturn(Optional.of(inventario));
        when(inventarioService.guardar(any(Inventario.class))).thenThrow(new RuntimeException("Database error"));

        ResponseEntity<?> response = inventarioController.actualizarInventarioSingular(1L, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(inventarioService).obtenerPorId(1L);
        verify(inventarioService).guardar(any(Inventario.class));
    }

    @Test
    void testObtenerInventarioSingular() {
        when(inventarioService.obtenerPorId(1L)).thenReturn(Optional.of(inventario));

        ResponseEntity<?> response = inventarioController.obtenerInventarioSingular(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(inventario, response.getBody());
        verify(inventarioService).obtenerPorId(1L);
    }

    @Test
    void testObtenerInventarioSingular_NoEncontrado() {
        when(inventarioService.obtenerPorId(1L)).thenReturn(Optional.empty());

        ResponseEntity<?> response = inventarioController.obtenerInventarioSingular(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(inventarioService).obtenerPorId(1L);
    }

    @Test
    void testObtenerInventarioSingular_Exception() {
        when(inventarioService.obtenerPorId(1L)).thenThrow(new RuntimeException("Database error"));

        ResponseEntity<?> response = inventarioController.obtenerInventarioSingular(1L);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(inventarioService).obtenerPorId(1L);
    }
}