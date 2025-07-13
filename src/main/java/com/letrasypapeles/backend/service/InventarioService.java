package com.letrasypapeles.backend.service;

import com.letrasypapeles.backend.entity.Inventario;
import com.letrasypapeles.backend.repository.InventarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class InventarioService {

    @Autowired
    private InventarioRepository inventarioRepository;

    public List<Inventario> obtenerTodos() {
        return inventarioRepository.findAll();
    }

    public Optional<Inventario> obtenerPorId(Long id) {
        return inventarioRepository.findById(id);
    }

    public Inventario guardar(Inventario inventario) {
        return inventarioRepository.save(inventario);
    }

    public void eliminar(Long id) {
        inventarioRepository.deleteById(id);
    }

    public List<Inventario> obtenerPorProductoId(Long productoId) {
        return inventarioRepository.findByProductoId(productoId);
    }

    public List<Inventario> obtenerPorSucursalId(Long sucursalId) {
        return inventarioRepository.findBySucursalId(sucursalId);
    }

    public List<Inventario> obtenerInventarioBajoUmbral(Integer umbral) {
        return inventarioRepository.findByCantidadLessThan(umbral);
    }

    /**
     * Verifica si el inventario está por debajo del umbral mínimo
     * @param inventarioId ID del inventario
     * @return true si está por debajo del umbral, false en caso contrario
     */
    public boolean estaDebajoDeLUmbral(Long inventarioId) {
        Inventario inventario = inventarioRepository.findById(inventarioId)
                .orElseThrow(() -> new RuntimeException("Inventario no encontrado"));

        return inventario.getCantidad() != null && inventario.getUmbral() != null &&
               inventario.getCantidad() < inventario.getUmbral();
    }

    /**
     * Obtiene todos los inventarios que están por debajo del umbral
     * @return Lista de inventarios con stock bajo
     */
    public List<Inventario> obtenerInventariosConStockBajo() {
        List<Inventario> todosLosInventarios = inventarioRepository.findAll();
        return todosLosInventarios.stream()
                .filter(inventario -> inventario.getCantidad() != null &&
                                    inventario.getUmbral() != null &&
                                    inventario.getCantidad() < inventario.getUmbral())
                .collect(Collectors.toList());
    }

    /**
     * Actualiza la cantidad en inventario después de una venta o reserva
     * @param inventarioId ID del inventario
     * @param cantidadReducir Cantidad a reducir
     * @return Inventario actualizado
     */
    public Inventario reducirCantidad(Long inventarioId, Integer cantidadReducir) {
        Inventario inventario = inventarioRepository.findById(inventarioId)
                .orElseThrow(() -> new RuntimeException("Inventario no encontrado"));

        if (inventario.getCantidad() == null || inventario.getCantidad() < cantidadReducir) {
            throw new RuntimeException("Cantidad insuficiente en inventario. Cantidad actual: " +
                (inventario.getCantidad() != null ? inventario.getCantidad() : 0) +
                ", cantidad a reducir: " + cantidadReducir);
        }

        inventario.setCantidad(inventario.getCantidad() - cantidadReducir);
        return inventarioRepository.save(inventario);
    }

    /**
     * Aumenta la cantidad en inventario (restock)
     * @param inventarioId ID del inventario
     * @param cantidadAumentar Cantidad a aumentar
     * @return Inventario actualizado
     */
    public Inventario aumentarCantidad(Long inventarioId, Integer cantidadAumentar) {
        Inventario inventario = inventarioRepository.findById(inventarioId)
                .orElseThrow(() -> new RuntimeException("Inventario no encontrado"));

        if (cantidadAumentar <= 0) {
            throw new RuntimeException("La cantidad a aumentar debe ser mayor a 0");
        }

        int cantidadActual = inventario.getCantidad() != null ? inventario.getCantidad() : 0;
        inventario.setCantidad(cantidadActual + cantidadAumentar);
        return inventarioRepository.save(inventario);
    }

    /**
     * Actualiza el umbral mínimo de un inventario
     * @param inventarioId ID del inventario
     * @param nuevoUmbral Nuevo umbral mínimo
     * @return Inventario actualizado
     */
    public Inventario actualizarUmbral(Long inventarioId, Integer nuevoUmbral) {
        Inventario inventario = inventarioRepository.findById(inventarioId)
                .orElseThrow(() -> new RuntimeException("Inventario no encontrado"));

        if (nuevoUmbral < 0) {
            throw new RuntimeException("El umbral no puede ser negativo");
        }

        inventario.setUmbral(nuevoUmbral);
        return inventarioRepository.save(inventario);
    }

    /**
     * Genera alerta de restock para inventarios con stock bajo
     * @return Lista de mensajes de alerta
     */
    public List<String> generarAlertasDeRestock() {
        List<Inventario> inventariosConStockBajo = obtenerInventariosConStockBajo();
        return inventariosConStockBajo.stream()
                .map(inventario -> String.format(
                    "ALERTA: Producto '%s' en sucursal '%s' tiene stock bajo. " +
                    "Cantidad actual: %d, Umbral: %d",
                    inventario.getProducto() != null ? inventario.getProducto().getNombre() : "Desconocido",
                    inventario.getSucursal() != null ? inventario.getSucursal().getNombre() : "Desconocida",
                    inventario.getCantidad(),
                    inventario.getUmbral()
                ))
                .collect(Collectors.toList());
    }

    /**
     * Verifica si se necesita restock para un inventario específico
     * @param inventarioId ID del inventario
     * @return true si necesita restock, false en caso contrario
     */
    public boolean necesitaRestock(Long inventarioId) {
        return estaDebajoDeLUmbral(inventarioId);
    }
}
