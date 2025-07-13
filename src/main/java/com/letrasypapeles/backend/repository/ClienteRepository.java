package com.letrasypapeles.backend.repository;

import com.letrasypapeles.backend.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    @Query("SELECT c FROM Cliente c LEFT JOIN FETCH c.roles WHERE c.email = :email")
    Optional<Cliente> findByEmail(@Param("email") String email);

    boolean existsByEmail(String email);

    @Modifying
    @Transactional
    @Query("UPDATE Cliente c SET c.puntosFidelidad = c.puntosFidelidad + :puntos WHERE c.id = :clienteId")
    void actualizarPuntosFidelidad(@Param("clienteId") Long clienteId, @Param("puntos") Integer puntos);

    @Query("SELECT c.puntosFidelidad FROM Cliente c WHERE c.id = :clienteId")
    Integer obtenerPuntosFidelidad(@Param("clienteId") Long clienteId);
}
