package com.letrasypapeles.backend.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.letrasypapeles.backend.entity.*;
import com.letrasypapeles.backend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ActiveProfiles("test")
@Transactional
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
public class HateoasIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Cliente cliente;
    private Producto producto;
    private Categoria categoria;
    private Pedido pedido;
    private Reserva reserva;

    @BeforeEach
    void setUp() {
        // Create test role if it doesn't exist
        Role clienteRole = roleRepository.findById("ROLE_CLIENTE")
                .orElseGet(() -> {
                    Role newRole = Role.builder()
                            .nombre("ROLE_CLIENTE")
                            .build();
                    return roleRepository.save(newRole);
                });

        // Create test category
        categoria = Categoria.builder()
                .nombre("Papelería")
                .descripcion("Productos de papelería")
                .build();
        categoria = categoriaRepository.save(categoria);

        // Create test product
        producto = Producto.builder()
                .nombre("Cuaderno")
                .descripcion("Cuaderno universitario")
                .precio(new BigDecimal("5.99"))
                .stock(100)
                .categoria(categoria)
                .build();
        producto = productoRepository.save(producto);

        // Create test client
        cliente = Cliente.builder()
                .nombre("Juan")
                .apellido("Pérez")
                .email("juan@test.com")
                .contraseña(passwordEncoder.encode("password"))
                .puntosFidelidad(0)
                .roles(Set.of(clienteRole))
                .build();
        cliente = clienteRepository.save(cliente);
    }

    @Test
    void testProductoHateoasLinks() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/productos/{id}", producto.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        String content = result.getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(content);

        // Verify product data
        assertEquals("Cuaderno", jsonNode.get("nombre").asText());
        assertEquals("5.99", jsonNode.get("precio").asText());

        // Verify HATEOAS links
        JsonNode links = jsonNode.get("_links");
        assertNotNull(links);
        
        // Self link
        assertTrue(links.has("self"));
        assertTrue(links.get("self").get("href").asText().contains("/api/productos/" + producto.getId()));
        
        // Products collection link
        assertTrue(links.has("productos"));
        assertTrue(links.get("productos").get("href").asText().contains("/api/productos"));
        
        // Category link
        assertTrue(links.has("categoria"));
        assertTrue(links.get("categoria").get("href").asText().contains("/api/categorias/" + categoria.getId()));
        
        // Update link
        assertTrue(links.has("update"));
        
        // Delete link
        assertTrue(links.has("delete"));
    }

    @Test
    void testClienteHateoasLinks() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/clientes/{id}", cliente.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        String content = result.getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(content);

        // Verify client data
        assertEquals("Juan", jsonNode.get("nombre").asText());
        assertEquals("Pérez", jsonNode.get("apellido").asText());

        // Verify HATEOAS links
        JsonNode links = jsonNode.get("_links");
        assertNotNull(links);
        
        // Self link
        assertTrue(links.has("self"));
        assertTrue(links.get("self").get("href").asText().contains("/api/clientes/" + cliente.getId()));
        
        // Clients collection link
        assertTrue(links.has("clientes"));
        assertTrue(links.get("clientes").get("href").asText().contains("/api/clientes"));
        
        // Orders link
        assertTrue(links.has("pedidos"));
        assertTrue(links.get("pedidos").get("href").asText().contains("/api/pedidos/cliente/" + cliente.getId()));
        
        // Reservations link
        assertTrue(links.has("reservas"));
        assertTrue(links.get("reservas").get("href").asText().contains("/api/reservas"));
    }

    @Test
    void testCategoriaHateoasLinks() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/categorias/{id}", categoria.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        String content = result.getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(content);

        // Verify category data
        assertEquals("Papelería", jsonNode.get("nombre").asText());

        // Verify HATEOAS links
        JsonNode links = jsonNode.get("_links");
        assertNotNull(links);
        
        // Self link
        assertTrue(links.has("self"));
        assertTrue(links.get("self").get("href").asText().contains("/api/categorias/" + categoria.getId()));
        
        // Categories collection link
        assertTrue(links.has("categorias"));
        assertTrue(links.get("categorias").get("href").asText().contains("/api/categorias"));
        
        // Products link
        assertTrue(links.has("productos"));
        assertTrue(links.get("productos").get("href").asText().contains("/api/productos"));
    }

    @Test
    void testProductosCollectionHateoasLinks() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/productos")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        String content = result.getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(content);

        // Verify collection structure
        assertTrue(jsonNode.has("_embedded"));
        assertTrue(jsonNode.has("_links"));
        
        // Verify self link for collection
        JsonNode links = jsonNode.get("_links");
        assertTrue(links.has("self"));
        assertTrue(links.get("self").get("href").asText().contains("/api/productos"));
        
        // Verify embedded products have links
        JsonNode embedded = jsonNode.get("_embedded");
        if (embedded.has("productoList")) {
            JsonNode productos = embedded.get("productoList");
            if (productos.isArray() && productos.size() > 0) {
                JsonNode firstProduct = productos.get(0);
                assertTrue(firstProduct.has("_links"));
                assertTrue(firstProduct.get("_links").has("self"));
            }
        }
    }

    @Test
    void testNavigationBetweenResources() throws Exception {
        // Get product
        MvcResult productResult = mockMvc.perform(get("/api/productos/{id}", producto.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String productContent = productResult.getResponse().getContentAsString();
        JsonNode productNode = objectMapper.readTree(productContent);
        
        // Extract category link from product
        String categoryHref = productNode.get("_links").get("categoria").get("href").asText();
        
        // Navigate to category using the link
        MvcResult categoryResult = mockMvc.perform(get(categoryHref)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String categoryContent = categoryResult.getResponse().getContentAsString();
        JsonNode categoryNode = objectMapper.readTree(categoryContent);
        
        // Verify we got the correct category
        assertEquals(categoria.getId().toString(), categoryNode.get("id").asText());
        assertEquals("Papelería", categoryNode.get("nombre").asText());
    }
}
