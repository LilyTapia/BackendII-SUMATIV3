package com.letrasypapeles.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservaRequest {
    
    private Long clienteId;
    private Long productoId;
    private Integer cantidad;
    private String estado;
    private LocalDateTime fechaReserva;
}
