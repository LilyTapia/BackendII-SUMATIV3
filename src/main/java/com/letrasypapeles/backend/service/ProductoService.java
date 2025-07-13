package com.letrasypapeles.backend.service;

import com.letrasypapeles.backend.entity.Producto;
import com.letrasypapeles.backend.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductoService {

    @Autowired
    private ProductoRepository productoRepository;

    public List<Producto> obtenerTodos() {
        return productoRepository.findAll();
    }

    public Optional<Producto> obtenerPorId(Long id) {
        return productoRepository.findById(id);
    }

    public Producto guardar(Producto producto) {
        return productoRepository.save(producto);
    }

    public void eliminar(Long id) {
        productoRepository.deleteById(id);
    }

    /**
     * Valida si hay suficiente stock para una cantidad solicitada
     * @param productoId ID del producto
     * @param cantidadSolicitada Cantidad solicitada
     * @return true si hay suficiente stock, false en caso contrario
     */
    public boolean validarStockDisponible(Long productoId, Integer cantidadSolicitada) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        return producto.getStock() != null && producto.getStock() >= cantidadSolicitada;
    }

    /**
     * Reduce el stock de un producto después de una reserva o venta
     * @param productoId ID del producto
     * @param cantidadReducir Cantidad a reducir del stock
     * @return Producto actualizado
     */
    public Producto reducirStock(Long productoId, Integer cantidadReducir) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        if (producto.getStock() == null || producto.getStock() < cantidadReducir) {
            throw new RuntimeException("Stock insuficiente. Stock actual: " +
                (producto.getStock() != null ? producto.getStock() : 0) +
                ", cantidad solicitada: " + cantidadReducir);
        }

        producto.setStock(producto.getStock() - cantidadReducir);
        return productoRepository.save(producto);
    }

    /**
     * Aumenta el stock de un producto (por ejemplo, cuando llega nueva mercancía)
     * @param productoId ID del producto
     * @param cantidadAumentar Cantidad a aumentar al stock
     * @return Producto actualizado
     */
    public Producto aumentarStock(Long productoId, Integer cantidadAumentar) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        if (cantidadAumentar <= 0) {
            throw new RuntimeException("La cantidad a aumentar debe ser mayor a 0");
        }

        int stockActual = producto.getStock() != null ? producto.getStock() : 0;
        producto.setStock(stockActual + cantidadAumentar);
        return productoRepository.save(producto);
    }

    /**
     * Verifica si un producto tiene stock bajo (menor al umbral especificado)
     * @param productoId ID del producto
     * @param umbral Umbral mínimo de stock
     * @return true si el stock está bajo, false en caso contrario
     */
    public boolean tieneStockBajo(Long productoId, Integer umbral) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        return producto.getStock() != null && producto.getStock() < umbral;
    }

    /**
     * Obtiene el stock actual de un producto
     * @param productoId ID del producto
     * @return Stock actual del producto
     */
    public Integer obtenerStockActual(Long productoId) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        return producto.getStock() != null ? producto.getStock() : 0;
    }
}
