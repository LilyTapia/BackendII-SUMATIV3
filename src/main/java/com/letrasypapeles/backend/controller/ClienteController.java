package com.letrasypapeles.backend.controller;

import com.letrasypapeles.backend.assembler.ClienteModelAssembler;
import com.letrasypapeles.backend.entity.Cliente;
import com.letrasypapeles.backend.service.ClienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/clientes")
@Tag(name = "Clientes", description = "Operaciones para gestionar clientes")
public class ClienteController {

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private ClienteModelAssembler clienteModelAssembler;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Obtiene todos los clientes", description = "Devuelve la lista completa de clientes registrados")
    @ApiResponse(responseCode = "200", description = "Clientes recuperados exitosamente")
    public ResponseEntity<CollectionModel<EntityModel<Cliente>>> obtenerTodos() {
        List<Cliente> clientes = clienteService.obtenerTodos();
        List<EntityModel<Cliente>> clientesModel = clientes.stream()
                .map(clienteModelAssembler::toModel)
                .collect(Collectors.toList());

        CollectionModel<EntityModel<Cliente>> collectionModel = CollectionModel.of(clientesModel);
        collectionModel.add(linkTo(ClienteController.class).withSelfRel());

        return ResponseEntity.ok(collectionModel);
    }

    @GetMapping("/perfil")
    @PreAuthorize("hasRole('CLIENTE')")
    @Operation(summary = "Obtiene el perfil del cliente autenticado", description = "Devuelve los datos del cliente actualmente autenticado")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Perfil recuperado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Cliente no encontrado"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<Cliente> obtenerPerfil() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();

            return clienteService.obtenerPorEmail(email)
                    .map(cliente -> {
                        cliente.setContraseña(null); // No exponer la contraseña
                        return ResponseEntity.ok(cliente);
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtiene un cliente por ID", description = "Devuelve los datos del cliente con el ID especificado")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Cliente recuperado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Cliente no encontrado")
    })
    public ResponseEntity<EntityModel<Cliente>> obtenerPorId(@PathVariable Long id) {
        return clienteService.obtenerPorId(id)
                .map(cliente -> {
                    cliente.setContraseña(null); // No exponer la contraseña
                    EntityModel<Cliente> clienteModel = clienteModelAssembler.toModel(cliente);
                    return ResponseEntity.ok(clienteModel);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/registro")
    @Operation(summary = "Registra un nuevo cliente", description = "Crea un nuevo cliente en el sistema")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Cliente registrado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Solicitud inválida")
    })
    public ResponseEntity<EntityModel<Cliente>> registrarCliente(@RequestBody Cliente cliente) {
        Cliente nuevoCliente = clienteService.registrarCliente(cliente);
        nuevoCliente.setContraseña(null); // No exponer la contraseña
        EntityModel<Cliente> clienteModel = clienteModelAssembler.toModel(nuevoCliente);
        return ResponseEntity.ok(clienteModel);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualiza un cliente existente", description = "Actualiza los datos del cliente con el ID especificado")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Cliente actualizado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Cliente no encontrado")
    })
    public ResponseEntity<EntityModel<Cliente>> actualizarCliente(@PathVariable Long id, @RequestBody Cliente cliente) {
        return clienteService.obtenerPorId(id)
                .map(c -> {
                    cliente.setId(id);
                    Cliente clienteActualizado = clienteService.actualizarCliente(cliente);
                    clienteActualizado.setContraseña(null);
                    EntityModel<Cliente> clienteModel = clienteModelAssembler.toModel(clienteActualizado);
                    return ResponseEntity.ok(clienteModel);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Elimina un cliente", description = "Elimina el cliente con el ID especificado del sistema")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Cliente eliminado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Cliente no encontrado")
    })
    public ResponseEntity<Void> eliminarCliente(@PathVariable Long id) {
        return clienteService.obtenerPorId(id)
                .map(c -> {
                    clienteService.eliminar(id);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
