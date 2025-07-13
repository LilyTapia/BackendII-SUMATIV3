package com.letrasypapeles.backend.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.letrasypapeles.backend.dto.LoginRequest;
import com.letrasypapeles.backend.dto.RegisterRequest;
import com.letrasypapeles.backend.entity.*;
import com.letrasypapeles.backend.repository.*;
import com.letrasypapeles.backend.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ActiveProfiles("test")
@Transactional
class BusinessWorkflowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private InventarioRepository inventarioRepository;

    @Autowired
    private SucursalRepository sucursalRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private ProductoService productoService;

    @Autowired
    private ReservaService reservaService;

    @Autowired
    private InventarioService inventarioService;

    private Cliente clienteTest;
    private Producto productoTest;
    private Sucursal sucursalTest;
    private Categoria categoriaTest;
    private Role roleCliente;
    private String clienteToken;

    @BeforeEach
    void setUp() throws Exception {
        // Limpiar datos de prueba
        reservaRepository.deleteAll();
        inventarioRepository.deleteAll();
        productoRepository.deleteAll();
        clienteRepository.deleteAll();
        categoriaRepository.deleteAll();
        sucursalRepository.deleteAll();
        roleRepository.deleteAll();

        // Crear rol cliente
        roleCliente = new Role();
        roleCliente.setNombre("CLIENTE");
        roleCliente = roleRepository.save(roleCliente);

        // Crear categoría
        categoriaTest = new Categoria();
        categoriaTest.setNombre("Papelería");
        categoriaTest.setDescripcion("Productos de papelería");
        categoriaTest = categoriaRepository.save(categoriaTest);

        // Crear sucursal
        sucursalTest = new Sucursal();
        sucursalTest.setNombre("Sucursal Central");
        sucursalTest.setDireccion("Av. Principal 123");
        sucursalTest.setRegion("Metropolitana");
        sucursalTest = sucursalRepository.save(sucursalTest);

        // Crear producto
        productoTest = new Producto();
        productoTest.setNombre("Cuaderno A4");
        productoTest.setDescripcion("Cuaderno universitario de 100 hojas");
        productoTest.setPrecio(new BigDecimal("15.99"));
        productoTest.setStock(50);
        productoTest.setCategoria(categoriaTest);
        productoTest = productoRepository.save(productoTest);

        // Crear inventario
        Inventario inventario = new Inventario();
        inventario.setProducto(productoTest);
        inventario.setSucursal(sucursalTest);
        inventario.setCantidad(50);
        inventario.setUmbral(10);
        inventarioRepository.save(inventario);

        // Crear cliente
        Set<Role> roles = new HashSet<>();
        roles.add(roleCliente);

        clienteTest = Cliente.builder()
                .nombre("Juan")
                .apellido("Pérez")
                .email("juan@example.com")
                .contraseña(passwordEncoder.encode("password123"))
                .puntosFidelidad(0)
                .roles(roles)
                .build();
        clienteTest = clienteRepository.save(clienteTest);

        // Obtener token de autenticación
        clienteToken = obtenerTokenAutenticacion("juan@example.com", "password123");
    }

    private String obtenerTokenAutenticacion(String email, String password) throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(email);
        loginRequest.setPassword(password);

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("token").asText();
    }

    @Test
    void testWorkflowCompleto_RegistroLoginReservaYPuntosFidelidad() throws Exception {
        // 1. Registro de nuevo usuario
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setNombre("María");
        registerRequest.setApellido("González");
        registerRequest.setEmail("maria@example.com");
        registerRequest.setPassword("password123");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("María"))
                .andExpect(jsonPath("$.email").value("maria@example.com"));

        // 2. Login del nuevo usuario
        String mariaToken = obtenerTokenAutenticacion("maria@example.com", "password123");
        assertNotNull(mariaToken);

        // 3. Crear reserva
        String reservaJson = String.format("""
            {
                "clienteId": %d,
                "productoId": %d,
                "cantidad": 5,
                "fechaReserva": "%s"
            }
            """, 
            clienteRepository.findByEmail("maria@example.com").get().getId(),
            productoTest.getId(),
            LocalDateTime.now().toString()
        );

        MvcResult reservaResult = mockMvc.perform(post("/api/reservas")
                .header("Authorization", "Bearer " + mariaToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(reservaJson))
                .andExpect(status().isOk())
                .andReturn();

        // 4. Verificar que la reserva se creó correctamente
        Long reservaId = objectMapper.readTree(reservaResult.getResponse().getContentAsString())
                .get("id").asLong();
        
        Reserva reservaCreada = reservaRepository.findById(reservaId).orElse(null);
        assertNotNull(reservaCreada);
        assertEquals(5, reservaCreada.getCantidad());
        assertEquals("PENDIENTE", reservaCreada.getEstado());

        // 5. Verificar que el stock se redujo
        Producto productoActualizado = productoRepository.findById(productoTest.getId()).orElse(null);
        assertNotNull(productoActualizado);
        assertEquals(45, productoActualizado.getStock()); // 50 - 5 = 45

        // 6. Confirmar reserva (simular compra)
        mockMvc.perform(put("/api/reservas/" + reservaId + "/confirmar")
                .header("Authorization", "Bearer " + mariaToken))
                .andExpect(status().isOk());

        // 7. Verificar que se acumularon puntos de fidelidad
        Cliente mariaActualizada = clienteRepository.findByEmail("maria@example.com").orElse(null);
        assertNotNull(mariaActualizada);
        
        // Calcular puntos esperados: 5 * 15.99 = 79.95, puntos = 79.95 / 10 = 7 puntos
        int puntosEsperados = (int) (5 * productoTest.getPrecio().doubleValue() / 10);
        assertEquals(puntosEsperados, mariaActualizada.getPuntosFidelidad());
    }

    @Test
    void testWorkflow_ReservaConStockInsuficiente() throws Exception {
        // Intentar crear reserva con cantidad mayor al stock disponible
        String reservaJson = String.format("""
            {
                "clienteId": %d,
                "productoId": %d,
                "cantidad": 100,
                "fechaReserva": "%s"
            }
            """, 
            clienteTest.getId(),
            productoTest.getId(),
            LocalDateTime.now().toString()
        );

        mockMvc.perform(post("/api/reservas")
                .header("Authorization", "Bearer " + clienteToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(reservaJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Stock insuficiente"));

        // Verificar que el stock no cambió
        Producto productoSinCambios = productoRepository.findById(productoTest.getId()).orElse(null);
        assertNotNull(productoSinCambios);
        assertEquals(50, productoSinCambios.getStock());
    }

    @Test
    void testWorkflow_AlertasInventarioBajo() throws Exception {
        // Crear múltiples reservas para reducir el stock por debajo del umbral
        for (int i = 0; i < 4; i++) {
            String reservaJson = String.format("""
                {
                    "clienteId": %d,
                    "productoId": %d,
                    "cantidad": 10,
                    "fechaReserva": "%s"
                }
                """, 
                clienteTest.getId(),
                productoTest.getId(),
                LocalDateTime.now().toString()
            );

            mockMvc.perform(post("/api/reservas")
                    .header("Authorization", "Bearer " + clienteToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(reservaJson))
                    .andExpect(status().isOk());
        }

        // Verificar que el stock del producto está reducido (50 - 40 = 10)
        Producto productoActualizado = productoRepository.findById(productoTest.getId()).orElse(null);
        assertNotNull(productoActualizado);
        assertEquals(10, productoActualizado.getStock());

        // También verificar el inventario
        Inventario inventario = inventarioRepository.findByProductoIdAndSucursalId(
                productoTest.getId(), sucursalTest.getId()).orElse(null);
        assertNotNull(inventario);

        // Verificar que se genera alerta de restock
        boolean necesitaRestock = inventarioService.necesitaRestock(inventario.getId());
        assertFalse(necesitaRestock); // Justo en el umbral, no por debajo

        // Hacer una reserva más para que esté por debajo del umbral
        String reservaFinalJson = String.format("""
            {
                "clienteId": %d,
                "productoId": %d,
                "cantidad": 5,
                "fechaReserva": "%s"
            }
            """, 
            clienteTest.getId(),
            productoTest.getId(),
            LocalDateTime.now().toString()
        );

        mockMvc.perform(post("/api/reservas")
                .header("Authorization", "Bearer " + clienteToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(reservaFinalJson))
                .andExpect(status().isOk());

        // Verificar que el stock del producto está reducido (50 - 45 = 5)
        Producto productoFinal = productoRepository.findById(productoTest.getId()).orElse(null);
        assertNotNull(productoFinal);
        assertEquals(5, productoFinal.getStock());

        // Ahora verificar que necesita restock
        inventario = inventarioRepository.findByProductoIdAndSucursalId(
                productoTest.getId(), sucursalTest.getId()).orElse(null);
        assertNotNull(inventario);

        boolean necesitaRestockAhora = inventarioService.necesitaRestock(inventario.getId());
        assertTrue(necesitaRestockAhora);
    }

    @Test
    void testWorkflow_CancelacionReservaYRestauracionStock() throws Exception {
        // 1. Crear reserva
        String reservaJson = String.format("""
            {
                "clienteId": %d,
                "productoId": %d,
                "cantidad": 15,
                "fechaReserva": "%s"
            }
            """,
            clienteTest.getId(),
            productoTest.getId(),
            LocalDateTime.now().toString()
        );

        MvcResult reservaResult = mockMvc.perform(post("/api/reservas")
                .header("Authorization", "Bearer " + clienteToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(reservaJson))
                .andExpect(status().isOk())
                .andReturn();

        Long reservaId = objectMapper.readTree(reservaResult.getResponse().getContentAsString())
                .get("id").asLong();

        // 2. Verificar que el stock se redujo
        Producto productoConStockReducido = productoRepository.findById(productoTest.getId()).orElse(null);
        assertNotNull(productoConStockReducido);
        assertEquals(35, productoConStockReducido.getStock()); // 50 - 15 = 35

        // 3. Cancelar reserva
        mockMvc.perform(put("/api/reservas/" + reservaId + "/cancelar")
                .header("Authorization", "Bearer " + clienteToken))
                .andExpect(status().isOk());

        // 4. Verificar que el stock se restauró
        Producto productoConStockRestaurado = productoRepository.findById(productoTest.getId()).orElse(null);
        assertNotNull(productoConStockRestaurado);
        assertEquals(50, productoConStockRestaurado.getStock()); // Vuelve a 50

        // 5. Verificar que la reserva está cancelada
        Reserva reservaCancelada = reservaRepository.findById(reservaId).orElse(null);
        assertNotNull(reservaCancelada);
        assertEquals("CANCELADA", reservaCancelada.getEstado());
    }

    @Test
    void testWorkflow_MultiplesClientesCompitendoPorStock() throws Exception {
        // Crear segundo cliente
        Set<Role> roles = new HashSet<>();
        roles.add(roleCliente);

        Cliente cliente2 = Cliente.builder()
                .nombre("Ana")
                .apellido("López")
                .email("ana@example.com")
                .contraseña(passwordEncoder.encode("password123"))
                .puntosFidelidad(0)
                .roles(roles)
                .build();
        cliente2 = clienteRepository.save(cliente2);

        String cliente2Token = obtenerTokenAutenticacion("ana@example.com", "password123");

        // Ambos clientes intentan reservar la misma cantidad que agotaría el stock
        String reserva1Json = String.format("""
            {
                "clienteId": %d,
                "productoId": %d,
                "cantidad": 30,
                "fechaReserva": "%s"
            }
            """,
            clienteTest.getId(),
            productoTest.getId(),
            LocalDateTime.now().toString()
        );

        String reserva2Json = String.format("""
            {
                "clienteId": %d,
                "productoId": %d,
                "cantidad": 30,
                "fechaReserva": "%s"
            }
            """,
            cliente2.getId(),
            productoTest.getId(),
            LocalDateTime.now().toString()
        );

        // Primera reserva debe ser exitosa
        mockMvc.perform(post("/api/reservas")
                .header("Authorization", "Bearer " + clienteToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(reserva1Json))
                .andExpect(status().isOk());

        // Segunda reserva debe fallar por stock insuficiente
        mockMvc.perform(post("/api/reservas")
                .header("Authorization", "Bearer " + cliente2Token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(reserva2Json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Stock insuficiente"));

        // Verificar stock final
        Producto productoFinal = productoRepository.findById(productoTest.getId()).orElse(null);
        assertNotNull(productoFinal);
        assertEquals(20, productoFinal.getStock()); // 50 - 30 = 20
    }

    @Test
    void testWorkflow_AccumulacionPuntosFidelidadMultiplesCompras() throws Exception {
        // Realizar múltiples compras para acumular puntos
        for (int i = 0; i < 3; i++) {
            // Crear reserva
            String reservaJson = String.format("""
                {
                    "clienteId": %d,
                    "productoId": %d,
                    "cantidad": 2,
                    "fechaReserva": "%s"
                }
                """,
                clienteTest.getId(),
                productoTest.getId(),
                LocalDateTime.now().toString()
            );

            MvcResult reservaResult = mockMvc.perform(post("/api/reservas")
                    .header("Authorization", "Bearer " + clienteToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(reservaJson))
                    .andExpect(status().isOk())
                    .andReturn();

            Long reservaId = objectMapper.readTree(reservaResult.getResponse().getContentAsString())
                    .get("id").asLong();

            // Confirmar reserva para acumular puntos
            mockMvc.perform(put("/api/reservas/" + reservaId + "/confirmar")
                    .header("Authorization", "Bearer " + clienteToken))
                    .andExpect(status().isOk());
        }

        // Verificar puntos acumulados
        Cliente clienteConPuntos = clienteRepository.findById(clienteTest.getId()).orElse(null);
        assertNotNull(clienteConPuntos);

        // 3 compras * 2 unidades * 15.99 = 95.94, puntos = 95.94 / 10 = 9 puntos
        int puntosEsperados = (int) (3 * 2 * productoTest.getPrecio().doubleValue() / 10);
        assertEquals(puntosEsperados, clienteConPuntos.getPuntosFidelidad());
    }

    @Test
    void testWorkflow_GestionInventarioConMultiplesSucursales() throws Exception {
        // Crear segunda sucursal
        Sucursal sucursal2 = new Sucursal();
        sucursal2.setNombre("Sucursal Norte");
        sucursal2.setDireccion("Av. Norte 456");
        sucursal2.setRegion("Norte");
        sucursal2 = sucursalRepository.save(sucursal2);

        // Crear inventario en segunda sucursal
        Inventario inventario2 = new Inventario();
        inventario2.setProducto(productoTest);
        inventario2.setSucursal(sucursal2);
        inventario2.setCantidad(25);
        inventario2.setUmbral(5);
        inventario2 = inventarioRepository.save(inventario2);

        // Verificar inventarios con stock bajo
        var inventariosConStockBajo = inventarioService.obtenerInventariosConStockBajo();
        assertEquals(0, inventariosConStockBajo.size()); // Ninguno está por debajo del umbral

        // Reducir stock en ambas sucursales
        inventarioService.reducirCantidad(inventario2.getId(), 22); // 25 - 22 = 3 (por debajo del umbral de 5)

        // Verificar que ahora hay inventarios con stock bajo
        inventariosConStockBajo = inventarioService.obtenerInventariosConStockBajo();
        assertEquals(1, inventariosConStockBajo.size());
        assertEquals(sucursal2.getId(), inventariosConStockBajo.get(0).getSucursal().getId());

        // Generar alertas de restock
        var alertas = inventarioService.generarAlertasDeRestock();
        assertEquals(1, alertas.size());
        assertTrue(alertas.get(0).contains("Sucursal Norte"));
        assertTrue(alertas.get(0).contains("stock bajo"));
    }
}
