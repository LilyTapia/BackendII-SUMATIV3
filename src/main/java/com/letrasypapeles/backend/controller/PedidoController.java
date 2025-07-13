package com.letrasypapeles.backend.controller;

import com.letrasypapeles.backend.assembler.PedidoModelAssembler;
import com.letrasypapeles.backend.dto.PedidoRequest;
import com.letrasypapeles.backend.entity.Pedido;
import com.letrasypapeles.backend.service.PedidoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    @Autowired
    private PedidoService pedidoService;

    @Autowired
    private PedidoModelAssembler pedidoModelAssembler;

    @Operation(summary = "Obtener todos los pedidos", description = "Retorna la lista de todos los pedidos registrados en el sistema.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de pedidos obtenida exitosamente")
    })
    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<Pedido>>> obtenerTodos() {
        List<Pedido> pedidos = pedidoService.obtenerTodos();
        List<EntityModel<Pedido>> pedidosModel = pedidos.stream()
                .map(pedidoModelAssembler::toModel)
                .collect(Collectors.toList());

        CollectionModel<EntityModel<Pedido>> collectionModel = CollectionModel.of(pedidosModel);
        collectionModel.add(linkTo(PedidoController.class).withSelfRel());

        return ResponseEntity.ok(collectionModel);
    }

    @Operation(summary = "Obtener pedido por ID", description = "Retorna un pedido específico dado su ID.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Pedido encontrado y retornado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Pedido no encontrado con el ID proporcionado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<Pedido>> obtenerPorId(@PathVariable Long id) {
        return pedidoService.obtenerPorId(id)
                .map(pedidoModelAssembler::toModel)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Obtener pedidos por cliente", description = "Retorna la lista de pedidos asociados a un cliente dado su ID.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de pedidos del cliente obtenida exitosamente")
    })
    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<CollectionModel<EntityModel<Pedido>>> obtenerPorClienteId(@PathVariable Long clienteId) {
        List<Pedido> pedidos = pedidoService.obtenerPorClienteId(clienteId);
        List<EntityModel<Pedido>> pedidosModel = pedidos.stream()
                .map(pedidoModelAssembler::toModel)
                .collect(Collectors.toList());

        CollectionModel<EntityModel<Pedido>> collectionModel = CollectionModel.of(pedidosModel);
        collectionModel.add(linkTo(methodOn(PedidoController.class)
                .obtenerPorClienteId(clienteId)).withSelfRel());
        collectionModel.add(linkTo(PedidoController.class).withRel("pedidos"));

        return ResponseEntity.ok(collectionModel);
    }

    @Operation(summary = "Crear un nuevo pedido", description = "Crea un nuevo pedido en el sistema con los datos proporcionados.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Pedido creado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Solicitud inválida para crear el pedido")
    })
    @PostMapping
    public ResponseEntity<EntityModel<Pedido>> crearPedido(@RequestBody Pedido pedido) {
        Pedido nuevoPedido = pedidoService.guardar(pedido);
        EntityModel<Pedido> pedidoModel = pedidoModelAssembler.toModel(nuevoPedido);
        return ResponseEntity.status(201).body(pedidoModel);
    }

    @Operation(summary = "Crear un nuevo pedido con IDs", description = "Crea un nuevo pedido usando IDs de cliente y productos.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Pedido creado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Solicitud inválida para crear el pedido"),
        @ApiResponse(responseCode = "404", description = "Cliente o producto no encontrado")
    })
    @PostMapping("/crear")
    public ResponseEntity<EntityModel<Pedido>> crearPedidoConIds(@RequestBody PedidoRequest pedidoRequest) {
        try {
            Pedido nuevoPedido = pedidoService.crearDesdePedidoRequest(pedidoRequest);
            EntityModel<Pedido> pedidoModel = pedidoModelAssembler.toModel(nuevoPedido);
            return ResponseEntity.status(201).body(pedidoModel);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Actualizar pedido existente", description = "Actualiza un pedido existente dado su ID con los datos proporcionados.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Pedido actualizado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Pedido no encontrado con el ID proporcionado")
    })
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<Pedido>> actualizarPedido(@PathVariable Long id, @RequestBody Pedido pedido) {
        return pedidoService.obtenerPorId(id)
                .map(p -> {
                    pedido.setId(id);
                    Pedido pedidoActualizado = pedidoService.guardar(pedido);
                    EntityModel<Pedido> pedidoModel = pedidoModelAssembler.toModel(pedidoActualizado);
                    return ResponseEntity.ok(pedidoModel);
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
