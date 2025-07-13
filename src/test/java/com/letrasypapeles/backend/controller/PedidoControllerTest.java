package com.letrasypapeles.backend.controller;

import com.letrasypapeles.backend.assembler.PedidoModelAssembler;
import com.letrasypapeles.backend.entity.Pedido;
import com.letrasypapeles.backend.service.PedidoService;
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

class PedidoControllerTest {

    @Mock
    private PedidoService pedidoService;

    @Mock
    private PedidoModelAssembler pedidoModelAssembler;

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
        EntityModel<Pedido> pedidoModel = EntityModel.of(pedido);
        when(pedidoService.obtenerTodos()).thenReturn(pedidos);
        when(pedidoModelAssembler.toModel(pedido)).thenReturn(pedidoModel);

        ResponseEntity<CollectionModel<EntityModel<Pedido>>> response = pedidoController.obtenerTodos();

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().getContent().size());
    }

    @Test
    void testObtenerPorId() {
        EntityModel<Pedido> pedidoModel = EntityModel.of(pedido);
        when(pedidoService.obtenerPorId(1L)).thenReturn(Optional.of(pedido));
        when(pedidoModelAssembler.toModel(pedido)).thenReturn(pedidoModel);

        ResponseEntity<EntityModel<Pedido>> response = pedidoController.obtenerPorId(1L);

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void testObtenerPorIdNoEncontrado() {
        when(pedidoService.obtenerPorId(1L)).thenReturn(Optional.empty());

        ResponseEntity<EntityModel<Pedido>> response = pedidoController.obtenerPorId(1L);

        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void testObtenerPorClienteId() {
        List<Pedido> pedidos = Arrays.asList(pedido);
        EntityModel<Pedido> pedidoModel = EntityModel.of(pedido);
        when(pedidoService.obtenerPorClienteId(1L)).thenReturn(pedidos);
        when(pedidoModelAssembler.toModel(pedido)).thenReturn(pedidoModel);

        ResponseEntity<CollectionModel<EntityModel<Pedido>>> response = pedidoController.obtenerPorClienteId(1L);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().getContent().size());
    }

    @Test
    void testCrearPedido() {
        EntityModel<Pedido> pedidoModel = EntityModel.of(pedido);
        when(pedidoService.guardar(any(Pedido.class))).thenReturn(pedido);
        when(pedidoModelAssembler.toModel(pedido)).thenReturn(pedidoModel);

        ResponseEntity<EntityModel<Pedido>> response = pedidoController.crearPedido(pedido);

        assertEquals(201, response.getStatusCode().value());
    }

    @Test
    void testActualizarPedido() {
        EntityModel<Pedido> pedidoModel = EntityModel.of(pedido);
        when(pedidoService.obtenerPorId(1L)).thenReturn(Optional.of(pedido));
        when(pedidoService.guardar(any(Pedido.class))).thenReturn(pedido);
        when(pedidoModelAssembler.toModel(pedido)).thenReturn(pedidoModel);

        ResponseEntity<EntityModel<Pedido>> response = pedidoController.actualizarPedido(1L, pedido);

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void testActualizarPedidoNoEncontrado() {
        when(pedidoService.obtenerPorId(1L)).thenReturn(Optional.empty());

        ResponseEntity<EntityModel<Pedido>> response = pedidoController.actualizarPedido(1L, pedido);

        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void testEliminarPedido() {
        when(pedidoService.obtenerPorId(1L)).thenReturn(Optional.of(pedido));
        doNothing().when(pedidoService).eliminar(1L);

        ResponseEntity<Void> response = pedidoController.eliminarPedido(1L);

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void testEliminarPedidoNoEncontrado() {
        when(pedidoService.obtenerPorId(1L)).thenReturn(Optional.empty());

        ResponseEntity<Void> response = pedidoController.eliminarPedido(1L);

        assertEquals(404, response.getStatusCode().value());
    }
}