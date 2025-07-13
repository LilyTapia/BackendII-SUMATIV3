package com.letrasypapeles.backend.repository;

import com.letrasypapeles.backend.entity.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    List<Reserva> findByClienteId(Long clienteId);

    List<Reserva> findByProductoId(Long productoId);

    List<Reserva> findByEstado(String estado);

    @Query("SELECT r FROM Reserva r LEFT JOIN FETCH r.cliente LEFT JOIN FETCH r.producto WHERE r.id = :id")
    Optional<Reserva> findByIdWithRelations(@Param("id") Long id);
}
