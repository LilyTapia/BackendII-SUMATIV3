package com.letrasypapeles.backend.controller;

import com.letrasypapeles.backend.entity.Role;
import com.letrasypapeles.backend.service.RoleService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/roles")
@Tag(name = "Roles", description = "Gestión de perfiles y permisos de usuario")
public class RoleController {

    @Autowired
    private RoleService roleService;

    @GetMapping
    @Operation(summary = "Obtiene todos los roles", description = "Recupera la lista completa de roles disponibles en el sistema")
    @ApiResponse(responseCode = "200", description = "Roles obtenidos exitosamente")
    public ResponseEntity<List<Role>> obtenerTodos() {
        List<Role> roles = roleService.obtenerTodos();
        return ResponseEntity.ok(roles);
    }

    @GetMapping("/{nombre}")
    @Operation(summary = "Obtiene un rol por nombre", description = "Busca un rol específico utilizando su nombre único")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Rol encontrado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Rol no encontrado")
    })
    public ResponseEntity<Role> obtenerPorNombre(@PathVariable String nombre) {
        return roleService.obtenerPorNombre(nombre)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Crea un nuevo rol", description = "Agrega un nuevo rol al sistema con permisos asociados")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Rol creado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos de rol inválidos")
    })
    public ResponseEntity<Role> crearRole(@RequestBody Role role) {
        Role nuevoRole = roleService.guardar(role);
        return ResponseEntity.ok(nuevoRole);
    }

    @DeleteMapping("/{nombre}")
    @Operation(summary = "Elimina un rol", description = "Borra un rol existente identificado por su nombre")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Rol eliminado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Rol no encontrado")
    })
    public ResponseEntity<Void> eliminarRole(@PathVariable String nombre) {
        return roleService.obtenerPorNombre(nombre)
                .map(r -> {
                    roleService.eliminar(nombre);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}