package com.letrasypapeles.backend.controller;

import com.letrasypapeles.backend.dto.MessageResponse;
import com.letrasypapeles.backend.entity.Cliente;
import com.letrasypapeles.backend.entity.Role;
import com.letrasypapeles.backend.service.ClienteService;
import com.letrasypapeles.backend.service.RoleService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Administración", description = "Operaciones administrativas de usuarios y roles")
public class AdminController {

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private RoleService roleService;

    @GetMapping("/usuarios")
    @Operation(summary = "Obtiene todos los usuarios", description = "Devuelve la lista completa de clientes registrados en el sistema")
    @ApiResponse(responseCode = "200", description = "Usuarios recuperados exitosamente")
    public ResponseEntity<List<Cliente>> obtenerTodosLosUsuarios() {
        try {
            List<Cliente> usuarios = clienteService.obtenerTodosLosClientes();
            return ResponseEntity.ok(usuarios);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/usuarios")
    @Operation(summary = "Crea un nuevo usuario", description = "Registra un nuevo cliente en el sistema")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Usuario creado exitosamente"),
        @ApiResponse(responseCode = "500", description = "Error interno al crear usuario")
    })
    public ResponseEntity<?> crearUsuario(@RequestBody Cliente usuario) {
        try {
            Cliente nuevoUsuario = clienteService.registrarCliente(usuario);
            return ResponseEntity.ok(nuevoUsuario);
        } catch (Exception e) {
            MessageResponse response = new MessageResponse();
            response.setMessage("Error al crear usuario: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @PutMapping("/usuarios/{id}/roles")
    @Operation(summary = "Actualiza roles de usuario", description = "Modifica el conjunto de roles asignados a un cliente existente")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Roles actualizados exitosamente"),
        @ApiResponse(responseCode = "400", description = "Solicitud inválida: lista de roles vacía"),
        @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
        @ApiResponse(responseCode = "500", description = "Error interno al actualizar roles")
    })
    public ResponseEntity<?> actualizarRolesUsuario(
            @PathVariable Long id,
            @RequestBody Map<String, List<String>> request) {
        try {
            List<String> roleNames = request.get("roles");
            if (roleNames == null || roleNames.isEmpty()) {
                MessageResponse response = new MessageResponse();
                response.setMessage("Lista de roles no puede estar vacía");
                return ResponseEntity.badRequest().body(response);
            }

            Cliente cliente = clienteService.obtenerClientePorId(id);
            if (cliente == null) {
                MessageResponse response = new MessageResponse();
                response.setMessage("Usuario no encontrado");
                return ResponseEntity.notFound().build();
            }

            Set<Role> roles = roleNames.stream()
                    .map(roleService::obtenerRolePorNombre)
                    .filter(role -> role != null)
                    .collect(Collectors.toSet());

            cliente.setRoles(roles);
            Cliente clienteActualizado = clienteService.actualizarCliente(cliente);
            return ResponseEntity.ok(clienteActualizado);

        } catch (Exception e) {
            MessageResponse response = new MessageResponse();
            response.setMessage("Error al actualizar roles: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}