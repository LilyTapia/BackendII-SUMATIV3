package com.letrasypapeles.backend.controller;

import com.letrasypapeles.backend.dto.MessageResponse;
import com.letrasypapeles.backend.entity.Inventario;
import com.letrasypapeles.backend.entity.Producto;
import com.letrasypapeles.backend.service.InventarioService;
import com.letrasypapeles.backend.service.ProductoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@Tag(name = "Inventarios", description = "Operaciones para gestionar inventarios ")
public class InventarioController {

    @Autowired
    private InventarioService inventarioService;

    @Autowired
    private ProductoService productoService;

    @GetMapping("/api/inventarios")
    @Operation(summary = "Obtiene todos los inventarios", description = "Devuelve la lista completa de registros de inventario")
    @ApiResponse(responseCode = "200", description = "Inventarios recuperados exitosamente")
    public ResponseEntity<List<Inventario>> obtenerTodos() {
        List<Inventario> inventarios = inventarioService.obtenerTodos();
        return ResponseEntity.ok(inventarios);
    }

    @GetMapping("/api/inventarios/{id}")
    @Operation(summary = "Obtiene un inventario por ID", description = "Devuelve el registro de inventario asociado al ID proporcionado")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Inventario recuperado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Inventario no encontrado")
    })
    public ResponseEntity<Inventario> obtenerPorId(@PathVariable Long id) {
        return inventarioService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/api/inventarios/producto/{productoId}")
    @Operation(summary = "Filtra inventarios por ID de producto", description = "Devuelve todos los inventarios asociados a un producto específico")
    @ApiResponse(responseCode = "200", description = "Inventarios recuperados exitosamente")
    public ResponseEntity<List<Inventario>> obtenerPorProductoId(@PathVariable Long productoId) {
        List<Inventario> inventarios = inventarioService.obtenerPorProductoId(productoId);
        return ResponseEntity.ok(inventarios);
    }

    @GetMapping("/api/inventarios/sucursal/{sucursalId}")
    @Operation(summary = "Filtra inventarios por ID de sucursal", description = "Devuelve todos los inventarios asociados a una sucursal específica")
    @ApiResponse(responseCode = "200", description = "Inventarios recuperados exitosamente")
    public ResponseEntity<List<Inventario>> obtenerPorSucursalId(@PathVariable Long sucursalId) {
        List<Inventario> inventarios = inventarioService.obtenerPorSucursalId(sucursalId);
        return ResponseEntity.ok(inventarios);
    }

    @PostMapping("/api/inventarios")
    @Operation(summary = "Crea un nuevo inventario", description = "Guarda un nuevo registro de inventario en el sistema")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Inventario creado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Solicitud inválida")
    })
    public ResponseEntity<Inventario> crearInventario(@RequestBody Inventario inventario) {
        Inventario nuevoInventario = inventarioService.guardar(inventario);
        return ResponseEntity.ok(nuevoInventario);
    }

    @PutMapping("/api/inventarios/{id}")
    @Operation(summary = "Actualiza un inventario existente", description = "Modifica un registro de inventario basado en el ID proporcionado")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Inventario actualizado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Inventario no encontrado")
    })
    public ResponseEntity<Inventario> actualizarInventario(@PathVariable Long id, @RequestBody Inventario inventario) {
        return inventarioService.obtenerPorId(id)
                .map(i -> {
                    inventario.setId(id);
                    Inventario actualizado = inventarioService.guardar(inventario);
                    return ResponseEntity.ok(actualizado);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/api/inventarios/{id}")
    @Operation(summary = "Elimina un inventario", description = "Elimina el registro de inventario con el ID especificado")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Inventario eliminado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Inventario no encontrado")
    })
    public ResponseEntity<Void> eliminarInventario(@PathVariable Long id) {
        return inventarioService.obtenerPorId(id)
                .map(i -> {
                    inventarioService.eliminar(id);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Endpoints singulares para compatibilidad con tests

    @PostMapping("/api/inventario")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crea inventario (singular)", description = "Guarda inventario usando formato singular para compatibilidad con tests")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Inventario creado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Producto no encontrado"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<?> crearInventarioSingular(@RequestBody Map<String, Object> request) {
        try {
            Long productoId = Long.valueOf(request.get("productoId").toString());
            Integer cantidad = Integer.valueOf(request.get("cantidad").toString());

            Producto producto = productoService.obtenerPorId(productoId).orElse(null);
            if (producto == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new MessageResponse("Producto no encontrado"));
            }

            Inventario inventario = new Inventario();
            inventario.setProducto(producto);
            inventario.setCantidad(cantidad);

            Inventario nuevo = inventarioService.guardar(inventario);
            return ResponseEntity.ok(nuevo);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error al crear inventario: " + e.getMessage()));
        }
    }

    @PutMapping("/api/inventario/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
    @Operation(summary = "Actualiza inventario (singular)", description = "Modifica cantidad de inventario usando formato singular para compatibilidad con tests")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Inventario actualizado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Inventario no encontrado"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<?> actualizarInventarioSingular(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        try {
            Integer cantidad = Integer.valueOf(request.get("cantidad").toString());

            return inventarioService.obtenerPorId(id)
                    .map(inv -> {
                        inv.setCantidad(cantidad);
                        Inventario actualizado = inventarioService.guardar(inv);
                        return ResponseEntity.ok(actualizado);
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error al actualizar inventario: " + e.getMessage()));
        }
    }

    @GetMapping("/api/inventario/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
    @Operation(summary = "Obtiene inventario (singular)", description = "Recupera inventario usando formato singular para compatibilidad con tests")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Inventario recuperado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Inventario no encontrado"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<?> obtenerInventarioSingular(@PathVariable Long id) {
        try {
            return inventarioService.obtenerPorId(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error al obtener inventario: " + e.getMessage()));
        }
    }
}
