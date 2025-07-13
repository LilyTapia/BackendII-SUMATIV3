package com.letrasypapeles.backend.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@RequestMapping("/api/test")
@CrossOrigin(origins = "*", maxAge = 3600)
@Tag(name = "Pruebas", description = "Endpoints de prueba de acceso según roles")
public class TestController {

    @GetMapping("/all")
    @Operation(summary = "Acceso público", description = "Endpoint accesible sin autenticar")
    @ApiResponse(responseCode = "200", description = "Contenido público devuelto correctamente")
    public String allAccess() {
        return "Contenido público";
    }

    @GetMapping("/cliente")
    @PreAuthorize("hasAuthority('CLIENTE') or hasAuthority('EMPLEADO') or hasAuthority('GERENTE')")
    @Operation(summary = "Acceso cliente/empleado/gerente", description = "Endpoint accesible para usuarios con rol CLIENTE, EMPLEADO o GERENTE")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Contenido para clientes devuelto correctamente"),
        @ApiResponse(responseCode = "403", description = "Acceso denegado")
    })
    public String clienteAccess() {
        return "Contenido para clientes";
    }

    @GetMapping("/empleado")
    @PreAuthorize("hasAuthority('EMPLEADO') or hasAuthority('GERENTE')")
    @Operation(summary = "Acceso empleado/gerente", description = "Endpoint accesible para usuarios con rol EMPLEADO o GERENTE")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Contenido para empleados devuelto correctamente"),
        @ApiResponse(responseCode = "403", description = "Acceso denegado")
    })
    public String empleadoAccess() {
        return "Contenido para empleados";
    }

    @GetMapping("/gerente")
    @PreAuthorize("hasAuthority('GERENTE')")
    @Operation(summary = "Acceso gerente", description = "Endpoint accesible únicamente para usuarios con rol GERENTE")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Contenido para gerentes devuelto correctamente"),
        @ApiResponse(responseCode = "403", description = "Acceso denegado")
    })
    public String gerenteAccess() {
        return "Contenido para gerentes";
    }
}
