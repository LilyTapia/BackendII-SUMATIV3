package com.letrasypapeles.backend.service;

import com.letrasypapeles.backend.entity.Inventario;
import com.letrasypapeles.backend.entity.Producto;
import com.letrasypapeles.backend.entity.Sucursal;
import com.letrasypapeles.backend.repository.InventarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class InventarioServiceTest {

    @Mock
    private InventarioRepository inventarioRepository;

    @InjectMocks
    private InventarioService inventarioService;

    private Inventario inventario;
    private Producto producto;
    private Sucursal sucursal;

    @BeforeEach
    void setUp() {
        // Configurar datos de prueba
        producto = Producto.builder()
                .id(1L)
                .nombre("El Quijote")
                .build();

        sucursal = Sucursal.builder()
                .id(1L)
                .nombre("Tienda Central")
                .build();

        inventario = Inventario.builder()
                .id(1L)
                .cantidad(50)
                .umbral(10)
                .producto(producto)
                .sucursal(sucursal)
                .build();
    }

    @Test
    void obtenerTodos() {
        // Given
        List<Inventario> inventarios = Arrays.asList(inventario);
        when(inventarioRepository.findAll()).thenReturn(inventarios);

        // When
        List<Inventario> result = inventarioService.obtenerTodos();

        // Then
        assertEquals(1, result.size());
        assertEquals(50, result.get(0).getCantidad());
        verify(inventarioRepository, times(1)).findAll();
    }

    @Test
    void obtenerPorId() {
        // Given
        when(inventarioRepository.findById(1L)).thenReturn(Optional.of(inventario));

        // When
        Optional<Inventario> result = inventarioService.obtenerPorId(1L);

        // Then
        assertTrue(result.isPresent());
        assertEquals(50, result.get().getCantidad());
        assertEquals(10, result.get().getUmbral());
        verify(inventarioRepository, times(1)).findById(1L);
    }

    @Test
    void obtenerPorIdNoExistente() {
        // Given
        when(inventarioRepository.findById(99L)).thenReturn(Optional.empty());

        // When
        Optional<Inventario> result = inventarioService.obtenerPorId(99L);

        // Then
        assertFalse(result.isPresent());
        verify(inventarioRepository, times(1)).findById(99L);
    }

    @Test
    void guardar() {
        // Given
        Inventario inventarioNuevo = Inventario.builder()
                .cantidad(30)
                .umbral(5)
                .producto(producto)
                .sucursal(sucursal)
                .build();

        Inventario inventarioGuardado = Inventario.builder()
                .id(2L)
                .cantidad(30)
                .umbral(5)
                .producto(producto)
                .sucursal(sucursal)
                .build();
        
        when(inventarioRepository.save(any(Inventario.class))).thenReturn(inventarioGuardado);

        // When
        Inventario result = inventarioService.guardar(inventarioNuevo);

        // Then
        assertNotNull(result);
        assertEquals(2L, result.getId());
        assertEquals(30, result.getCantidad());
        assertEquals(5, result.getUmbral());
        verify(inventarioRepository, times(1)).save(any(Inventario.class));
    }

    @Test
    void eliminar() {
        // Given
        Long idToDelete = 1L;
        doNothing().when(inventarioRepository).deleteById(idToDelete);

        // When
        inventarioService.eliminar(idToDelete);

        // Then
        verify(inventarioRepository, times(1)).deleteById(idToDelete);
    }

    @Test
    void obtenerPorProductoId() {
        // Given
        List<Inventario> inventarios = Arrays.asList(inventario);
        when(inventarioRepository.findByProductoId(1L)).thenReturn(inventarios);

        // When
        List<Inventario> result = inventarioService.obtenerPorProductoId(1L);

        // Then
        assertEquals(1, result.size());
        assertEquals(50, result.get(0).getCantidad());
        assertEquals("El Quijote", result.get(0).getProducto().getNombre());
        verify(inventarioRepository, times(1)).findByProductoId(1L);
    }

    @Test
    void obtenerPorSucursalId() {
        // Given
        List<Inventario> inventarios = Arrays.asList(inventario);
        when(inventarioRepository.findBySucursalId(1L)).thenReturn(inventarios);

        // When
        List<Inventario> result = inventarioService.obtenerPorSucursalId(1L);

        // Then
        assertEquals(1, result.size());
        assertEquals(50, result.get(0).getCantidad());
        assertEquals("Tienda Central", result.get(0).getSucursal().getNombre());
        verify(inventarioRepository, times(1)).findBySucursalId(1L);
    }

    @Test
    void obtenerInventarioBajoUmbral() {
        // Given
        List<Inventario> inventarios = Arrays.asList(inventario);
        when(inventarioRepository.findByCantidadLessThan(100)).thenReturn(inventarios);

        // When
        List<Inventario> result = inventarioService.obtenerInventarioBajoUmbral(100);

        // Then
        assertEquals(1, result.size());
        assertEquals(50, result.get(0).getCantidad());
        verify(inventarioRepository, times(1)).findByCantidadLessThan(100);
    }

    @Test
    void estaDebajoDeLUmbral_StockBajo() {
        // Given
        Long inventarioId = 1L;
        Inventario inventarioConStockBajo = Inventario.builder()
                .id(inventarioId)
                .cantidad(5)
                .umbral(10)
                .producto(producto)
                .sucursal(sucursal)
                .build();

        when(inventarioRepository.findById(inventarioId)).thenReturn(Optional.of(inventarioConStockBajo));

        // When
        boolean result = inventarioService.estaDebajoDeLUmbral(inventarioId);

        // Then
        assertTrue(result);
        verify(inventarioRepository, times(1)).findById(inventarioId);
    }

    @Test
    void estaDebajoDeLUmbral_StockSuficiente() {
        // Given
        Long inventarioId = 1L;
        Inventario inventarioConStockSuficiente = Inventario.builder()
                .id(inventarioId)
                .cantidad(15)
                .umbral(10)
                .producto(producto)
                .sucursal(sucursal)
                .build();

        when(inventarioRepository.findById(inventarioId)).thenReturn(Optional.of(inventarioConStockSuficiente));

        // When
        boolean result = inventarioService.estaDebajoDeLUmbral(inventarioId);

        // Then
        assertFalse(result);
        verify(inventarioRepository, times(1)).findById(inventarioId);
    }

    @Test
    void estaDebajoDeLUmbral_CantidadNula() {
        // Given
        Long inventarioId = 1L;
        Inventario inventarioSinCantidad = Inventario.builder()
                .id(inventarioId)
                .cantidad(null)
                .umbral(10)
                .producto(producto)
                .sucursal(sucursal)
                .build();

        when(inventarioRepository.findById(inventarioId)).thenReturn(Optional.of(inventarioSinCantidad));

        // When
        boolean result = inventarioService.estaDebajoDeLUmbral(inventarioId);

        // Then
        assertFalse(result);
        verify(inventarioRepository, times(1)).findById(inventarioId);
    }

    @Test
    void estaDebajoDeLUmbral_InventarioNoExistente() {
        // Given
        Long inventarioId = 999L;
        when(inventarioRepository.findById(inventarioId)).thenReturn(Optional.empty());

        // When & Then
        Exception exception = assertThrows(RuntimeException.class, () -> {
            inventarioService.estaDebajoDeLUmbral(inventarioId);
        });

        assertEquals("Inventario no encontrado", exception.getMessage());
        verify(inventarioRepository, times(1)).findById(inventarioId);
    }

    @Test
    void obtenerInventariosConStockBajo() {
        // Given
        Inventario inventario1 = Inventario.builder()
                .id(1L)
                .cantidad(5)
                .umbral(10)
                .producto(producto)
                .sucursal(sucursal)
                .build();

        Inventario inventario2 = Inventario.builder()
                .id(2L)
                .cantidad(15)
                .umbral(10)
                .producto(producto)
                .sucursal(sucursal)
                .build();

        Inventario inventario3 = Inventario.builder()
                .id(3L)
                .cantidad(3)
                .umbral(10)
                .producto(producto)
                .sucursal(sucursal)
                .build();

        List<Inventario> todosLosInventarios = Arrays.asList(inventario1, inventario2, inventario3);
        when(inventarioRepository.findAll()).thenReturn(todosLosInventarios);

        // When
        List<Inventario> result = inventarioService.obtenerInventariosConStockBajo();

        // Then
        assertEquals(2, result.size()); // Solo inventario1 e inventario3 tienen stock bajo
        assertTrue(result.stream().anyMatch(inv -> inv.getId().equals(1L)));
        assertTrue(result.stream().anyMatch(inv -> inv.getId().equals(3L)));
        assertFalse(result.stream().anyMatch(inv -> inv.getId().equals(2L)));
        verify(inventarioRepository, times(1)).findAll();
    }

    @Test
    void reducirCantidad_Exitoso() {
        // Given
        Long inventarioId = 1L;
        Integer cantidadReducir = 10;
        Inventario inventarioOriginal = Inventario.builder()
                .id(inventarioId)
                .cantidad(50)
                .umbral(10)
                .producto(producto)
                .sucursal(sucursal)
                .build();

        Inventario inventarioActualizado = Inventario.builder()
                .id(inventarioId)
                .cantidad(40)
                .umbral(10)
                .producto(producto)
                .sucursal(sucursal)
                .build();

        when(inventarioRepository.findById(inventarioId)).thenReturn(Optional.of(inventarioOriginal));
        when(inventarioRepository.save(any(Inventario.class))).thenReturn(inventarioActualizado);

        // When
        Inventario result = inventarioService.reducirCantidad(inventarioId, cantidadReducir);

        // Then
        assertNotNull(result);
        assertEquals(40, result.getCantidad());
        verify(inventarioRepository, times(1)).findById(inventarioId);
        verify(inventarioRepository, times(1)).save(any(Inventario.class));
    }

    @Test
    void reducirCantidad_CantidadInsuficiente() {
        // Given
        Long inventarioId = 1L;
        Integer cantidadReducir = 60;
        Inventario inventarioConCantidadBaja = Inventario.builder()
                .id(inventarioId)
                .cantidad(50)
                .umbral(10)
                .producto(producto)
                .sucursal(sucursal)
                .build();

        when(inventarioRepository.findById(inventarioId)).thenReturn(Optional.of(inventarioConCantidadBaja));

        // When & Then
        Exception exception = assertThrows(RuntimeException.class, () -> {
            inventarioService.reducirCantidad(inventarioId, cantidadReducir);
        });

        assertTrue(exception.getMessage().contains("Cantidad insuficiente en inventario"));
        verify(inventarioRepository, times(1)).findById(inventarioId);
        verify(inventarioRepository, never()).save(any(Inventario.class));
    }

    @Test
    void aumentarCantidad_Exitoso() {
        // Given
        Long inventarioId = 1L;
        Integer cantidadAumentar = 20;
        Inventario inventarioOriginal = Inventario.builder()
                .id(inventarioId)
                .cantidad(30)
                .umbral(10)
                .producto(producto)
                .sucursal(sucursal)
                .build();

        Inventario inventarioActualizado = Inventario.builder()
                .id(inventarioId)
                .cantidad(50)
                .umbral(10)
                .producto(producto)
                .sucursal(sucursal)
                .build();

        when(inventarioRepository.findById(inventarioId)).thenReturn(Optional.of(inventarioOriginal));
        when(inventarioRepository.save(any(Inventario.class))).thenReturn(inventarioActualizado);

        // When
        Inventario result = inventarioService.aumentarCantidad(inventarioId, cantidadAumentar);

        // Then
        assertNotNull(result);
        assertEquals(50, result.getCantidad());
        verify(inventarioRepository, times(1)).findById(inventarioId);
        verify(inventarioRepository, times(1)).save(any(Inventario.class));
    }

    @Test
    void aumentarCantidad_CantidadNulaInicial() {
        // Given
        Long inventarioId = 1L;
        Integer cantidadAumentar = 20;
        Inventario inventarioSinCantidad = Inventario.builder()
                .id(inventarioId)
                .cantidad(null)
                .umbral(10)
                .producto(producto)
                .sucursal(sucursal)
                .build();

        Inventario inventarioActualizado = Inventario.builder()
                .id(inventarioId)
                .cantidad(20)
                .umbral(10)
                .producto(producto)
                .sucursal(sucursal)
                .build();

        when(inventarioRepository.findById(inventarioId)).thenReturn(Optional.of(inventarioSinCantidad));
        when(inventarioRepository.save(any(Inventario.class))).thenReturn(inventarioActualizado);

        // When
        Inventario result = inventarioService.aumentarCantidad(inventarioId, cantidadAumentar);

        // Then
        assertNotNull(result);
        assertEquals(20, result.getCantidad());
        verify(inventarioRepository, times(1)).findById(inventarioId);
        verify(inventarioRepository, times(1)).save(any(Inventario.class));
    }

    @Test
    void aumentarCantidad_CantidadInvalida() {
        // Given
        Long inventarioId = 1L;
        Integer cantidadAumentar = -5;
        Inventario inventarioOriginal = Inventario.builder()
                .id(inventarioId)
                .cantidad(30)
                .umbral(10)
                .producto(producto)
                .sucursal(sucursal)
                .build();

        when(inventarioRepository.findById(inventarioId)).thenReturn(Optional.of(inventarioOriginal));

        // When & Then
        Exception exception = assertThrows(RuntimeException.class, () -> {
            inventarioService.aumentarCantidad(inventarioId, cantidadAumentar);
        });

        assertEquals("La cantidad a aumentar debe ser mayor a 0", exception.getMessage());
        verify(inventarioRepository, times(1)).findById(inventarioId);
        verify(inventarioRepository, never()).save(any(Inventario.class));
    }

    @Test
    void actualizarUmbral_Exitoso() {
        // Given
        Long inventarioId = 1L;
        Integer nuevoUmbral = 15;
        Inventario inventarioOriginal = Inventario.builder()
                .id(inventarioId)
                .cantidad(50)
                .umbral(10)
                .producto(producto)
                .sucursal(sucursal)
                .build();

        Inventario inventarioActualizado = Inventario.builder()
                .id(inventarioId)
                .cantidad(50)
                .umbral(15)
                .producto(producto)
                .sucursal(sucursal)
                .build();

        when(inventarioRepository.findById(inventarioId)).thenReturn(Optional.of(inventarioOriginal));
        when(inventarioRepository.save(any(Inventario.class))).thenReturn(inventarioActualizado);

        // When
        Inventario result = inventarioService.actualizarUmbral(inventarioId, nuevoUmbral);

        // Then
        assertNotNull(result);
        assertEquals(15, result.getUmbral());
        verify(inventarioRepository, times(1)).findById(inventarioId);
        verify(inventarioRepository, times(1)).save(any(Inventario.class));
    }

    @Test
    void actualizarUmbral_UmbralNegativo() {
        // Given
        Long inventarioId = 1L;
        Integer nuevoUmbral = -5;
        Inventario inventarioOriginal = Inventario.builder()
                .id(inventarioId)
                .cantidad(50)
                .umbral(10)
                .producto(producto)
                .sucursal(sucursal)
                .build();

        when(inventarioRepository.findById(inventarioId)).thenReturn(Optional.of(inventarioOriginal));

        // When & Then
        Exception exception = assertThrows(RuntimeException.class, () -> {
            inventarioService.actualizarUmbral(inventarioId, nuevoUmbral);
        });

        assertEquals("El umbral no puede ser negativo", exception.getMessage());
        verify(inventarioRepository, times(1)).findById(inventarioId);
        verify(inventarioRepository, never()).save(any(Inventario.class));
    }

    // Missing exception tests for 100% coverage

    @Test
    void reducirCantidad_InventarioNoEncontrado() {
        // Given
        Long inventarioId = 999L;
        Integer cantidadReducir = 10;
        when(inventarioRepository.findById(inventarioId)).thenReturn(Optional.empty());

        // When & Then
        Exception exception = assertThrows(RuntimeException.class, () -> {
            inventarioService.reducirCantidad(inventarioId, cantidadReducir);
        });

        assertEquals("Inventario no encontrado", exception.getMessage());
        verify(inventarioRepository).findById(inventarioId);
        verify(inventarioRepository, never()).save(any(Inventario.class));
    }

    @Test
    void reducirCantidad_CantidadNull() {
        // Given
        Long inventarioId = 1L;
        Integer cantidadReducir = 10;
        Inventario inventarioSinCantidad = Inventario.builder()
                .id(inventarioId)
                .cantidad(null)
                .umbral(10)
                .build();

        when(inventarioRepository.findById(inventarioId)).thenReturn(Optional.of(inventarioSinCantidad));

        // When & Then
        Exception exception = assertThrows(RuntimeException.class, () -> {
            inventarioService.reducirCantidad(inventarioId, cantidadReducir);
        });

        assertTrue(exception.getMessage().contains("Cantidad insuficiente en inventario"));
        verify(inventarioRepository).findById(inventarioId);
        verify(inventarioRepository, never()).save(any(Inventario.class));
    }

    @Test
    void aumentarCantidad_InventarioNoEncontrado() {
        // Given
        Long inventarioId = 999L;
        Integer cantidadAumentar = 10;
        when(inventarioRepository.findById(inventarioId)).thenReturn(Optional.empty());

        // When & Then
        Exception exception = assertThrows(RuntimeException.class, () -> {
            inventarioService.aumentarCantidad(inventarioId, cantidadAumentar);
        });

        assertEquals("Inventario no encontrado", exception.getMessage());
        verify(inventarioRepository).findById(inventarioId);
        verify(inventarioRepository, never()).save(any(Inventario.class));
    }

    @Test
    void actualizarUmbral_InventarioNoEncontrado() {
        // Given
        Long inventarioId = 999L;
        Integer nuevoUmbral = 15;
        when(inventarioRepository.findById(inventarioId)).thenReturn(Optional.empty());

        // When & Then
        Exception exception = assertThrows(RuntimeException.class, () -> {
            inventarioService.actualizarUmbral(inventarioId, nuevoUmbral);
        });

        assertEquals("Inventario no encontrado", exception.getMessage());
        verify(inventarioRepository).findById(inventarioId);
        verify(inventarioRepository, never()).save(any(Inventario.class));
    }

    @Test
    void estaDebajoDeLUmbral_UmbralNull() {
        // Given
        Long inventarioId = 1L;
        Inventario inventarioSinUmbral = Inventario.builder()
                .id(inventarioId)
                .cantidad(5)
                .umbral(null)
                .build();

        when(inventarioRepository.findById(inventarioId)).thenReturn(Optional.of(inventarioSinUmbral));

        // When
        boolean result = inventarioService.estaDebajoDeLUmbral(inventarioId);

        // Then
        assertFalse(result); // Umbral null should return false
        verify(inventarioRepository).findById(inventarioId);
    }

    @Test
    void generarAlertasDeRestock() {
        // Given
        Inventario inventarioStockBajo = Inventario.builder()
                .id(1L)
                .cantidad(5)
                .umbral(10)
                .producto(producto)
                .sucursal(sucursal)
                .build();

        List<Inventario> inventariosConStockBajo = Arrays.asList(inventarioStockBajo);
        when(inventarioRepository.findAll()).thenReturn(inventariosConStockBajo);

        // When
        List<String> alertas = inventarioService.generarAlertasDeRestock();

        // Then
        assertEquals(1, alertas.size());
        assertTrue(alertas.get(0).contains("ALERTA"));
        assertTrue(alertas.get(0).contains("El Quijote"));
        assertTrue(alertas.get(0).contains("Tienda Central"));
        verify(inventarioRepository).findAll();
    }

    @Test
    void generarAlertasDeRestock_ProductoNull() {
        // Given
        Inventario inventarioSinProducto = Inventario.builder()
                .id(1L)
                .cantidad(5)
                .umbral(10)
                .producto(null)
                .sucursal(sucursal)
                .build();

        List<Inventario> inventariosConStockBajo = Arrays.asList(inventarioSinProducto);
        when(inventarioRepository.findAll()).thenReturn(inventariosConStockBajo);

        // When
        List<String> alertas = inventarioService.generarAlertasDeRestock();

        // Then
        assertEquals(1, alertas.size());
        assertTrue(alertas.get(0).contains("Desconocido"));
        verify(inventarioRepository).findAll();
    }

    @Test
    void generarAlertasDeRestock_SucursalNull() {
        // Given
        Inventario inventarioSinSucursal = Inventario.builder()
                .id(1L)
                .cantidad(5)
                .umbral(10)
                .producto(producto)
                .sucursal(null)
                .build();

        List<Inventario> inventariosConStockBajo = Arrays.asList(inventarioSinSucursal);
        when(inventarioRepository.findAll()).thenReturn(inventariosConStockBajo);

        // When
        List<String> alertas = inventarioService.generarAlertasDeRestock();

        // Then
        assertEquals(1, alertas.size());
        assertTrue(alertas.get(0).contains("Desconocida"));
        verify(inventarioRepository).findAll();
    }

    @Test
    void necesitaRestock() {
        // Given
        Long inventarioId = 1L;
        Inventario inventarioConStockBajo = Inventario.builder()
                .id(inventarioId)
                .cantidad(5)
                .umbral(10)
                .build();

        when(inventarioRepository.findById(inventarioId)).thenReturn(Optional.of(inventarioConStockBajo));

        // When
        boolean result = inventarioService.necesitaRestock(inventarioId);

        // Then
        assertTrue(result);
        verify(inventarioRepository).findById(inventarioId);
    }
}
