package com.letrasypapeles.backend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.letrasypapeles.backend.dto.LoginRequest;
import com.letrasypapeles.backend.entity.*;
import com.letrasypapeles.backend.repository.*;
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

import java.util.HashSet;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ActiveProfiles("test")
@Transactional
class RoleBasedAccessControlTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private InventarioRepository inventarioRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private SucursalRepository sucursalRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Role roleCliente;
    private Role roleAdmin;
    private Role roleVendedor;
    private Cliente clienteUser;
    private Cliente adminUser;
    private Cliente vendedorUser;

    // IDs generados para las pruebas
    private Long productoTestId;
    private Long inventarioTestId;
    private Long clienteTestId;

    @BeforeEach
    void setUp() {
        // Limpiar datos de prueba
        clienteRepository.deleteAll();
        roleRepository.deleteAll();

        // Crear roles
        roleCliente = new Role();
        roleCliente.setNombre("CLIENTE");
        roleCliente = roleRepository.save(roleCliente);

        roleAdmin = new Role();
        roleAdmin.setNombre("ADMIN");
        roleAdmin = roleRepository.save(roleAdmin);

        roleVendedor = new Role();
        roleVendedor.setNombre("VENDEDOR");
        roleVendedor = roleRepository.save(roleVendedor);

        // Crear usuario con rol CLIENTE
        Set<Role> clienteRoles = new HashSet<>();
        clienteRoles.add(roleCliente);

        clienteUser = Cliente.builder()
                .nombre("Cliente")
                .apellido("Usuario")
                .email("cliente@example.com")
                .contraseña(passwordEncoder.encode("password123"))
                .puntosFidelidad(0)
                .roles(clienteRoles)
                .build();
        clienteUser = clienteRepository.save(clienteUser);
        clienteTestId = clienteUser.getId();

        // Crear usuario con rol ADMIN
        Set<Role> adminRoles = new HashSet<>();
        adminRoles.add(roleAdmin);

        adminUser = Cliente.builder()
                .nombre("Admin")
                .apellido("Usuario")
                .email("admin@example.com")
                .contraseña(passwordEncoder.encode("admin123"))
                .puntosFidelidad(0)
                .roles(adminRoles)
                .build();
        clienteRepository.save(adminUser);

        // Crear usuario con rol VENDEDOR
        Set<Role> vendedorRoles = new HashSet<>();
        vendedorRoles.add(roleVendedor);

        vendedorUser = Cliente.builder()
                .nombre("Vendedor")
                .apellido("Usuario")
                .email("vendedor@example.com")
                .contraseña(passwordEncoder.encode("vendedor123"))
                .puntosFidelidad(0)
                .roles(vendedorRoles)
                .build();
        clienteRepository.save(vendedorUser);

        // Crear datos de prueba para inventario
        crearDatosPruebaInventario();
    }

    private void crearDatosPruebaInventario() {
        // Limpiar datos relacionados
        inventarioRepository.deleteAll();
        productoRepository.deleteAll();
        sucursalRepository.deleteAll();
        categoriaRepository.deleteAll();

        // Crear categoría
        Categoria categoria = Categoria.builder()
                .nombre("Electrónicos")
                .descripcion("Productos electrónicos")
                .build();
        categoria = categoriaRepository.save(categoria);

        // Crear sucursal
        Sucursal sucursal = Sucursal.builder()
                .nombre("Sucursal Central")
                .direccion("Av. Principal 123")
                .region("Centro")
                .build();
        sucursal = sucursalRepository.save(sucursal);

        // Crear producto y guardar su ID generado
        Producto producto = Producto.builder()
                .nombre("Laptop")
                .descripcion("Laptop para oficina")
                .precio(new java.math.BigDecimal("999.99"))
                .stock(10)
                .categoria(categoria)
                .build();
        producto = productoRepository.save(producto);
        productoTestId = producto.getId();

        // Crear inventario y guardar su ID generado
        Inventario inventario = new Inventario();
        inventario.setCantidad(100);
        inventario.setUmbral(10);
        inventario.setProducto(producto);
        inventario.setSucursal(sucursal);
        inventario = inventarioRepository.save(inventario);
        inventarioTestId = inventario.getId();
    }

    private String obtenerTokenParaUsuario(String email, String password) throws Exception {
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
    void testAccesoCliente_EndpointsPermitidos() throws Exception {
        String clienteToken = obtenerTokenParaUsuario("cliente@example.com", "password123");

        // Cliente puede acceder a sus propios datos
        mockMvc.perform(get("/api/clientes/perfil")
                .header("Authorization", "Bearer " + clienteToken))
                .andExpect(status().isOk());

        // Cliente puede ver productos
        mockMvc.perform(get("/api/productos")
                .header("Authorization", "Bearer " + clienteToken))
                .andExpect(status().isOk());

        // Cliente puede crear reservas
        mockMvc.perform(post("/api/reservas")
                .header("Authorization", "Bearer " + clienteToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"productoId\": 1, \"cantidad\": 1}"))
                .andExpect(status().isOk());
    }

    @Test
    void testAccesoCliente_EndpointsRestringidos() throws Exception {
        String clienteToken = obtenerTokenParaUsuario("cliente@example.com", "password123");

        // Cliente NO puede acceder a funciones administrativas
        mockMvc.perform(get("/api/admin/usuarios")
                .header("Authorization", "Bearer " + clienteToken))
                .andExpect(status().isForbidden());

        // Cliente NO puede gestionar inventario
        mockMvc.perform(post("/api/inventario")
                .header("Authorization", "Bearer " + clienteToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"productoId\": 1, \"cantidad\": 100}"))
                .andExpect(status().isForbidden());

        // Cliente NO puede eliminar productos
        mockMvc.perform(delete("/api/productos/1")
                .header("Authorization", "Bearer " + clienteToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void testAccesoAdmin_TodosLosEndpoints() throws Exception {
        String adminToken = obtenerTokenParaUsuario("admin@example.com", "admin123");

        // Admin puede acceder a funciones administrativas
        mockMvc.perform(get("/api/admin/usuarios")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        // Admin puede gestionar productos
        mockMvc.perform(post("/api/productos")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\": \"Nuevo Producto\", \"precio\": 29.99}"))
                .andExpect(status().isOk());

        // Admin puede gestionar inventario
        mockMvc.perform(post("/api/inventario")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"productoId\": " + productoTestId + ", \"cantidad\": 100}"))
                .andExpect(status().isOk());

        // Admin puede eliminar productos
        mockMvc.perform(delete("/api/productos/" + productoTestId)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        // Admin puede ver todos los clientes
        mockMvc.perform(get("/api/clientes")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    @Test
    void testAccesoVendedor_EndpointsEspecificos() throws Exception {
        String vendedorToken = obtenerTokenParaUsuario("vendedor@example.com", "vendedor123");

        // Vendedor puede ver productos
        mockMvc.perform(get("/api/productos")
                .header("Authorization", "Bearer " + vendedorToken))
                .andExpect(status().isOk());

        // Vendedor puede gestionar reservas
        mockMvc.perform(get("/api/reservas")
                .header("Authorization", "Bearer " + vendedorToken))
                .andExpect(status().isOk());

        // Vendedor puede actualizar inventario
        mockMvc.perform(put("/api/inventario/" + inventarioTestId)
                .header("Authorization", "Bearer " + vendedorToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"cantidad\": 50}"))
                .andExpect(status().isOk());
    }

    @Test
    void testAccesoVendedor_EndpointsRestringidos() throws Exception {
        String vendedorToken = obtenerTokenParaUsuario("vendedor@example.com", "vendedor123");

        // Vendedor NO puede acceder a funciones administrativas
        mockMvc.perform(get("/api/admin/usuarios")
                .header("Authorization", "Bearer " + vendedorToken))
                .andExpect(status().isForbidden());

        // Vendedor NO puede eliminar productos
        mockMvc.perform(delete("/api/productos/1")
                .header("Authorization", "Bearer " + vendedorToken))
                .andExpect(status().isForbidden());

        // Vendedor NO puede crear nuevos usuarios admin
        mockMvc.perform(post("/api/admin/usuarios")
                .header("Authorization", "Bearer " + vendedorToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\": \"Nuevo Admin\", \"email\": \"nuevo@admin.com\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void testAccesoMultipleRoles() throws Exception {
        // Crear usuario con múltiples roles
        Set<Role> multipleRoles = new HashSet<>();
        multipleRoles.add(roleCliente);
        multipleRoles.add(roleVendedor);

        Cliente multiRoleUser = Cliente.builder()
                .nombre("Multi")
                .apellido("Role")
                .email("multirole@example.com")
                .contraseña(passwordEncoder.encode("multi123"))
                .puntosFidelidad(0)
                .roles(multipleRoles)
                .build();
        clienteRepository.save(multiRoleUser);

        String multiToken = obtenerTokenParaUsuario("multirole@example.com", "multi123");

        // Usuario con múltiples roles puede acceder a endpoints de ambos roles
        mockMvc.perform(get("/api/productos")
                .header("Authorization", "Bearer " + multiToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/reservas")
                .header("Authorization", "Bearer " + multiToken))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/inventario/" + inventarioTestId)
                .header("Authorization", "Bearer " + multiToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"cantidad\": 50}"))
                .andExpect(status().isOk());
    }

    @Test
    void testAccesoSinRoles() throws Exception {
        // Crear usuario sin roles
        Cliente sinRolesUser = Cliente.builder()
                .nombre("Sin")
                .apellido("Roles")
                .email("sinroles@example.com")
                .contraseña(passwordEncoder.encode("sinroles123"))
                .puntosFidelidad(0)
                .roles(new HashSet<>())
                .build();
        clienteRepository.save(sinRolesUser);

        String sinRolesToken = obtenerTokenParaUsuario("sinroles@example.com", "sinroles123");

        // Usuario sin roles no puede acceder a endpoints protegidos
        mockMvc.perform(get("/api/productos")
                .header("Authorization", "Bearer " + sinRolesToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/reservas")
                .header("Authorization", "Bearer " + sinRolesToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void testValidacionJerarquiaRoles() throws Exception {
        String adminToken = obtenerTokenParaUsuario("admin@example.com", "admin123");
        String clienteToken = obtenerTokenParaUsuario("cliente@example.com", "password123");

        // Admin puede modificar roles de otros usuarios
        mockMvc.perform(put("/api/admin/usuarios/" + clienteTestId + "/roles")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"roles\": [\"CLIENTE\", \"VENDEDOR\"]}"))
                .andExpect(status().isOk());

        // Cliente NO puede modificar roles
        mockMvc.perform(put("/api/admin/usuarios/" + clienteTestId + "/roles")
                .header("Authorization", "Bearer " + clienteToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"roles\": [\"ADMIN\"]}"))
                .andExpect(status().isForbidden());
    }
}
