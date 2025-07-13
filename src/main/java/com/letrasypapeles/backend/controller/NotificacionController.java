package com.letrasypapeles.backend.controller;

import com.letrasypapeles.backend.entity.Notificacion;
import com.letrasypapeles.backend.service.NotificacionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/notificaciones")
@Tag(name = "Notificaciones", description = "Operaciones para gestionar notificaciones de clientes")
public class NotificacionController {

    @Autowired
    private NotificacionService notificacionService;

    @GetMapping
    @Operation(summary = "Obtiene todas las notificaciones", description = "Devuelve la lista completa de notificaciones registradas")
    @ApiResponse(responseCode = "200", description = "Notificaciones recuperadas exitosamente")
    public ResponseEntity<List<Notificacion>> obtenerTodas() {
        List<Notificacion> notificaciones = notificacionService.obtenerTodas();
        return ResponseEntity.ok(notificaciones);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtiene una notificación por ID", description = "Devuelve la notificación asociada al ID proporcionado")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Notificación recuperada exitosamente"),
        @ApiResponse(responseCode = "404", description = "Notificación no encontrada")
    })
    public ResponseEntity<Notificacion> obtenerPorId(@PathVariable Long id) {
        return notificacionService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/cliente/{clienteId}")
    @Operation(summary = "Obtiene notificaciones por cliente", description = "Devuelve las notificaciones asociadas al cliente indicado")
    @ApiResponse(responseCode = "200", description = "Notificaciones recuperadas exitosamente")
    public ResponseEntity<List<Notificacion>> obtenerPorClienteId(@PathVariable Long clienteId) {
        List<Notificacion> notificaciones = notificacionService.obtenerPorClienteId(clienteId);
        return ResponseEntity.ok(notificaciones);
    }

    @PostMapping
    @Operation(summary = "Crea una nueva notificación", description = "Guarda una nueva notificación y asigna la fecha actual automáticamente")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Notificación creada exitosamente"),
        @ApiResponse(responseCode = "400", description = "Solicitud inválida")
    })
    public ResponseEntity<Notificacion> crearNotificacion(@RequestBody Notificacion notificacion) {
        notificacion.setFecha(LocalDateTime.now());
        Notificacion nueva = notificacionService.guardar(notificacion);
        return ResponseEntity.ok(nueva);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Elimina una notificación", description = "Elimina la notificación con el ID especificado")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Notificación eliminada exitosamente"),
        @ApiResponse(responseCode = "404", description = "Notificación no encontrada")
    })
    public ResponseEntity<Void> eliminarNotificacion(@PathVariable Long id) {
        return notificacionService.obtenerPorId(id)
                .map(n -> {
                    notificacionService.eliminar(id);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
