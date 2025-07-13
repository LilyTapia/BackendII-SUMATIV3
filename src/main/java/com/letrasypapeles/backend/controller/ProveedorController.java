package com.letrasypapeles.backend.controller;

import com.letrasypapeles.backend.entity.Proveedor;
import com.letrasypapeles.backend.service.ProveedorService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/proveedores")
@Tag(name = "Proveedores", description = "Operaciones para gestionar proveedores")
public class ProveedorController {

    @Autowired
    private ProveedorService proveedorService;

    @GetMapping
    @Operation(summary = "Obtiene todos los proveedores", description = "Devuelve la lista completa de proveedores")
    @ApiResponse(responseCode = "200", description = "Proveedores recuperados exitosamente")
    public ResponseEntity<List<Proveedor>> obtenerTodos() {
        List<Proveedor> proveedores = proveedorService.obtenerTodos();
        return ResponseEntity.ok(proveedores);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtiene un proveedor por ID", description = "Devuelve los datos del proveedor con el ID especificado")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Proveedor recuperado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Proveedor no encontrado")
    })
    public ResponseEntity<Proveedor> obtenerPorId(@PathVariable Long id) {
        return proveedorService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Crea un nuevo proveedor", description = "Guarda un nuevo proveedor en el sistema")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Proveedor creado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Solicitud inválida")
    })
    public ResponseEntity<Proveedor> crearProveedor(@RequestBody Proveedor proveedor) {
        Proveedor nuevoProveedor = proveedorService.guardar(proveedor);
        return ResponseEntity.ok(nuevoProveedor);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualiza un proveedor existente", description = "Modifica los datos del proveedor con el ID especificado")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Proveedor actualizado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Proveedor no encontrado")
    })
    public ResponseEntity<Proveedor> actualizarProveedor(@PathVariable Long id, @RequestBody Proveedor proveedor) {
        return proveedorService.obtenerPorId(id)
                .map(p -> {
                    proveedor.setId(id);
                    Proveedor actualizado = proveedorService.guardar(proveedor);
                    return ResponseEntity.ok(actualizado);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Elimina un proveedor", description = "Elimina el proveedor con el ID especificado del sistema")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Proveedor eliminado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Proveedor no encontrado")
    })
    public ResponseEntity<Void> eliminarProveedor(@PathVariable Long id) {
        return proveedorService.obtenerPorId(id)
                .map(p -> {
                    proveedorService.eliminar(id);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
