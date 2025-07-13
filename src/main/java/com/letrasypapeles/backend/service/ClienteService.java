package com.letrasypapeles.backend.service;

import com.letrasypapeles.backend.entity.Cliente;
import com.letrasypapeles.backend.entity.Role;
import com.letrasypapeles.backend.repository.ClienteRepository;
import com.letrasypapeles.backend.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<Cliente> obtenerTodos() {
        return clienteRepository.findAll();
    }

    public List<Cliente> obtenerTodosLosClientes() {
        return clienteRepository.findAll();
    }

    public Optional<Cliente> obtenerPorId(Long id) {
        return clienteRepository.findById(id);
    }

    public Cliente obtenerClientePorId(Long id) {
        return clienteRepository.findById(id).orElse(null);
    }

    public Optional<Cliente> obtenerPorEmail(String email) {
        return clienteRepository.findByEmail(email);
    }

    public Cliente registrarCliente(Cliente cliente) {
        if (clienteRepository.existsByEmail(cliente.getEmail())) {
            throw new RuntimeException("El correo electrónico ya está registrado.");
        }
        cliente.setContraseña(passwordEncoder.encode(cliente.getContraseña()));
        cliente.setPuntosFidelidad(0);

        // Asignar el rol "CLIENTE"
        Role roleCliente = roleRepository.findByNombre("CLIENTE")
                .orElseGet(() -> {
                    Role newRole = new Role();
                    newRole.setNombre("CLIENTE");
                    return newRole;
                });
        cliente.setRoles(Collections.singleton(roleCliente));

        return clienteRepository.save(cliente);
    }

    public Cliente actualizarCliente(Cliente cliente) {
        return clienteRepository.save(cliente);
    }

    public void eliminar(Long id) {
        clienteRepository.deleteById(id);
    }

    /**
     * Acumula puntos de fidelidad para un cliente basado en el monto de compra
     * @param clienteId ID del cliente
     * @param montoCompra Monto de la compra
     * @return Cliente actualizado con los nuevos puntos
     */
    public Cliente acumularPuntosFidelidad(Long clienteId, Double montoCompra) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        // Regla de negocio: 1 punto por cada $10 de compra
        int puntosGanados = (int) (montoCompra / 10);
        int puntosActuales = cliente.getPuntosFidelidad() != null ? cliente.getPuntosFidelidad() : 0;
        cliente.setPuntosFidelidad(puntosActuales + puntosGanados);

        return clienteRepository.save(cliente);
    }

    /**
     * Valida si un cliente tiene un rol específico
     * @param clienteId ID del cliente
     * @param nombreRole Nombre del rol a validar
     * @return true si el cliente tiene el rol, false en caso contrario
     */
    public boolean tieneRole(Long clienteId, String nombreRole) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        return cliente.getRoles().stream()
                .anyMatch(role -> role.getNombre().equals(nombreRole));
    }

    /**
     * Asigna un rol adicional a un cliente
     * @param clienteId ID del cliente
     * @param nombreRole Nombre del rol a asignar
     * @return Cliente actualizado
     */
    public Cliente asignarRole(Long clienteId, String nombreRole) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        Role role = roleRepository.findByNombre(nombreRole)
                .orElseThrow(() -> new RuntimeException("Role no encontrado: " + nombreRole));

        cliente.addRole(role);
        return clienteRepository.save(cliente);
    }

    /**
     * Remueve un rol de un cliente
     * @param clienteId ID del cliente
     * @param nombreRole Nombre del rol a remover
     * @return Cliente actualizado
     */
    public Cliente removerRole(Long clienteId, String nombreRole) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        Role role = roleRepository.findByNombre(nombreRole)
                .orElseThrow(() -> new RuntimeException("Role no encontrado: " + nombreRole));

        cliente.removeRole(role);
        return clienteRepository.save(cliente);
    }

    /**
     * Actualiza los puntos de fidelidad de un cliente usando una consulta directa
     * @param clienteId ID del cliente
     * @param puntos Puntos a agregar
     */
    public void actualizarPuntosFidelidadDirecto(Long clienteId, Integer puntos) {
        clienteRepository.actualizarPuntosFidelidad(clienteId, puntos);
    }
}
