package com.letrasypapeles.backend.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.junit.jupiter.api.Assertions.*;

@SpringJUnitConfig
class TestControllerTest {

    @InjectMocks
    private TestController testController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testAllAccess() {
        String result = testController.allAccess();
        assertEquals("Contenido público", result);
    }

    @Test
    @WithMockUser(authorities = { "CLIENTE" })
    void testClienteAccess() {
        String result = testController.clienteAccess();
        assertEquals("Contenido para clientes", result);
    }

    @Test
    @WithMockUser(authorities = { "EMPLEADO" })
    void testEmpleadoAccess() {
        String result = testController.empleadoAccess();
        assertEquals("Contenido para empleados", result);
    }

    @Test
    @WithMockUser(authorities = { "GERENTE" })
    void testGerenteAccess() {
        String result = testController.gerenteAccess();
        assertEquals("Contenido para gerentes", result);
    }

    @Test
    @WithMockUser(authorities = { "EMPLEADO" })
    void testClienteAccessConEmpleado() {
        String result = testController.clienteAccess();
        assertEquals("Contenido para clientes", result);
    }

    @Test
    @WithMockUser(authorities = { "GERENTE" })
    void testEmpleadoAccessConGerente() {
        String result = testController.empleadoAccess();
        assertEquals("Contenido para empleados", result);
    }

    @Test
    @WithMockUser(authorities = { "GERENTE" })
    void testClienteAccessConGerente() {
        String result = testController.clienteAccess();
        assertEquals("Contenido para clientes", result);
    }
}