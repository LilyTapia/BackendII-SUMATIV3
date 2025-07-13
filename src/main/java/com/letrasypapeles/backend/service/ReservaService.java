package com.letrasypapeles.backend.service;

import com.letrasypapeles.backend.dto.ReservaRequest;
import com.letrasypapeles.backend.entity.Reserva;
import com.letrasypapeles.backend.entity.Producto;
import com.letrasypapeles.backend.entity.Cliente;
import com.letrasypapeles.backend.repository.ReservaRepository;
import com.letrasypapeles.backend.repository.ProductoRepository;
import com.letrasypapeles.backend.repository.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ReservaService {

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    public List<Reserva> obtenerTodas() {
        return reservaRepository.findAll();
    }

    public Optional<Reserva> obtenerPorId(Long id) {
        return reservaRepository.findById(id);
    }

    public Reserva guardar(Reserva reserva) {
        // Resolve cliente relationship if only ID is provided (incomplete object)
        if (reserva.getCliente() != null && reserva.getCliente().getId() != null &&
            (reserva.getCliente().getNombre() == null || reserva.getCliente().getEmail() == null)) {
            Cliente cliente = clienteRepository.findById(reserva.getCliente().getId())
                    .orElseThrow(() -> new RuntimeException("Cliente no encontrado con ID: " + reserva.getCliente().getId()));
            reserva.setCliente(cliente);
        }

        // Resolve producto relationship if only ID is provided (incomplete object)
        if (reserva.getProducto() != null && reserva.getProducto().getId() != null &&
            (reserva.getProducto().getNombre() == null || reserva.getProducto().getPrecio() == null)) {
            Producto producto = productoRepository.findById(reserva.getProducto().getId())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + reserva.getProducto().getId()));
            reserva.setProducto(producto);
        }

        // Set fecha if not provided
        if (reserva.getFechaReserva() == null) {
            reserva.setFechaReserva(LocalDateTime.now());
        }

        // Set default estado if not provided
        if (reserva.getEstado() == null || reserva.getEstado().trim().isEmpty()) {
            reserva.setEstado("PENDIENTE");
        }

        // Set default cantidad if not provided
        if (reserva.getCantidad() == null) {
            reserva.setCantidad(1);
        }

        return reservaRepository.save(reserva);
    }

    public Reserva crearDesdeReservaRequest(ReservaRequest reservaRequest) {
        // Fetch cliente
        Cliente cliente = clienteRepository.findById(reservaRequest.getClienteId())
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con ID: " + reservaRequest.getClienteId()));

        // Fetch producto
        Producto producto = productoRepository.findById(reservaRequest.getProductoId())
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + reservaRequest.getProductoId()));

        // Create Reserva
        Reserva reserva = Reserva.builder()
                .cliente(cliente)
                .producto(producto)
                .cantidad(reservaRequest.getCantidad() != null ? reservaRequest.getCantidad() : 1)
                .estado(reservaRequest.getEstado() != null ? reservaRequest.getEstado() : "PENDIENTE")
                .fechaReserva(reservaRequest.getFechaReserva() != null ? reservaRequest.getFechaReserva() : LocalDateTime.now())
                .build();

        return reservaRepository.save(reserva);
    }

    public void eliminar(Long id) {
        reservaRepository.deleteById(id);
    }

    public List<Reserva> obtenerPorClienteId(Long clienteId) {
        return reservaRepository.findByClienteId(clienteId);
    }

    public List<Reserva> obtenerPorProductoId(Long productoId) {
        return reservaRepository.findByProductoId(productoId);
    }

    public List<Reserva> obtenerPorEstado(String estado) {
        return reservaRepository.findByEstado(estado);
    }

    public Optional<Reserva> obtenerPorIdConRelaciones(Long id) {
        return reservaRepository.findByIdWithRelations(id);
    }

    /**
     * Crea una nueva reserva validando que haya stock suficiente
     * @param clienteId ID del cliente
     * @param productoId ID del producto
     * @param cantidad Cantidad a reservar (por defecto 1)
     * @return Reserva creada
     */
    public Reserva crearReservaConValidacion(Long clienteId, Long productoId, Integer cantidad) {
        // Validar que el cliente existe
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        // Validar que el producto existe
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        // Validar stock disponible
        if (producto.getStock() == null || producto.getStock() < cantidad) {
            throw new RuntimeException("Stock insuficiente. Stock disponible: " +
                (producto.getStock() != null ? producto.getStock() : 0) +
                ", cantidad solicitada: " + cantidad);
        }

        // Crear la reserva
        Reserva reserva = Reserva.builder()
                .cliente(cliente)
                .producto(producto)
                .fechaReserva(LocalDateTime.now())
                .estado("PENDIENTE")
                .build();

        return reservaRepository.save(reserva);
    }

    /**
     * Confirma una reserva y reduce el stock del producto
     * @param reservaId ID de la reserva
     * @return Reserva confirmada
     */
    public Reserva confirmarReserva(Long reservaId) {
        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada"));

        if (!"PENDIENTE".equals(reserva.getEstado())) {
            throw new RuntimeException("Solo se pueden confirmar reservas en estado PENDIENTE");
        }

        // Validar stock nuevamente por si cambió
        Producto producto = reserva.getProducto();
        if (producto.getStock() == null || producto.getStock() < 1) {
            throw new RuntimeException("Stock insuficiente para confirmar la reserva");
        }

        // Reducir stock
        producto.setStock(producto.getStock() - 1);
        productoRepository.save(producto);

        // Actualizar estado de la reserva
        reserva.setEstado("CONFIRMADA");
        return reservaRepository.save(reserva);
    }

    /**
     * Cancela una reserva
     * @param reservaId ID de la reserva
     * @return Reserva cancelada
     */
    public Reserva cancelarReserva(Long reservaId) {
        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada"));

        if ("CONFIRMADA".equals(reserva.getEstado())) {
            throw new RuntimeException("No se puede cancelar una reserva ya confirmada");
        }

        reserva.setEstado("CANCELADA");
        return reservaRepository.save(reserva);
    }

    /**
     * Valida si se puede crear una reserva para un producto específico
     * @param productoId ID del producto
     * @param cantidad Cantidad a reservar
     * @return true si se puede reservar, false en caso contrario
     */
    public boolean puedeReservar(Long productoId, Integer cantidad) {
        Producto producto = productoRepository.findById(productoId)
                .orElse(null);

        if (producto == null) {
            return false;
        }

        return producto.getStock() != null && producto.getStock() >= cantidad;
    }

    /**
     * Obtiene el número de reservas pendientes para un producto
     * @param productoId ID del producto
     * @return Número de reservas pendientes
     */
    public long contarReservasPendientes(Long productoId) {
        return reservaRepository.findByProductoId(productoId).stream()
                .filter(reserva -> "PENDIENTE".equals(reserva.getEstado()))
                .count();
    }
}
