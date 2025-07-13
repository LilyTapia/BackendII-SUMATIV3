package com.letrasypapeles.backend.service;

import com.letrasypapeles.backend.dto.PedidoRequest;
import com.letrasypapeles.backend.entity.Cliente;
import com.letrasypapeles.backend.entity.Pedido;
import com.letrasypapeles.backend.entity.Producto;
import com.letrasypapeles.backend.repository.ClienteRepository;
import com.letrasypapeles.backend.repository.PedidoRepository;
import com.letrasypapeles.backend.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class PedidoService {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private ProductoRepository productoRepository;

    public List<Pedido> obtenerTodos() {
        return pedidoRepository.findAll();
    }

    public Optional<Pedido> obtenerPorId(Long id) {
        return pedidoRepository.findById(id);
    }

    public Pedido guardar(Pedido pedido) {
        // Resolve cliente relationship if only ID is provided (incomplete object)
        if (pedido.getCliente() != null && pedido.getCliente().getId() != null &&
            (pedido.getCliente().getNombre() == null || pedido.getCliente().getEmail() == null)) {
            Cliente cliente = clienteRepository.findById(pedido.getCliente().getId())
                    .orElseThrow(() -> new RuntimeException("Cliente no encontrado con ID: " + pedido.getCliente().getId()));
            pedido.setCliente(cliente);
        }

        // Resolve productos relationships if only IDs are provided (incomplete objects)
        if (pedido.getListaProductos() != null && !pedido.getListaProductos().isEmpty()) {
            List<Producto> productosCompletos = new ArrayList<>();
            for (Producto producto : pedido.getListaProductos()) {
                if (producto.getId() != null &&
                    (producto.getNombre() == null || producto.getPrecio() == null)) {
                    Producto productoCompleto = productoRepository.findById(producto.getId())
                            .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + producto.getId()));
                    productosCompletos.add(productoCompleto);
                } else {
                    productosCompletos.add(producto);
                }
            }
            pedido.setListaProductos(productosCompletos);
        }

        // Set fecha if not provided
        if (pedido.getFecha() == null) {
            pedido.setFecha(LocalDateTime.now());
        }

        // Set default estado if not provided
        if (pedido.getEstado() == null || pedido.getEstado().trim().isEmpty()) {
            pedido.setEstado("PENDIENTE");
        }

        return pedidoRepository.save(pedido);
    }

    public void eliminar(Long id) {
        pedidoRepository.deleteById(id);
    }

    public List<Pedido> obtenerPorClienteId(Long clienteId) {
        return pedidoRepository.findByClienteId(clienteId);
    }

    public List<Pedido> obtenerPorEstado(String estado) {
        return pedidoRepository.findByEstado(estado);
    }

    public Pedido crearDesdePedidoRequest(PedidoRequest pedidoRequest) {
        // Fetch cliente
        Cliente cliente = clienteRepository.findById(pedidoRequest.getClienteId())
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con ID: " + pedidoRequest.getClienteId()));

        // Fetch productos
        List<Producto> productos = new ArrayList<>();
        if (pedidoRequest.getProductosIds() != null && !pedidoRequest.getProductosIds().isEmpty()) {
            for (Long productoId : pedidoRequest.getProductosIds()) {
                Producto producto = productoRepository.findById(productoId)
                        .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + productoId));
                productos.add(producto);
            }
        }

        // Create Pedido
        Pedido pedido = Pedido.builder()
                .cliente(cliente)
                .listaProductos(productos)
                .estado(pedidoRequest.getEstado() != null ? pedidoRequest.getEstado() : "PENDIENTE")
                .fecha(pedidoRequest.getFecha() != null ? pedidoRequest.getFecha() : LocalDateTime.now())
                .build();

        return pedidoRepository.save(pedido);
    }
}
