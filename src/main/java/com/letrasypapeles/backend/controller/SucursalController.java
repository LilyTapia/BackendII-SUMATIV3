package com.letrasypapeles.backend.controller;

import com.letrasypapeles.backend.entity.Sucursal;
import com.letrasypapeles.backend.service.SucursalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/sucursales")
public class SucursalController {

    private final SucursalService sucursalService;

    public SucursalController(SucursalService sucursalService) {
        this.sucursalService = sucursalService;
    }

    @Operation(summary = "Obtener todas las sucursales")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de sucursales obtenida exitosamente")
    })
    @GetMapping
    public ResponseEntity<List<Sucursal>> obtenerTodas() {
        return ResponseEntity.ok(sucursalService.obtenerTodas());
    }

    @Operation(summary = "Obtener una sucursal por ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Sucursal encontrada"),
        @ApiResponse(responseCode = "404", description = "Sucursal no encontrada")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Sucursal> obtenerPorId(
            @Parameter(description = "ID de la sucursal") @PathVariable Long id) {
        Optional<Sucursal> sucursal = sucursalService.obtenerPorId(id);
        return sucursal.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Guardar una nueva sucursal")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Sucursal guardada exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content)
    })
    @PostMapping
    public ResponseEntity<Sucursal> guardar(
            @RequestBody(
                description = "Datos de la nueva sucursal",
                required = true,
                content = @Content(schema = @Schema(implementation = Sucursal.class))
            )
            @org.springframework.web.bind.annotation.RequestBody Sucursal sucursal) {
        return ResponseEntity.ok(sucursalService.guardar(sucursal));
    }

    @Operation(summary = "Eliminar una sucursal por ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Sucursal eliminada correctamente"),
        @ApiResponse(responseCode = "404", description = "Sucursal no encontrada")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @Parameter(description = "ID de la sucursal a eliminar") @PathVariable Long id) {
        sucursalService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
