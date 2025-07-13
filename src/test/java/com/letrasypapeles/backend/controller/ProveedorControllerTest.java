package com.letrasypapeles.backend.controller;

import com.letrasypapeles.backend.entity.Proveedor;
import com.letrasypapeles.backend.service.ProveedorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProveedorControllerTest {

    @Mock
    private ProveedorService proveedorService;

    @InjectMocks
    private ProveedorController proveedorController;

    private Proveedor proveedor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        proveedor = new Proveedor();
        proveedor.setId(1L);
        proveedor.setNombre("Proveedor Test");
        proveedor.setContacto("contacto@test.com");
    }

    @Test
    void obtenerTodos_DeberiaRetornarListaProveedores() {
        List<Proveedor> proveedores = Arrays.asList(proveedor);
        doReturn(proveedores).when(proveedorService).obtenerTodos();

        ResponseEntity<List<Proveedor>> response = proveedorController.obtenerTodos();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals("Proveedor Test", response.getBody().get(0).getNombre());
    }

    @Test
    void obtenerPorId_ProveedorExiste_DeberiaRetornarProveedor() {
        doReturn(Optional.of(proveedor)).when(proveedorService).obtenerPorId(1L);

        ResponseEntity<Proveedor> response = proveedorController.obtenerPorId(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Proveedor Test", response.getBody().getNombre());
        assertEquals("contacto@test.com", response.getBody().getContacto());
    }

    @Test
    void obtenerPorId_ProveedorNoExiste_DeberiaRetornar404() {
        doReturn(Optional.empty()).when(proveedorService).obtenerPorId(1L);
        ResponseEntity<Proveedor> response = proveedorController.obtenerPorId(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void crearProveedor_DeberiaRetornarProveedorCreado() {
        doReturn(proveedor).when(proveedorService).guardar(any(Proveedor.class));

        ResponseEntity<Proveedor> response = proveedorController.crearProveedor(proveedor);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Proveedor Test", response.getBody().getNombre());
        verify(proveedorService).guardar(proveedor);
    }

    @Test
    void actualizarProveedor_ProveedorExiste_DeberiaActualizar() {
        Proveedor proveedorActualizado = new Proveedor();
        proveedorActualizado.setId(1L);
        proveedorActualizado.setNombre("Proveedor Actualizado");
        proveedorActualizado.setContacto("nuevo@contacto.com");

        doReturn(Optional.of(proveedor)).when(proveedorService).obtenerPorId(1L);
        doReturn(proveedorActualizado).when(proveedorService).guardar(any(Proveedor.class));

        ResponseEntity<Proveedor> response = proveedorController.actualizarProveedor(1L, proveedorActualizado);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Proveedor Actualizado", response.getBody().getNombre());
        verify(proveedorService).guardar(any(Proveedor.class));
    }

    @Test
    void actualizarProveedor_ProveedorNoExiste_DeberiaRetornar404() {
        doReturn(Optional.empty()).when(proveedorService).obtenerPorId(1L);

        ResponseEntity<Proveedor> response = proveedorController.actualizarProveedor(1L, proveedor);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void eliminarProveedor_ProveedorExiste_DeberiaEliminar() {
        doReturn(Optional.of(proveedor)).when(proveedorService).obtenerPorId(1L);

        ResponseEntity<Void> response = proveedorController.eliminarProveedor(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(proveedorService).eliminar(1L);
    }

    @Test
    void eliminarProveedor_ProveedorNoExiste_DeberiaRetornar404() {
        doReturn(Optional.empty()).when(proveedorService).obtenerPorId(1L);

        ResponseEntity<Void> response = proveedorController.eliminarProveedor(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(proveedorService, never()).eliminar(1L);
    }
}