package com.letrasypapeles.backend.service;

import com.letrasypapeles.backend.entity.Cliente;
import com.letrasypapeles.backend.entity.Pedido;
import com.letrasypapeles.backend.entity.Producto;
import com.letrasypapeles.backend.repository.PedidoRepository;
import com.letrasypapeles.backend.repository.ClienteRepository;
import com.letrasypapeles.backend.repository.ProductoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PedidoServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private PedidoService pedidoService;

    private Pedido pedido;
    private Cliente cliente;
    private Producto producto;
    private LocalDateTime fecha;

    @BeforeEach
    void setUp() {
        // Configurar datos de prueba
        fecha = LocalDateTime.now();
        
        cliente = Cliente.builder()
                .id(1L)
                .nombre("Test")
                .apellido("Usuario")
                .email("test@example.com")
                .build();

        producto = Producto.builder()
                .id(1L)
                .nombre("El Quijote")
                .build();

        List<Producto> listaProductos = new ArrayList<>();
        listaProductos.add(producto);

        pedido = Pedido.builder()
                .id(1L)
                .fecha(fecha)
                .estado("PENDIENTE")
                .cliente(cliente)
                .listaProductos(listaProductos)
                .build();
    }

    @Test
    void obtenerTodos() {
        // Given
        List<Pedido> pedidos = Arrays.asList(pedido);
        when(pedidoRepository.findAll()).thenReturn(pedidos);

        // When
        List<Pedido> result = pedidoService.obtenerTodos();

        // Then
        assertEquals(1, result.size());
        assertEquals("PENDIENTE", result.get(0).getEstado());
        verify(pedidoRepository, times(1)).findAll();
    }

    @Test
    void obtenerPorId() {
        // Given
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));

        // When
        Optional<Pedido> result = pedidoService.obtenerPorId(1L);

        // Then
        assertTrue(result.isPresent());
        assertEquals("PENDIENTE", result.get().getEstado());
        assertEquals(cliente.getId(), result.get().getCliente().getId());
        verify(pedidoRepository, times(1)).findById(1L);
    }

    @Test
    void obtenerPorIdNoExistente() {
        // Given
        when(pedidoRepository.findById(99L)).thenReturn(Optional.empty());

        // When
        Optional<Pedido> result = pedidoService.obtenerPorId(99L);

        // Then
        assertFalse(result.isPresent());
        verify(pedidoRepository, times(1)).findById(99L);
    }

    @Test
    void guardar() {
        // Given
        Pedido pedidoNuevo = Pedido.builder()
                .fecha(fecha)
                .estado("NUEVO")
                .cliente(cliente)
                .listaProductos(new ArrayList<>())
                .build();

        Pedido pedidoGuardado = Pedido.builder()
                .id(2L)
                .fecha(fecha)
                .estado("NUEVO")
                .cliente(cliente)
                .listaProductos(new ArrayList<>())
                .build();
        
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedidoGuardado);

        // When
        Pedido result = pedidoService.guardar(pedidoNuevo);

        // Then
        assertNotNull(result);
        assertEquals(2L, result.getId());
        assertEquals("NUEVO", result.getEstado());
        verify(pedidoRepository, times(1)).save(any(Pedido.class));
    }

    @Test
    void eliminar() {
        // Given
        Long idToDelete = 1L;
        doNothing().when(pedidoRepository).deleteById(idToDelete);

        // When
        pedidoService.eliminar(idToDelete);

        // Then
        verify(pedidoRepository, times(1)).deleteById(idToDelete);
    }

    @Test
    void obtenerPorClienteId() {
        // Given
        List<Pedido> pedidos = Arrays.asList(pedido);
        when(pedidoRepository.findByClienteId(1L)).thenReturn(pedidos);

        // When
        List<Pedido> result = pedidoService.obtenerPorClienteId(1L);

        // Then
        assertEquals(1, result.size());
        assertEquals("PENDIENTE", result.get(0).getEstado());
        assertEquals(1L, result.get(0).getCliente().getId());
        verify(pedidoRepository, times(1)).findByClienteId(1L);
    }

    @Test
    void obtenerPorEstado() {
        // Given
        List<Pedido> pedidos = Arrays.asList(pedido);
        when(pedidoRepository.findByEstado("PENDIENTE")).thenReturn(pedidos);

        // When
        List<Pedido> result = pedidoService.obtenerPorEstado("PENDIENTE");

        // Then
        assertEquals(1, result.size());
        assertEquals("PENDIENTE", result.get(0).getEstado());
        verify(pedidoRepository, times(1)).findByEstado("PENDIENTE");
    }
}
