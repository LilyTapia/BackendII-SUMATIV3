package com.letrasypapeles.backend.service;

import com.letrasypapeles.backend.entity.Categoria;
import com.letrasypapeles.backend.entity.Producto;
import com.letrasypapeles.backend.entity.Proveedor;
import com.letrasypapeles.backend.repository.ProductoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private ProductoService productoService;

    private Producto producto;
    private Categoria categoria;
    private Proveedor proveedor;

    @BeforeEach
    void setUp() {
        // Configurar datos de prueba
        categoria = Categoria.builder()
                .id(1L)
                .nombre("Libros")
                .build();

        proveedor = Proveedor.builder()
                .id(1L)
                .nombre("Editorial XYZ")
                .build();

        producto = Producto.builder()
                .id(1L)
                .nombre("El Quijote")
                .descripcion("Obra de Miguel de Cervantes")
                .precio(new BigDecimal("29.99"))
                .stock(50)
                .categoria(categoria)
                .proveedor(proveedor)
                .build();
    }

    @Test
    void obtenerTodos() {
        // Given
        List<Producto> productos = Arrays.asList(producto);
        when(productoRepository.findAll()).thenReturn(productos);

        // When
        List<Producto> result = productoService.obtenerTodos();

        // Then
        assertEquals(1, result.size());
        assertEquals("El Quijote", result.get(0).getNombre());
        verify(productoRepository, times(1)).findAll();
    }

    @Test
    void obtenerPorId() {
        // Given
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));

        // When
        Optional<Producto> result = productoService.obtenerPorId(1L);

        // Then
        assertTrue(result.isPresent());
        assertEquals("El Quijote", result.get().getNombre());
        assertEquals(new BigDecimal("29.99"), result.get().getPrecio());
        verify(productoRepository, times(1)).findById(1L);
    }

    @Test
    void obtenerPorIdNoExistente() {
        // Given
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());

        // When
        Optional<Producto> result = productoService.obtenerPorId(99L);

        // Then
        assertFalse(result.isPresent());
        verify(productoRepository, times(1)).findById(99L);
    }

    @Test
    void guardar() {
        // Given
        Producto productoNuevo = Producto.builder()
                .nombre("Cien Años de Soledad")
                .descripcion("Obra de Gabriel García Márquez")
                .precio(new BigDecimal("24.99"))
                .stock(30)
                .categoria(categoria)
                .proveedor(proveedor)
                .build();

        Producto productoGuardado = Producto.builder()
                .id(2L)
                .nombre("Cien Años de Soledad")
                .descripcion("Obra de Gabriel García Márquez")
                .precio(new BigDecimal("24.99"))
                .stock(30)
                .categoria(categoria)
                .proveedor(proveedor)
                .build();
        
        when(productoRepository.save(any(Producto.class))).thenReturn(productoGuardado);

        // When
        Producto result = productoService.guardar(productoNuevo);

        // Then
        assertNotNull(result);
        assertEquals(2L, result.getId());
        assertEquals("Cien Años de Soledad", result.getNombre());
        assertEquals(new BigDecimal("24.99"), result.getPrecio());
        verify(productoRepository, times(1)).save(any(Producto.class));
    }

    @Test
    void eliminar() {
        // Given
        Long idToDelete = 1L;
        doNothing().when(productoRepository).deleteById(idToDelete);

        // When
        productoService.eliminar(idToDelete);

        // Then
        verify(productoRepository, times(1)).deleteById(idToDelete);
    }

    @Test
    void validarStockDisponible_StockSuficiente() {
        // Given
        Long productoId = 1L;
        Integer cantidadSolicitada = 10;
        Producto productoConStock = Producto.builder()
                .id(productoId)
                .nombre("El Quijote")
                .stock(50)
                .build();

        when(productoRepository.findById(productoId)).thenReturn(Optional.of(productoConStock));

        // When
        boolean result = productoService.validarStockDisponible(productoId, cantidadSolicitada);

        // Then
        assertTrue(result);
        verify(productoRepository, times(1)).findById(productoId);
    }

    @Test
    void validarStockDisponible_StockInsuficiente() {
        // Given
        Long productoId = 1L;
        Integer cantidadSolicitada = 60;
        Producto productoConStock = Producto.builder()
                .id(productoId)
                .nombre("El Quijote")
                .stock(50)
                .build();

        when(productoRepository.findById(productoId)).thenReturn(Optional.of(productoConStock));

        // When
        boolean result = productoService.validarStockDisponible(productoId, cantidadSolicitada);

        // Then
        assertFalse(result);
        verify(productoRepository, times(1)).findById(productoId);
    }

    @Test
    void validarStockDisponible_StockNulo() {
        // Given
        Long productoId = 1L;
        Integer cantidadSolicitada = 10;
        Producto productoSinStock = Producto.builder()
                .id(productoId)
                .nombre("El Quijote")
                .stock(null)
                .build();

        when(productoRepository.findById(productoId)).thenReturn(Optional.of(productoSinStock));

        // When
        boolean result = productoService.validarStockDisponible(productoId, cantidadSolicitada);

        // Then
        assertFalse(result);
        verify(productoRepository, times(1)).findById(productoId);
    }

    @Test
    void validarStockDisponible_ProductoNoExistente() {
        // Given
        Long productoId = 999L;
        Integer cantidadSolicitada = 10;
        when(productoRepository.findById(productoId)).thenReturn(Optional.empty());

        // When & Then
        Exception exception = assertThrows(RuntimeException.class, () -> {
            productoService.validarStockDisponible(productoId, cantidadSolicitada);
        });

        assertEquals("Producto no encontrado", exception.getMessage());
        verify(productoRepository, times(1)).findById(productoId);
    }

    @Test
    void reducirStock_Exitoso() {
        // Given
        Long productoId = 1L;
        Integer cantidadReducir = 10;
        Producto productoOriginal = Producto.builder()
                .id(productoId)
                .nombre("El Quijote")
                .stock(50)
                .build();

        Producto productoActualizado = Producto.builder()
                .id(productoId)
                .nombre("El Quijote")
                .stock(40)
                .build();

        when(productoRepository.findById(productoId)).thenReturn(Optional.of(productoOriginal));
        when(productoRepository.save(any(Producto.class))).thenReturn(productoActualizado);

        // When
        Producto result = productoService.reducirStock(productoId, cantidadReducir);

        // Then
        assertNotNull(result);
        assertEquals(40, result.getStock());
        verify(productoRepository, times(1)).findById(productoId);
        verify(productoRepository, times(1)).save(any(Producto.class));
    }

    @Test
    void reducirStock_StockInsuficiente() {
        // Given
        Long productoId = 1L;
        Integer cantidadReducir = 60;
        Producto productoConStockBajo = Producto.builder()
                .id(productoId)
                .nombre("El Quijote")
                .stock(50)
                .build();

        when(productoRepository.findById(productoId)).thenReturn(Optional.of(productoConStockBajo));

        // When & Then
        Exception exception = assertThrows(RuntimeException.class, () -> {
            productoService.reducirStock(productoId, cantidadReducir);
        });

        assertTrue(exception.getMessage().contains("Stock insuficiente"));
        verify(productoRepository, times(1)).findById(productoId);
        verify(productoRepository, never()).save(any(Producto.class));
    }

    @Test
    void reducirStock_StockNulo() {
        // Given
        Long productoId = 1L;
        Integer cantidadReducir = 10;
        Producto productoSinStock = Producto.builder()
                .id(productoId)
                .nombre("El Quijote")
                .stock(null)
                .build();

        when(productoRepository.findById(productoId)).thenReturn(Optional.of(productoSinStock));

        // When & Then
        Exception exception = assertThrows(RuntimeException.class, () -> {
            productoService.reducirStock(productoId, cantidadReducir);
        });

        assertTrue(exception.getMessage().contains("Stock insuficiente"));
        verify(productoRepository, times(1)).findById(productoId);
        verify(productoRepository, never()).save(any(Producto.class));
    }

    @Test
    void aumentarStock_Exitoso() {
        // Given
        Long productoId = 1L;
        Integer cantidadAumentar = 20;
        Producto productoOriginal = Producto.builder()
                .id(productoId)
                .nombre("El Quijote")
                .stock(30)
                .build();

        Producto productoActualizado = Producto.builder()
                .id(productoId)
                .nombre("El Quijote")
                .stock(50)
                .build();

        when(productoRepository.findById(productoId)).thenReturn(Optional.of(productoOriginal));
        when(productoRepository.save(any(Producto.class))).thenReturn(productoActualizado);

        // When
        Producto result = productoService.aumentarStock(productoId, cantidadAumentar);

        // Then
        assertNotNull(result);
        assertEquals(50, result.getStock());
        verify(productoRepository, times(1)).findById(productoId);
        verify(productoRepository, times(1)).save(any(Producto.class));
    }

    @Test
    void aumentarStock_StockNuloInicial() {
        // Given
        Long productoId = 1L;
        Integer cantidadAumentar = 20;
        Producto productoSinStock = Producto.builder()
                .id(productoId)
                .nombre("El Quijote")
                .stock(null)
                .build();

        Producto productoActualizado = Producto.builder()
                .id(productoId)
                .nombre("El Quijote")
                .stock(20)
                .build();

        when(productoRepository.findById(productoId)).thenReturn(Optional.of(productoSinStock));
        when(productoRepository.save(any(Producto.class))).thenReturn(productoActualizado);

        // When
        Producto result = productoService.aumentarStock(productoId, cantidadAumentar);

        // Then
        assertNotNull(result);
        assertEquals(20, result.getStock());
        verify(productoRepository, times(1)).findById(productoId);
        verify(productoRepository, times(1)).save(any(Producto.class));
    }

    @Test
    void aumentarStock_CantidadInvalida() {
        // Given
        Long productoId = 1L;
        Integer cantidadAumentar = -5;
        Producto productoOriginal = Producto.builder()
                .id(productoId)
                .nombre("El Quijote")
                .stock(30)
                .build();

        when(productoRepository.findById(productoId)).thenReturn(Optional.of(productoOriginal));

        // When & Then
        Exception exception = assertThrows(RuntimeException.class, () -> {
            productoService.aumentarStock(productoId, cantidadAumentar);
        });

        assertEquals("La cantidad a aumentar debe ser mayor a 0", exception.getMessage());
        verify(productoRepository, times(1)).findById(productoId);
        verify(productoRepository, never()).save(any(Producto.class));
    }

    @Test
    void tieneStockBajo_StockBajo() {
        // Given
        Long productoId = 1L;
        Integer umbral = 10;
        Producto productoConStockBajo = Producto.builder()
                .id(productoId)
                .nombre("El Quijote")
                .stock(5)
                .build();

        when(productoRepository.findById(productoId)).thenReturn(Optional.of(productoConStockBajo));

        // When
        boolean result = productoService.tieneStockBajo(productoId, umbral);

        // Then
        assertTrue(result);
        verify(productoRepository, times(1)).findById(productoId);
    }

    @Test
    void tieneStockBajo_StockSuficiente() {
        // Given
        Long productoId = 1L;
        Integer umbral = 10;
        Producto productoConStockSuficiente = Producto.builder()
                .id(productoId)
                .nombre("El Quijote")
                .stock(15)
                .build();

        when(productoRepository.findById(productoId)).thenReturn(Optional.of(productoConStockSuficiente));

        // When
        boolean result = productoService.tieneStockBajo(productoId, umbral);

        // Then
        assertFalse(result);
        verify(productoRepository, times(1)).findById(productoId);
    }

    @Test
    void obtenerStockActual_ConStock() {
        // Given
        Long productoId = 1L;
        Producto productoConStock = Producto.builder()
                .id(productoId)
                .nombre("El Quijote")
                .stock(25)
                .build();

        when(productoRepository.findById(productoId)).thenReturn(Optional.of(productoConStock));

        // When
        Integer result = productoService.obtenerStockActual(productoId);

        // Then
        assertEquals(25, result);
        verify(productoRepository, times(1)).findById(productoId);
    }

    @Test
    void obtenerStockActual_StockNulo() {
        // Given
        Long productoId = 1L;
        Producto productoSinStock = Producto.builder()
                .id(productoId)
                .nombre("El Quijote")
                .stock(null)
                .build();

        when(productoRepository.findById(productoId)).thenReturn(Optional.of(productoSinStock));

        // When
        Integer result = productoService.obtenerStockActual(productoId);

        // Then
        assertEquals(0, result);
        verify(productoRepository, times(1)).findById(productoId);
    }

    // Missing exception tests for 100% coverage

    @Test
    void reducirStock_ProductoNoEncontrado() {
        // Given
        Long productoId = 999L;
        Integer cantidadReducir = 10;
        when(productoRepository.findById(productoId)).thenReturn(Optional.empty());

        // When & Then
        Exception exception = assertThrows(RuntimeException.class, () -> {
            productoService.reducirStock(productoId, cantidadReducir);
        });

        assertEquals("Producto no encontrado", exception.getMessage());
        verify(productoRepository).findById(productoId);
        verify(productoRepository, never()).save(any(Producto.class));
    }

    @Test
    void aumentarStock_ProductoNoEncontrado() {
        // Given
        Long productoId = 999L;
        Integer cantidadAumentar = 10;
        when(productoRepository.findById(productoId)).thenReturn(Optional.empty());

        // When & Then
        Exception exception = assertThrows(RuntimeException.class, () -> {
            productoService.aumentarStock(productoId, cantidadAumentar);
        });

        assertEquals("Producto no encontrado", exception.getMessage());
        verify(productoRepository).findById(productoId);
        verify(productoRepository, never()).save(any(Producto.class));
    }

    @Test
    void tieneStockBajo_ProductoNoEncontrado() {
        // Given
        Long productoId = 999L;
        Integer umbral = 10;
        when(productoRepository.findById(productoId)).thenReturn(Optional.empty());

        // When & Then
        Exception exception = assertThrows(RuntimeException.class, () -> {
            productoService.tieneStockBajo(productoId, umbral);
        });

        assertEquals("Producto no encontrado", exception.getMessage());
        verify(productoRepository).findById(productoId);
    }

    @Test
    void tieneStockBajo_StockNulo() {
        // Given
        Long productoId = 1L;
        Integer umbral = 10;
        Producto productoSinStock = Producto.builder()
                .id(productoId)
                .nombre("El Quijote")
                .stock(null)
                .build();

        when(productoRepository.findById(productoId)).thenReturn(Optional.of(productoSinStock));

        // When
        boolean result = productoService.tieneStockBajo(productoId, umbral);

        // Then
        assertFalse(result); // Stock null should return false
        verify(productoRepository).findById(productoId);
    }

    @Test
    void obtenerStockActual_ProductoNoEncontrado() {
        // Given
        Long productoId = 999L;
        when(productoRepository.findById(productoId)).thenReturn(Optional.empty());

        // When & Then
        Exception exception = assertThrows(RuntimeException.class, () -> {
            productoService.obtenerStockActual(productoId);
        });

        assertEquals("Producto no encontrado", exception.getMessage());
        verify(productoRepository).findById(productoId);
    }
}
