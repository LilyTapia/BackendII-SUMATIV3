package com.letrasypapeles.backend.controller;

import com.letrasypapeles.backend.entity.Pedido;
import com.letrasypapeles.backend.service.PedidoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    @Autowired
    private PedidoService pedidoService;

    @Operation(summary = "Obtener todos los pedidos", description = "Retorna la lista de todos los pedidos registrados en el sistema.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de pedidos obtenida exitosamente")
    })
    @GetMapping
    public ResponseEntity<List<Pedido>> obtenerTodos() {
        List<Pedido> pedidos = pedidoService.obtenerTodos();
        return ResponseEntity.ok(pedidos);
    }

    @Operation(summary = "Obtener pedido por ID", description = "Retorna un pedido específico dado su ID.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Pedido encontrado y retornado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Pedido no encontrado con el ID proporcionado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Pedido> obtenerPorId(@PathVariable Long id) {
        return pedidoService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Obtener pedidos por cliente", description = "Retorna la lista de pedidos asociados a un cliente dado su ID.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de pedidos del cliente obtenida exitosamente")
    })
    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<Pedido>> obtenerPorClienteId(@PathVariable Long clienteId) {
        List<Pedido> pedidos = pedidoService.obtenerPorClienteId(clienteId);
        return ResponseEntity.ok(pedidos);
    }

    @Operation(summary = "Crear un nuevo pedido", description = "Crea un nuevo pedido en el sistema con los datos proporcionados.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Pedido creado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Solicitud inválida para crear el pedido")
    })
    @PostMapping
    public ResponseEntity<Pedido> crearPedido(@RequestBody Pedido pedido) {
        Pedido nuevoPedido = pedidoService.guardar(pedido);
        return ResponseEntity.status(201).body(nuevoPedido);
    }

    @Operation(summary = "Actualizar pedido existente", description = "Actualiza un pedido existente dado su ID con los datos proporcionados.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Pedido actualizado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Pedido no encontrado con el ID proporcionado")
    })
    @PutMapping("/{id}")
    public ResponseEntity<Pedido> actualizarPedido(@PathVariable Long id, @RequestBody Pedido pedido) {
        return pedidoService.obtenerPorId(id)
                .map(p -> {
                    pedido.setId(id);
                    Pedido pedidoActualizado = pedidoService.guardar(pedido);
                    return ResponseEntity.ok(pedidoActualizado);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Eliminar un pedido", description = "Elimina un pedido existente dado su ID.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Pedido eliminado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Pedido no encontrado con el ID proporcionado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarPedido(@PathVariable Long id) {
        return pedidoService.obtenerPorId(id)
                .map(p -> {
                    pedidoService.eliminar(id);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
