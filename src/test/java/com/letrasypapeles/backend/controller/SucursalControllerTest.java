package com.letrasypapeles.backend.controller;

import com.letrasypapeles.backend.entity.Sucursal;
import com.letrasypapeles.backend.service.SucursalService;
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

class SucursalControllerTest {

    @Mock
    private SucursalService sucursalService;

    @InjectMocks
    private SucursalController sucursalController;

    private Sucursal sucursal;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        sucursal = new Sucursal();
        sucursal.setId(1L);
        sucursal.setNombre("Sucursal Centro");
        sucursal.setDireccion("Av. Principal 123");
    }

    @Test
    void testObtenerTodas() {
        List<Sucursal> sucursales = Arrays.asList(sucursal);
        when(sucursalService.obtenerTodas()).thenReturn(sucursales);

        ResponseEntity<List<Sucursal>> response = sucursalController.obtenerTodas();

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().size());
        assertEquals("Sucursal Centro", response.getBody().get(0).getNombre());
    }

    @Test
    void testObtenerPorId() {
        when(sucursalService.obtenerPorId(1L)).thenReturn(Optional.of(sucursal));

        ResponseEntity<Sucursal> response = sucursalController.obtenerPorId(1L);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Sucursal Centro", response.getBody().getNombre());
    }

    @Test
    void testObtenerPorIdNoEncontrado() {
        when(sucursalService.obtenerPorId(1L)).thenReturn(Optional.empty());

        ResponseEntity<Sucursal> response = sucursalController.obtenerPorId(1L);

        assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    void testGuardar() {
        when(sucursalService.guardar(any(Sucursal.class))).thenReturn(sucursal);

        ResponseEntity<Sucursal> response = sucursalController.guardar(sucursal);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Sucursal Centro", response.getBody().getNombre());
    }

    @Test
    void testEliminar() {
        doNothing().when(sucursalService).eliminar(1L);

        ResponseEntity<Void> response = sucursalController.eliminar(1L);

        assertEquals(204, response.getStatusCodeValue());
        verify(sucursalService, times(1)).eliminar(1L);
    }
}