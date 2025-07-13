package com.letrasypapeles.backend.service;

import com.letrasypapeles.backend.entity.Cliente;
import com.letrasypapeles.backend.entity.Notificacion;
import com.letrasypapeles.backend.repository.NotificacionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotificacionServiceTest {

    @Mock
    private NotificacionRepository notificacionRepository;

    @InjectMocks
    private NotificacionService notificacionService;

    private Notificacion notificacion;
    private Cliente cliente;
    private LocalDateTime fecha;

    @BeforeEach
    void setUp() {
        // Configurar datos de prueba
        fecha = LocalDateTime.now();
        
        cliente = Cliente.builder()
                .id(1L)
                .nombre("Test")
                .apellido("Usuario")
                .email("test@example.com")
                .build();

        notificacion = Notificacion.builder()
                .id(1L)
                .mensaje("Notificación de prueba")
                .fecha(fecha)
                .cliente(cliente)
                .build();
    }

    @Test
    void obtenerTodas() {
        // Given
        List<Notificacion> notificaciones = Arrays.asList(notificacion);
        when(notificacionRepository.findAll()).thenReturn(notificaciones);

        // When
        List<Notificacion> result = notificacionService.obtenerTodas();

        // Then
        assertEquals(1, result.size());
        assertEquals("Notificación de prueba", result.get(0).getMensaje());
        verify(notificacionRepository, times(1)).findAll();
    }

    @Test
    void obtenerPorId() {
        // Given
        when(notificacionRepository.findById(1L)).thenReturn(Optional.of(notificacion));

        // When
        Optional<Notificacion> result = notificacionService.obtenerPorId(1L);

        // Then
        assertTrue(result.isPresent());
        assertEquals("Notificación de prueba", result.get().getMensaje());
        assertEquals(cliente.getId(), result.get().getCliente().getId());
        verify(notificacionRepository, times(1)).findById(1L);
    }

    @Test
    void obtenerPorIdNoExistente() {
        // Given
        when(notificacionRepository.findById(99L)).thenReturn(Optional.empty());

        // When
        Optional<Notificacion> result = notificacionService.obtenerPorId(99L);

        // Then
        assertFalse(result.isPresent());
        verify(notificacionRepository, times(1)).findById(99L);
    }

    @Test
    void guardar() {
        // Given
        Notificacion notificacionNueva = Notificacion.builder()
                .mensaje("Nueva notificación")
                .fecha(fecha)
                .cliente(cliente)
                .build();

        Notificacion notificacionGuardada = Notificacion.builder()
                .id(2L)
                .mensaje("Nueva notificación")
                .fecha(fecha)
                .cliente(cliente)
                .build();
        
        when(notificacionRepository.save(any(Notificacion.class))).thenReturn(notificacionGuardada);

        // When
        Notificacion result = notificacionService.guardar(notificacionNueva);

        // Then
        assertNotNull(result);
        assertEquals(2L, result.getId());
        assertEquals("Nueva notificación", result.getMensaje());
        verify(notificacionRepository, times(1)).save(any(Notificacion.class));
    }

    @Test
    void eliminar() {
        // Given
        Long idToDelete = 1L;
        doNothing().when(notificacionRepository).deleteById(idToDelete);

        // When
        notificacionService.eliminar(idToDelete);

        // Then
        verify(notificacionRepository, times(1)).deleteById(idToDelete);
    }

    @Test
    void obtenerPorClienteId() {
        // Given
        List<Notificacion> notificaciones = Arrays.asList(notificacion);
        when(notificacionRepository.findByClienteId(1L)).thenReturn(notificaciones);

        // When
        List<Notificacion> result = notificacionService.obtenerPorClienteId(1L);

        // Then
        assertEquals(1, result.size());
        assertEquals("Notificación de prueba", result.get(0).getMensaje());
        assertEquals(1L, result.get(0).getCliente().getId());
        verify(notificacionRepository, times(1)).findByClienteId(1L);
    }

    @Test
    void obtenerPorFechaEntre() {
        // Given
        LocalDateTime inicio = fecha.minusDays(1);
        LocalDateTime fin = fecha.plusDays(1);
        
        List<Notificacion> notificaciones = Arrays.asList(notificacion);
        when(notificacionRepository.findByFechaBetween(inicio, fin)).thenReturn(notificaciones);

        // When
        List<Notificacion> result = notificacionService.obtenerPorFechaEntre(inicio, fin);

        // Then
        assertEquals(1, result.size());
        assertEquals("Notificación de prueba", result.get(0).getMensaje());
        verify(notificacionRepository, times(1)).findByFechaBetween(inicio, fin);
    }
}
