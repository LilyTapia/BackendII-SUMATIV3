package com.letrasypapeles.backend.controller;

import com.letrasypapeles.backend.entity.Pedido;
import com.letrasypapeles.backend.service.PedidoService;
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

class PedidoControllerTest {

    @Mock
    private PedidoService pedidoService;

    @InjectMocks
    private PedidoController pedidoController;

    private Pedido pedido;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        pedido = new Pedido();
        pedido.setId(1L);
    }

    @Test
    void testObtenerTodos() {
        List<Pedido> pedidos = Arrays.asList(pedido);
        when(pedidoService.obtenerTodos()).thenReturn(pedidos);

        ResponseEntity<List<Pedido>> response = pedidoController.obtenerTodos();

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testObtenerPorId() {
        when(pedidoService.obtenerPorId(1L)).thenReturn(Optional.of(pedido));

        ResponseEntity<Pedido> response = pedidoController.obtenerPorId(1L);

        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void testObtenerPorIdNoEncontrado() {
        when(pedidoService.obtenerPorId(1L)).thenReturn(Optional.empty());

        ResponseEntity<Pedido> response = pedidoController.obtenerPorId(1L);

        assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    void testObtenerPorClienteId() {
        List<Pedido> pedidos = Arrays.asList(pedido);
        when(pedidoService.obtenerPorClienteId(1L)).thenReturn(pedidos);

        ResponseEntity<List<Pedido>> response = pedidoController.obtenerPorClienteId(1L);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testCrearPedido() {
        when(pedidoService.guardar(any(Pedido.class))).thenReturn(pedido);

        ResponseEntity<Pedido> response = pedidoController.crearPedido(pedido);

        assertEquals(201, response.getStatusCodeValue());
    }

    @Test
    void testActualizarPedido() {
        when(pedidoService.obtenerPorId(1L)).thenReturn(Optional.of(pedido));
        when(pedidoService.guardar(any(Pedido.class))).thenReturn(pedido);

        ResponseEntity<Pedido> response = pedidoController.actualizarPedido(1L, pedido);

        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void testActualizarPedidoNoEncontrado() {
        when(pedidoService.obtenerPorId(1L)).thenReturn(Optional.empty());

        ResponseEntity<Pedido> response = pedidoController.actualizarPedido(1L, pedido);

        assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    void testEliminarPedido() {
        when(pedidoService.obtenerPorId(1L)).thenReturn(Optional.of(pedido));
        doNothing().when(pedidoService).eliminar(1L);

        ResponseEntity<Void> response = pedidoController.eliminarPedido(1L);

        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void testEliminarPedidoNoEncontrado() {
        when(pedidoService.obtenerPorId(1L)).thenReturn(Optional.empty());

        ResponseEntity<Void> response = pedidoController.eliminarPedido(1L);

        assertEquals(404, response.getStatusCodeValue());
    }
}