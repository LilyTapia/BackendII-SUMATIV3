package com.letrasypapeles.backend.controller;

import com.letrasypapeles.backend.assembler.ReservaModelAssembler;
import com.letrasypapeles.backend.dto.ReservaRequest;
import com.letrasypapeles.backend.entity.Reserva;
import com.letrasypapeles.backend.entity.Cliente;
import com.letrasypapeles.backend.entity.Producto;
import com.letrasypapeles.backend.entity.Inventario;
import com.letrasypapeles.backend.service.ReservaService;
import com.letrasypapeles.backend.service.ClienteService;
import com.letrasypapeles.backend.service.ProductoService;
import com.letrasypapeles.backend.service.InventarioService;
import com.letrasypapeles.backend.repository.ClienteRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/reservas")
@Tag(name = "Reservas", description = "Operaciones para gestionar reservas")
public class ReservaController {

    @Autowired
    private ReservaService reservaService;

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private ProductoService productoService;

    @Autowired
    private InventarioService inventarioService;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private ReservaModelAssembler reservaModelAssembler;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('CLIENTE') or hasRole('VENDEDOR')")
    @Operation(summary = "Obtiene todas las reservas", description = "Devuelve la lista completa de reservas")
    @ApiResponse(responseCode = "200", description = "Reservas recuperadas exitosamente")
    public ResponseEntity<CollectionModel<EntityModel<Reserva>>> obtenerTodas() {
        List<Reserva> reservas = reservaService.obtenerTodas();
        List<EntityModel<Reserva>> reservasModel = reservas.stream()
                .map(reservaModelAssembler::toModel)
                .collect(Collectors.toList());

        CollectionModel<EntityModel<Reserva>> collectionModel = CollectionModel.of(reservasModel);
        collectionModel.add(linkTo(ReservaController.class).withSelfRel());

        return ResponseEntity.ok(collectionModel);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtiene una reserva por ID", description = "Devuelve los datos de la reserva con el ID especificado")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Reserva recuperada exitosamente"),
        @ApiResponse(responseCode = "404", description = "Reserva no encontrada")
    })
    public ResponseEntity<EntityModel<Reserva>> obtenerPorId(@PathVariable Long id) {
        return reservaService.obtenerPorId(id)
                .map(reservaModelAssembler::toModel)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/cliente/{clienteId}")
    @Operation(summary = "Obtiene reservas de un cliente", description = "Devuelve todas las reservas asociadas al cliente especificado")
    @ApiResponse(responseCode = "200", description = "Reservas del cliente recuperadas exitosamente")
    public ResponseEntity<List<Reserva>> obtenerPorClienteId(@PathVariable Long clienteId) {
        List<Reserva> reservas = reservaService.obtenerPorClienteId(clienteId);
        return ResponseEntity.ok(reservas);
    }

    @PostMapping
    @Operation(summary = "Crea una nueva reserva", description = "Procesa y guarda una nueva reserva con validaciones de stock y reducción de inventario")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Reserva creada exitosamente"),
        @ApiResponse(responseCode = "400", description = "Solicitud inválida o stock insuficiente"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<?> crearReserva(
            @Parameter(description = "Cuerpo de la solicitud con clienteId, productoId, cantidad y opcional fechaReserva", required = true)
            @RequestBody Map<String, Object> requestBody) {
        try {
            Reserva reserva = new Reserva();

            // Manejo de cliente
            Long clienteId = null;
            if (requestBody.containsKey("clienteId")) {
                clienteId = Long.valueOf(requestBody.get("clienteId").toString());
            } else if (requestBody.containsKey("cliente") && requestBody.get("cliente") instanceof Map) {
                Map<String, Object> clienteMap = (Map<String, Object>) requestBody.get("cliente");
                if (clienteMap.containsKey("id")) {
                    clienteId = Long.valueOf(clienteMap.get("id").toString());
                }
            }
            if (clienteId != null) {
                Cliente cliente = clienteService.obtenerPorId(clienteId)
                    .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
                reserva.setCliente(cliente);
            }

            // Manejo de producto y stock
            Long productoId = null;
            if (requestBody.containsKey("productoId")) {
                productoId = Long.valueOf(requestBody.get("productoId").toString());
            } else if (requestBody.containsKey("producto") && requestBody.get("producto") instanceof Map) {
                Map<String, Object> productoMap = (Map<String, Object>) requestBody.get("producto");
                if (productoMap.containsKey("id")) {
                    productoId = Long.valueOf(productoMap.get("id").toString());
                }
            }
            if (productoId != null) {
                Producto producto = productoService.obtenerPorId(productoId)
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

                Integer cantidad = Integer.valueOf(requestBody.get("cantidad").toString());
                if (producto.getStock() == null || producto.getStock() < cantidad) {
                    return ResponseEntity.badRequest()
                        .body(Map.of("error", "Stock insuficiente"));
                }
                producto.setStock(producto.getStock() - cantidad);
                productoService.guardar(producto);

                // Ajuste de inventario
                List<Inventario> inventarios = inventarioService.obtenerPorProductoId(productoId);
                if (!inventarios.isEmpty()) {
                    Inventario inventario = inventarios.get(0);
                    if (inventario.getCantidad() != null && inventario.getCantidad() >= cantidad) {
                        inventario.setCantidad(inventario.getCantidad() - cantidad);
                        inventarioService.guardar(inventario);
                    }
                }

                reserva.setProducto(producto);
                reserva.setCantidad(cantidad);
            }

            // Valores por defecto y fecha
            reserva.setEstado("PENDIENTE");
            if (requestBody.containsKey("fechaReserva")) {
                reserva.setFechaReserva(LocalDateTime.parse(requestBody.get("fechaReserva").toString()));
            } else {
                reserva.setFechaReserva(LocalDateTime.now());
            }

            Reserva nuevaReserva = reservaService.guardar(reserva);
            return ResponseEntity.ok(nuevaReserva);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/crear")
    @Operation(summary = "Crear una nueva reserva con IDs", description = "Crea una nueva reserva usando IDs de cliente y producto.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Reserva creada exitosamente"),
        @ApiResponse(responseCode = "400", description = "Solicitud inválida para crear la reserva"),
        @ApiResponse(responseCode = "404", description = "Cliente o producto no encontrado")
    })
    public ResponseEntity<EntityModel<Reserva>> crearReservaConIds(@RequestBody ReservaRequest reservaRequest) {
        try {
            Reserva nuevaReserva = reservaService.crearDesdeReservaRequest(reservaRequest);
            EntityModel<Reserva> reservaModel = reservaModelAssembler.toModel(nuevaReserva);
            return ResponseEntity.status(201).body(reservaModel);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualiza una reserva existente", description = "Modifica los datos de la reserva con el ID especificado")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Reserva actualizada exitosamente"),
        @ApiResponse(responseCode = "404", description = "Reserva no encontrada")
    })
    public ResponseEntity<Reserva> actualizarReserva(@PathVariable Long id, @RequestBody Reserva reserva) {
        return reservaService.obtenerPorId(id)
                .map(r -> {
                    reserva.setId(id);
                    Reserva reservaActualizada = reservaService.guardar(reserva);
                    return ResponseEntity.ok(reservaActualizada);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Elimina una reserva", description = "Elimina la reserva con el ID especificado del sistema")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Reserva eliminada exitosamente"),
        @ApiResponse(responseCode = "404", description = "Reserva no encontrada")
    })
    public ResponseEntity<Void> eliminarReserva(@PathVariable Long id) {
        return reservaService.obtenerPorId(id)
                .map(r -> {
                    reservaService.eliminar(id);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/confirmar")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CLIENTE') or hasRole('VENDEDOR')")
    @Operation(summary = "Confirma una reserva", description = "Cambia el estado de la reserva a CONFIRMADA y otorga puntos de fidelidad")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Reserva confirmada exitosamente"),
        @ApiResponse(responseCode = "404", description = "Reserva no encontrada"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<?> confirmarReserva(@PathVariable Long id) {
        try {
            Optional<Reserva> reservaOpt = reservaService.obtenerPorIdConRelaciones(id);
            if (reservaOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            Reserva reserva = reservaOpt.get();
            reserva.setEstado("CONFIRMADA");
            reservaService.guardar(reserva);

            if (reserva.getCliente() != null && reserva.getProducto() != null) {
                if (reserva.getCantidad() == null) {
                    throw new RuntimeException("Cantidad is null");
                }
                if (reserva.getProducto().getPrecio() == null) {
                    throw new RuntimeException("Precio is null");
                }
                double totalCompra = reserva.getCantidad() * reserva.getProducto().getPrecio().doubleValue();
                int puntosGanados = (int) (totalCompra / 10);
                clienteService.actualizarPuntosFidelidadDirecto(reserva.getCliente().getId(), puntosGanados);
                Integer puntosActualizados = clienteRepository.obtenerPuntosFidelidad(reserva.getCliente().getId());
                if (puntosActualizados != null) {
                    reserva.getCliente().setPuntosFidelidad(puntosActualizados);
                }
            }
            return ResponseEntity.ok(reserva);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Unknown error: " + e.getClass().getSimpleName()));
        }
    }

    @PutMapping("/{id}/cancelar")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CLIENTE') or hasRole('VENDEDOR')")
    @Operation(summary = "Cancela una reserva", description = "Cambia el estado de la reserva a CANCELADA y restaura stock e inventario")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Reserva cancelada exitosamente"),
        @ApiResponse(responseCode = "400", description = "Operación inválida: solo reservas PENDIENTES pueden ser canceladas"),
        @ApiResponse(responseCode = "404", description = "Reserva no encontrada"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<?> cancelarReserva(@PathVariable Long id) {
        try {
            Optional<Reserva> reservaOpt = reservaService.obtenerPorIdConRelaciones(id);
            if (reservaOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            Reserva reserva = reservaOpt.get();
            if (!"PENDIENTE".equals(reserva.getEstado())) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Solo se pueden cancelar reservas en estado PENDIENTE"));
            }
            Producto producto = reserva.getProducto();
            if (producto != null && reserva.getCantidad() != null) {
                producto.setStock(producto.getStock() + reserva.getCantidad());
                productoService.guardar(producto);
                List<Inventario> inventarios = inventarioService.obtenerPorProductoId(producto.getId());
                if (!inventarios.isEmpty()) {
                    Inventario inventario = inventarios.get(0);
                    if (inventario.getCantidad() != null) {
                        inventario.setCantidad(inventario.getCantidad() + reserva.getCantidad());
                        inventarioService.guardar(inventario);
                    }
                }
            }
            reserva.setEstado("CANCELADA");
            reservaService.guardar(reserva);
            return ResponseEntity.ok(reserva);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Unknown error: " + e.getClass().getSimpleName()));
        }
    }
}
