package com.letrasypapeles.backend.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.letrasypapeles.backend.dto.LoginRequest;
import com.letrasypapeles.backend.dto.RegisterRequest;
import com.letrasypapeles.backend.entity.Cliente;
import com.letrasypapeles.backend.entity.Role;
import com.letrasypapeles.backend.repository.ClienteRepository;
import com.letrasypapeles.backend.repository.RoleRepository;
import com.letrasypapeles.backend.security.JwtUtil;
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
import java.util.Map;
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
class AuthenticationFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    private Role roleCliente;
    private Role roleAdmin;

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
    }

    @Test
    void testFlowCompleto_RegistroLoginAccesoProtegido() throws Exception {
        // 1. Registro de usuario
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setNombre("Test");
        registerRequest.setApellido("Usuario");
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("password123");

        MvcResult registroResult = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Test"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andReturn();

        // Verificar que el usuario se creó en la base de datos
        Cliente usuarioCreado = clienteRepository.findByEmail("test@example.com").orElse(null);
        assertNotNull(usuarioCreado);
        assertEquals("Test", usuarioCreado.getNombre());
        assertTrue(passwordEncoder.matches("password123", usuarioCreado.getContraseña()));

        // 2. Login del usuario
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.type").value("Bearer"))
                .andReturn();

        String token = objectMapper.readTree(loginResult.getResponse().getContentAsString())
                .get("token").asText();

        // Verificar que el token es válido
        assertTrue(jwtUtil.validateJwtToken(token));
        assertEquals("test@example.com", jwtUtil.getUsernameFromToken(token));

        // 3. Acceso a endpoint protegido con token válido
        mockMvc.perform(get("/api/clientes/perfil")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        // 4. Verificar que sin token no se puede acceder
        mockMvc.perform(get("/api/clientes/perfil"))
                .andExpect(status().isUnauthorized());

        // 5. Verificar que con token inválido no se puede acceder
        mockMvc.perform(get("/api/clientes/perfil")
                .header("Authorization", "Bearer tokenInvalido"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testFlow_AutenticacionConRolesYPermisos() throws Exception {
        // Crear usuario admin
        Set<Role> adminRoles = new HashSet<>();
        adminRoles.add(roleAdmin);

        Cliente adminUser = Cliente.builder()
                .nombre("Admin")
                .apellido("Usuario")
                .email("admin@example.com")
                .contraseña(passwordEncoder.encode("admin123"))
                .puntosFidelidad(0)
                .roles(adminRoles)
                .build();
        clienteRepository.save(adminUser);

        // Crear usuario cliente
        Set<Role> clienteRoles = new HashSet<>();
        clienteRoles.add(roleCliente);

        Cliente clienteUser = Cliente.builder()
                .nombre("Cliente")
                .apellido("Usuario")
                .email("cliente@example.com")
                .contraseña(passwordEncoder.encode("cliente123"))
                .puntosFidelidad(0)
                .roles(clienteRoles)
                .build();
        clienteRepository.save(clienteUser);

        // Login como admin
        LoginRequest adminLogin = new LoginRequest();
        adminLogin.setEmail("admin@example.com");
        adminLogin.setPassword("admin123");

        MvcResult adminLoginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(adminLogin)))
                .andExpect(status().isOk())
                .andReturn();

        String adminToken = objectMapper.readTree(adminLoginResult.getResponse().getContentAsString())
                .get("token").asText();

        // Login como cliente
        LoginRequest clienteLogin = new LoginRequest();
        clienteLogin.setEmail("cliente@example.com");
        clienteLogin.setPassword("cliente123");

        MvcResult clienteLoginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(clienteLogin)))
                .andExpect(status().isOk())
                .andReturn();

        String clienteToken = objectMapper.readTree(clienteLoginResult.getResponse().getContentAsString())
                .get("token").asText();

        // Admin puede acceder a endpoints administrativos
        mockMvc.perform(get("/api/admin/usuarios")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        // Cliente NO puede acceder a endpoints administrativos
        mockMvc.perform(get("/api/admin/usuarios")
                .header("Authorization", "Bearer " + clienteToken))
                .andExpect(status().isForbidden());

        // Ambos pueden acceder a endpoints generales
        mockMvc.perform(get("/api/productos")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/productos")
                .header("Authorization", "Bearer " + clienteToken))
                .andExpect(status().isOk());
    }

    @Test
    void testFlow_TokenExpiracionYRenovacion() throws Exception {
        // Crear usuario
        Set<Role> roles = new HashSet<>();
        roles.add(roleCliente);

        Cliente usuario = Cliente.builder()
                .nombre("Test")
                .apellido("Usuario")
                .email("test@example.com")
                .contraseña(passwordEncoder.encode("password123"))
                .puntosFidelidad(0)
                .roles(roles)
                .build();
        clienteRepository.save(usuario);

        // Login
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String token = objectMapper.readTree(loginResult.getResponse().getContentAsString())
                .get("token").asText();

        // Verificar que el token es válido inicialmente
        assertTrue(jwtUtil.validateJwtToken(token));

        // Usar el token para acceder a un endpoint
        mockMvc.perform(get("/api/clientes/perfil")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        // Simular renovación de token (si el endpoint existe)
        String refreshRequestBody = objectMapper.writeValueAsString(Map.of("token", token));
        mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(refreshRequestBody))
                .andExpect(status().isOk());
    }

    @Test
    void testFlow_LogoutYInvalidacionToken() throws Exception {
        // Crear usuario
        Set<Role> roles = new HashSet<>();
        roles.add(roleCliente);

        Cliente usuario = Cliente.builder()
                .nombre("Test")
                .apellido("Usuario")
                .email("test@example.com")
                .contraseña(passwordEncoder.encode("password123"))
                .puntosFidelidad(0)
                .roles(roles)
                .build();
        clienteRepository.save(usuario);

        // Login
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String token = objectMapper.readTree(loginResult.getResponse().getContentAsString())
                .get("token").asText();

        // Usar el token antes del logout
        mockMvc.perform(get("/api/clientes/perfil")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        // Logout (si el endpoint existe)
        String logoutRequestBody = objectMapper.writeValueAsString(Map.of("token", token));
        mockMvc.perform(post("/api/auth/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(logoutRequestBody))
                .andExpect(status().isOk());
    }

    @Test
    void testFlow_ValidacionCamposRegistroYLogin() throws Exception {
        // Registro con campos inválidos
        RegisterRequest registroInvalido = new RegisterRequest();
        registroInvalido.setNombre("");
        registroInvalido.setApellido("");
        registroInvalido.setEmail("email-invalido");
        registroInvalido.setPassword("123");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registroInvalido)))
                .andExpect(status().isBadRequest());

        // Login con campos vacíos
        LoginRequest loginVacio = new LoginRequest();
        loginVacio.setEmail("");
        loginVacio.setPassword("");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginVacio)))
                .andExpect(status().isBadRequest());

        // Login con credenciales incorrectas
        LoginRequest loginIncorrecto = new LoginRequest();
        loginIncorrecto.setEmail("noexiste@example.com");
        loginIncorrecto.setPassword("passwordIncorrecto");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginIncorrecto)))
                .andExpect(status().isUnauthorized());
    }
}
