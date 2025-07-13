package com.letrasypapeles.backend.controller;

import com.letrasypapeles.backend.entity.Reserva;
import com.letrasypapeles.backend.entity.Cliente;
import com.letrasypapeles.backend.entity.Producto;
import com.letrasypapeles.backend.entity.Inventario;
import com.letrasypapeles.backend.service.ReservaService;
import com.letrasypapeles.backend.service.ClienteService;
import com.letrasypapeles.backend.service.ProductoService;
import com.letrasypapeles.backend.service.InventarioService;
import com.letrasypapeles.backend.repository.ClienteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doNothing;

class ReservaControllerTest {

    @Mock
    private ReservaService reservaService;

    @Mock
    private ClienteService clienteService;

    @Mock
    private ProductoService productoService;

    @Mock
    private InventarioService inventarioService;

    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private ReservaController reservaController;

    private Reserva reserva;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        reserva = new Reserva();
        reserva.setId(1L);
    }

    @Test
    void testObtenerTodas() {
        List<Reserva> reservas = Arrays.asList(reserva);
        when(reservaService.obtenerTodas()).thenReturn(reservas);

        ResponseEntity<List<Reserva>> response = reservaController.obtenerTodas();

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testObtenerPorId() {
        when(reservaService.obtenerPorId(1L)).thenReturn(Optional.of(reserva));

        ResponseEntity<Reserva> response = reservaController.obtenerPorId(1L);

        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void testObtenerPorIdNoEncontrado() {
        when(reservaService.obtenerPorId(1L)).thenReturn(Optional.empty());

        ResponseEntity<Reserva> response = reservaController.obtenerPorId(1L);

        assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    void testObtenerPorClienteId() {
        List<Reserva> reservas = Arrays.asList(reserva);
        when(reservaService.obtenerPorClienteId(1L)).thenReturn(reservas);

        ResponseEntity<List<Reserva>> response = reservaController.obtenerPorClienteId(1L);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testCrearReserva() {
        // Setup proper cliente and producto with IDs
        Cliente cliente = new Cliente();
        cliente.setId(1L);
        Producto producto = new Producto();
        producto.setId(1L);
        producto.setStock(10);

        // Create request body map
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("clienteId", 1L);
        requestBody.put("productoId", 1L);
        requestBody.put("cantidad", 5);

        // Setup inventory mock
        Inventario inventario = new Inventario();
        inventario.setId(1L);
        inventario.setCantidad(10);

        when(clienteService.obtenerPorId(1L)).thenReturn(Optional.of(cliente));
        when(productoService.obtenerPorId(1L)).thenReturn(Optional.of(producto));
        when(productoService.guardar(any(Producto.class))).thenReturn(producto);
        when(inventarioService.obtenerPorProductoId(1L)).thenReturn(Arrays.asList(inventario));
        when(inventarioService.guardar(any(Inventario.class))).thenReturn(inventario);
        when(reservaService.guardar(any(Reserva.class))).thenReturn(reserva);

        ResponseEntity<?> response = reservaController.crearReserva(requestBody);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testCrearReserva_ConClienteObject() {
        // Test the cliente object branch (lines 70-75)
        Map<String, Object> requestBody = new HashMap<>();
        Map<String, Object> clienteMap = new HashMap<>();
        clienteMap.put("id", 1L);
        requestBody.put("cliente", clienteMap);
        requestBody.put("productoId", 1L);
        requestBody.put("cantidad", 2);

        Cliente cliente = new Cliente();
        cliente.setId(1L);

        Producto producto = new Producto();
        producto.setId(1L);
        producto.setStock(10);

        when(clienteService.obtenerPorId(1L)).thenReturn(Optional.of(cliente));
        when(productoService.obtenerPorId(1L)).thenReturn(Optional.of(producto));
        when(inventarioService.obtenerPorProductoId(1L)).thenReturn(Arrays.asList());
        when(reservaService.guardar(any(Reserva.class))).thenReturn(reserva);

        ResponseEntity<?> response = reservaController.crearReserva(requestBody);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(clienteService).obtenerPorId(1L);
        verify(productoService).obtenerPorId(1L);
    }

    @Test
    void testCrearReserva_ConProductoObject() {
        // Test the producto object branch (lines 87-92)
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("clienteId", 1L);
        Map<String, Object> productoMap = new HashMap<>();
        productoMap.put("id", 1L);
        requestBody.put("producto", productoMap);
        requestBody.put("cantidad", 2);

        Cliente cliente = new Cliente();
        cliente.setId(1L);

        Producto producto = new Producto();
        producto.setId(1L);
        producto.setStock(10);

        when(clienteService.obtenerPorId(1L)).thenReturn(Optional.of(cliente));
        when(productoService.obtenerPorId(1L)).thenReturn(Optional.of(producto));
        when(inventarioService.obtenerPorProductoId(1L)).thenReturn(Arrays.asList());
        when(reservaService.guardar(any(Reserva.class))).thenReturn(reserva);

        ResponseEntity<?> response = reservaController.crearReserva(requestBody);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(clienteService).obtenerPorId(1L);
        verify(productoService).obtenerPorId(1L);
    }

    @Test
    void testCrearReserva_ClienteNoEncontrado() {
        // Test cliente not found exception (line 79)
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("clienteId", 1L);
        requestBody.put("productoId", 1L);
        requestBody.put("cantidad", 2);

        when(clienteService.obtenerPorId(1L)).thenReturn(Optional.empty());

        ResponseEntity<?> response = reservaController.crearReserva(requestBody);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(clienteService).obtenerPorId(1L);
    }

    @Test
    void testCrearReserva_ProductoNoEncontrado() {
        // Test producto not found exception (line 96)
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("clienteId", 1L);
        requestBody.put("productoId", 1L);
        requestBody.put("cantidad", 2);

        Cliente cliente = new Cliente();
        cliente.setId(1L);

        when(clienteService.obtenerPorId(1L)).thenReturn(Optional.of(cliente));
        when(productoService.obtenerPorId(1L)).thenReturn(Optional.empty());

        ResponseEntity<?> response = reservaController.crearReserva(requestBody);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(clienteService).obtenerPorId(1L);
        verify(productoService).obtenerPorId(1L);
    }

    @Test
    void testCrearReserva_Exception() {
        // Test general exception handling (lines 141-143)
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("clienteId", 1L);
        requestBody.put("productoId", 1L);
        requestBody.put("cantidad", 2);

        when(clienteService.obtenerPorId(1L)).thenThrow(new RuntimeException("Database error"));

        ResponseEntity<?> response = reservaController.crearReserva(requestBody);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(clienteService).obtenerPorId(1L);
    }

    @Test
    void testActualizarReserva() {
        when(reservaService.obtenerPorId(1L)).thenReturn(Optional.of(reserva));
        when(reservaService.guardar(any(Reserva.class))).thenReturn(reserva);

        ResponseEntity<Reserva> response = reservaController.actualizarReserva(1L, reserva);

        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void testActualizarReservaNoEncontrada() {
        when(reservaService.obtenerPorId(1L)).thenReturn(Optional.empty());

        ResponseEntity<Reserva> response = reservaController.actualizarReserva(1L, reserva);

        assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    void testEliminarReserva() {
        when(reservaService.obtenerPorId(1L)).thenReturn(Optional.of(reserva));
        doNothing().when(reservaService).eliminar(1L);

        ResponseEntity<Void> response = reservaController.eliminarReserva(1L);

        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void testEliminarReservaNoEncontrada() {
        when(reservaService.obtenerPorId(1L)).thenReturn(Optional.empty());

        ResponseEntity<Void> response = reservaController.eliminarReserva(1L);

        assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    void testConfirmarReserva_ReservaNoEncontrada() {
        // Test reserva not found (lines 173-174)
        when(reservaService.obtenerPorIdConRelaciones(1L)).thenReturn(Optional.empty());

        ResponseEntity<?> response = reservaController.confirmarReserva(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(reservaService).obtenerPorIdConRelaciones(1L);
    }

    @Test
    void testConfirmarReserva_CantidadNull() {
        // Test cantidad null exception (lines 186-187)
        Reserva reserva = new Reserva();
        reserva.setId(1L);
        reserva.setEstado("PENDIENTE");
        reserva.setCantidad(null); // This will trigger the exception

        Cliente cliente = new Cliente();
        cliente.setId(1L);
        Producto producto = new Producto();
        producto.setId(1L);
        reserva.setCliente(cliente);
        reserva.setProducto(producto);

        when(reservaService.obtenerPorIdConRelaciones(1L)).thenReturn(Optional.of(reserva));

        ResponseEntity<?> response = reservaController.confirmarReserva(1L);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(reservaService).obtenerPorIdConRelaciones(1L);
    }

    @Test
    void testConfirmarReserva_PrecioNull() {
        // Test precio null exception (lines 189-190)
        Reserva reserva = new Reserva();
        reserva.setId(1L);
        reserva.setEstado("PENDIENTE");
        reserva.setCantidad(2);

        Cliente cliente = new Cliente();
        cliente.setId(1L);
        Producto producto = new Producto();
        producto.setId(1L);
        producto.setPrecio(null); // This will trigger the exception
        reserva.setCliente(cliente);
        reserva.setProducto(producto);

        when(reservaService.obtenerPorIdConRelaciones(1L)).thenReturn(Optional.of(reserva));

        ResponseEntity<?> response = reservaController.confirmarReserva(1L);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(reservaService).obtenerPorIdConRelaciones(1L);
    }

    @Test
    void testConfirmarReserva_PuntosActualizadosNull() {
        // Test puntos actualizados null (lines 199-200)
        Reserva reserva = new Reserva();
        reserva.setId(1L);
        reserva.setEstado("PENDIENTE");
        reserva.setCantidad(2);

        Cliente cliente = new Cliente();
        cliente.setId(1L);
        Producto producto = new Producto();
        producto.setId(1L);
        producto.setPrecio(java.math.BigDecimal.valueOf(10.0));
        reserva.setCliente(cliente);
        reserva.setProducto(producto);

        when(reservaService.obtenerPorIdConRelaciones(1L)).thenReturn(Optional.of(reserva));
        when(reservaService.guardar(any(Reserva.class))).thenReturn(reserva);
        doNothing().when(clienteService).actualizarPuntosFidelidadDirecto(1L, 2);
        when(clienteRepository.obtenerPuntosFidelidad(1L)).thenReturn(null); // This triggers the branch

        ResponseEntity<?> response = reservaController.confirmarReserva(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(reservaService).obtenerPorIdConRelaciones(1L);
        verify(clienteRepository).obtenerPuntosFidelidad(1L);
    }

    @Test
    void testConfirmarReserva_Exception() {
        // Test general exception handling (lines 206-209)
        when(reservaService.obtenerPorIdConRelaciones(1L)).thenThrow(new RuntimeException("Database error"));

        ResponseEntity<?> response = reservaController.confirmarReserva(1L);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(reservaService).obtenerPorIdConRelaciones(1L);
    }

    @Test
    void testConfirmarReserva_ExceptionWithNullMessage() {
        // Test exception with null message to cover the ternary operator (line 209)
        RuntimeException exceptionWithNullMessage = new RuntimeException() {
            @Override
            public String getMessage() {
                return null;
            }
        };
        when(reservaService.obtenerPorIdConRelaciones(1L)).thenThrow(exceptionWithNullMessage);

        ResponseEntity<?> response = reservaController.confirmarReserva(1L);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(reservaService).obtenerPorIdConRelaciones(1L);
    }

    @Test
    void testCancelarReserva_ReservaNoEncontrada() {
        // Test reserva not found (lines 218-219)
        when(reservaService.obtenerPorIdConRelaciones(1L)).thenReturn(Optional.empty());

        ResponseEntity<?> response = reservaController.cancelarReserva(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(reservaService).obtenerPorIdConRelaciones(1L);
    }

    @Test
    void testCancelarReserva_EstadoNoPermitido() {
        // Test estado not PENDIENTE (lines 225-227)
        Reserva reserva = new Reserva();
        reserva.setId(1L);
        reserva.setEstado("CONFIRMADA"); // Not PENDIENTE

        when(reservaService.obtenerPorIdConRelaciones(1L)).thenReturn(Optional.of(reserva));

        ResponseEntity<?> response = reservaController.cancelarReserva(1L);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(reservaService).obtenerPorIdConRelaciones(1L);
    }

    @Test
    void testCancelarReserva_ProductoOCantidadNull() {
        // Test producto or cantidad null (lines 232-245)
        Reserva reserva = new Reserva();
        reserva.setId(1L);
        reserva.setEstado("PENDIENTE");
        reserva.setProducto(null); // This will skip the stock restoration
        reserva.setCantidad(null);

        when(reservaService.obtenerPorIdConRelaciones(1L)).thenReturn(Optional.of(reserva));
        when(reservaService.guardar(any(Reserva.class))).thenReturn(reserva);

        ResponseEntity<?> response = reservaController.cancelarReserva(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(reservaService).obtenerPorIdConRelaciones(1L);
        verify(reservaService).guardar(any(Reserva.class));
    }

    @Test
    void testCancelarReserva_InventarioVacio() {
        // Test empty inventory list (lines 238-244)
        Reserva reserva = new Reserva();
        reserva.setId(1L);
        reserva.setEstado("PENDIENTE");
        reserva.setCantidad(2);

        Producto producto = new Producto();
        producto.setId(1L);
        producto.setStock(10);
        reserva.setProducto(producto);

        when(reservaService.obtenerPorIdConRelaciones(1L)).thenReturn(Optional.of(reserva));
        when(reservaService.guardar(any(Reserva.class))).thenReturn(reserva);
        when(productoService.guardar(any(Producto.class))).thenReturn(producto);
        when(inventarioService.obtenerPorProductoId(1L)).thenReturn(Arrays.asList()); // Empty list

        ResponseEntity<?> response = reservaController.cancelarReserva(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(reservaService).obtenerPorIdConRelaciones(1L);
        verify(inventarioService).obtenerPorProductoId(1L);
    }

    @Test
    void testCancelarReserva_InventarioCantidadNull() {
        // Test inventory cantidad null (lines 240-243)
        Reserva reserva = new Reserva();
        reserva.setId(1L);
        reserva.setEstado("PENDIENTE");
        reserva.setCantidad(2);

        Producto producto = new Producto();
        producto.setId(1L);
        producto.setStock(10);
        reserva.setProducto(producto);

        Inventario inventario = new Inventario();
        inventario.setCantidad(null); // This will skip the inventory update

        when(reservaService.obtenerPorIdConRelaciones(1L)).thenReturn(Optional.of(reserva));
        when(reservaService.guardar(any(Reserva.class))).thenReturn(reserva);
        when(productoService.guardar(any(Producto.class))).thenReturn(producto);
        when(inventarioService.obtenerPorProductoId(1L)).thenReturn(Arrays.asList(inventario));

        ResponseEntity<?> response = reservaController.cancelarReserva(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(reservaService).obtenerPorIdConRelaciones(1L);
        verify(inventarioService).obtenerPorProductoId(1L);
    }

    @Test
    void testCancelarReserva_Exception() {
        // Test general exception handling (lines 252-255)
        when(reservaService.obtenerPorIdConRelaciones(1L)).thenThrow(new RuntimeException("Database error"));

        ResponseEntity<?> response = reservaController.cancelarReserva(1L);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(reservaService).obtenerPorIdConRelaciones(1L);
    }

    @Test
    void testCancelarReserva_ExceptionWithNullMessage() {
        // Test exception with null message to cover the ternary operator (line 255)
        RuntimeException exceptionWithNullMessage = new RuntimeException() {
            @Override
            public String getMessage() {
                return null;
            }
        };
        when(reservaService.obtenerPorIdConRelaciones(1L)).thenThrow(exceptionWithNullMessage);

        ResponseEntity<?> response = reservaController.cancelarReserva(1L);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(reservaService).obtenerPorIdConRelaciones(1L);
    }

    @Test
    void testCrearReserva_ClienteObjectBranch() {
        // Given - Test the cliente object branch (line 70)
        Map<String, Object> requestBody = new HashMap<>();
        Map<String, Object> clienteMap = new HashMap<>();
        clienteMap.put("id", 1L);
        requestBody.put("cliente", clienteMap);
        requestBody.put("productoId", 1L);
        requestBody.put("cantidad", 5);

        Cliente testCliente = Cliente.builder().id(1L).nombre("Test Cliente").build();
        Producto testProducto = Producto.builder().id(1L).nombre("Test Producto").precio(BigDecimal.valueOf(100.0)).stock(10).build();
        Inventario testInventario = Inventario.builder().id(1L).cantidad(20).umbral(5).build();

        when(clienteService.obtenerPorId(1L)).thenReturn(Optional.of(testCliente));
        when(productoService.obtenerPorId(1L)).thenReturn(Optional.of(testProducto));
        when(inventarioService.obtenerPorProductoId(1L)).thenReturn(Arrays.asList(testInventario));
        when(reservaService.guardar(any(Reserva.class))).thenReturn(reserva);

        // When
        ResponseEntity<?> response = reservaController.crearReserva(requestBody);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testCrearReserva_ClienteMapSinIdBranch() {
        // Given - Test cliente map without id (line 72)
        Map<String, Object> requestBody = new HashMap<>();
        Map<String, Object> clienteMap = new HashMap<>();
        // No id in cliente map
        requestBody.put("cliente", clienteMap);
        requestBody.put("productoId", 1L);
        requestBody.put("cantidad", 5);

        Producto testProducto = Producto.builder().id(1L).nombre("Test Producto").precio(BigDecimal.valueOf(100.0)).stock(10).build();
        when(productoService.obtenerPorId(1L)).thenReturn(Optional.of(testProducto));
        when(inventarioService.obtenerPorProductoId(1L)).thenReturn(Arrays.asList());
        when(reservaService.guardar(any(Reserva.class))).thenReturn(reserva);

        // When
        ResponseEntity<?> response = reservaController.crearReserva(requestBody);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testCrearReserva_ProductoObjectBranch() {
        // Given - Test the producto object branch (line 87)
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("clienteId", 1L);
        Map<String, Object> productoMap = new HashMap<>();
        productoMap.put("id", 1L);
        requestBody.put("producto", productoMap);
        requestBody.put("cantidad", 5);

        Cliente testCliente = Cliente.builder().id(1L).nombre("Test Cliente").build();
        Producto testProducto = Producto.builder().id(1L).nombre("Test Producto").precio(BigDecimal.valueOf(100.0)).stock(10).build();
        Inventario testInventario = Inventario.builder().id(1L).cantidad(20).umbral(5).build();

        when(clienteService.obtenerPorId(1L)).thenReturn(Optional.of(testCliente));
        when(productoService.obtenerPorId(1L)).thenReturn(Optional.of(testProducto));
        when(inventarioService.obtenerPorProductoId(1L)).thenReturn(Arrays.asList(testInventario));
        when(reservaService.guardar(any(Reserva.class))).thenReturn(reserva);

        // When
        ResponseEntity<?> response = reservaController.crearReserva(requestBody);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testCrearReserva_ProductoMapSinIdBranch() {
        // Given - Test producto map without id (line 89)
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("clienteId", 1L);
        Map<String, Object> productoMap = new HashMap<>();
        // No id in producto map
        requestBody.put("producto", productoMap);
        requestBody.put("cantidad", 5);

        Cliente testCliente = Cliente.builder().id(1L).nombre("Test Cliente").build();
        when(clienteService.obtenerPorId(1L)).thenReturn(Optional.of(testCliente));
        when(reservaService.guardar(any(Reserva.class))).thenReturn(reserva);

        // When
        ResponseEntity<?> response = reservaController.crearReserva(requestBody);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testCrearReserva_ProductoIdNullBranch() {
        // Given - Test productoId null branch (line 94)
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("clienteId", 1L);
        requestBody.put("cantidad", 5);
        // No productoId or producto

        Cliente testCliente = Cliente.builder().id(1L).nombre("Test Cliente").build();
        when(clienteService.obtenerPorId(1L)).thenReturn(Optional.of(testCliente));
        when(reservaService.guardar(any(Reserva.class))).thenReturn(reserva);

        // When
        ResponseEntity<?> response = reservaController.crearReserva(requestBody);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testCrearReserva_StockNullBranch() {
        // Given - Test stock null branch (line 102)
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("clienteId", 1L);
        requestBody.put("productoId", 1L);
        requestBody.put("cantidad", 5);

        Cliente testCliente = Cliente.builder().id(1L).nombre("Test Cliente").build();
        Producto productoSinStock = Producto.builder()
                .id(1L)
                .nombre("Producto Test")
                .precio(BigDecimal.valueOf(100.0))
                .stock(null) // Stock null
                .build();

        when(clienteService.obtenerPorId(1L)).thenReturn(Optional.of(testCliente));
        when(productoService.obtenerPorId(1L)).thenReturn(Optional.of(productoSinStock));

        // When
        ResponseEntity<?> response = reservaController.crearReserva(requestBody);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testCrearReserva_InventarioCantidadNullBranch() {
        // Given - Test inventario cantidad null branch (line 116)
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("clienteId", 1L);
        requestBody.put("productoId", 1L);
        requestBody.put("cantidad", 5);

        Cliente testCliente = Cliente.builder().id(1L).nombre("Test Cliente").build();
        Producto testProducto = Producto.builder().id(1L).nombre("Test Producto").precio(BigDecimal.valueOf(100.0)).stock(10).build();
        Inventario inventarioSinCantidad = Inventario.builder()
                .id(1L)
                .cantidad(null) // Cantidad null
                .umbral(10)
                .build();

        when(clienteService.obtenerPorId(1L)).thenReturn(Optional.of(testCliente));
        when(productoService.obtenerPorId(1L)).thenReturn(Optional.of(testProducto));
        when(inventarioService.obtenerPorProductoId(1L)).thenReturn(Arrays.asList(inventarioSinCantidad));
        when(reservaService.guardar(any(Reserva.class))).thenReturn(reserva);

        // When
        ResponseEntity<?> response = reservaController.crearReserva(requestBody);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}