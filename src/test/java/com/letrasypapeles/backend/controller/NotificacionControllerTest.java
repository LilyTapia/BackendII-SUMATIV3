package com.letrasypapeles.backend.controller;

import com.letrasypapeles.backend.entity.Notificacion;
import com.letrasypapeles.backend.service.NotificacionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class NotificacionControllerTest {

    @Mock
    private NotificacionService notificacionService;

    @InjectMocks
    private NotificacionController notificacionController;

    private Notificacion notificacion;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        notificacion = new Notificacion();
        notificacion.setId(1L);
        notificacion.setMensaje("Test mensaje");
        notificacion.setFecha(LocalDateTime.now());
    }

    @Test
    void testObtenerTodas() {
        List<Notificacion> notificaciones = Arrays.asList(notificacion);
        when(notificacionService.obtenerTodas()).thenReturn(notificaciones);

        ResponseEntity<List<Notificacion>> response = notificacionController.obtenerTodas();

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testObtenerPorId() {
        when(notificacionService.obtenerPorId(1L)).thenReturn(Optional.of(notificacion));

        ResponseEntity<Notificacion> response = notificacionController.obtenerPorId(1L);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Test mensaje", response.getBody().getMensaje());
    }

    @Test
    void testObtenerPorIdNoEncontrado() {
        when(notificacionService.obtenerPorId(1L)).thenReturn(Optional.empty());

        ResponseEntity<Notificacion> response = notificacionController.obtenerPorId(1L);

        assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    void testObtenerPorClienteId() {
        List<Notificacion> notificaciones = Arrays.asList(notificacion);
        when(notificacionService.obtenerPorClienteId(1L)).thenReturn(notificaciones);

        ResponseEntity<List<Notificacion>> response = notificacionController.obtenerPorClienteId(1L);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testCrearNotificacion() {
        when(notificacionService.guardar(any(Notificacion.class))).thenReturn(notificacion);

        ResponseEntity<Notificacion> response = notificacionController.crearNotificacion(notificacion);

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody().getFecha());
    }

    @Test
    void testEliminarNotificacion() {
        when(notificacionService.obtenerPorId(1L)).thenReturn(Optional.of(notificacion));
        doNothing().when(notificacionService).eliminar(1L);

        ResponseEntity<Void> response = notificacionController.eliminarNotificacion(1L);

        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void testEliminarNotificacionNoEncontrada() {
        when(notificacionService.obtenerPorId(1L)).thenReturn(Optional.empty());

        ResponseEntity<Void> response = notificacionController.eliminarNotificacion(1L);

        assertEquals(404, response.getStatusCodeValue());
    }
}