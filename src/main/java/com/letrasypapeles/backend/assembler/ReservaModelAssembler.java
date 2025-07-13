package com.letrasypapeles.backend.assembler;

import com.letrasypapeles.backend.controller.ReservaController;
import com.letrasypapeles.backend.controller.ClienteController;
import com.letrasypapeles.backend.controller.ProductoController;
import com.letrasypapeles.backend.entity.Reserva;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
public class ReservaModelAssembler implements RepresentationModelAssembler<Reserva, EntityModel<Reserva>> {

    @Override
    public EntityModel<Reserva> toModel(Reserva reserva) {
        EntityModel<Reserva> reservaModel = EntityModel.of(reserva);

        // Self link
        reservaModel.add(linkTo(methodOn(ReservaController.class)
                .obtenerPorId(reserva.getId())).withSelfRel());

        // Link to all reservations
        reservaModel.add(linkTo(ReservaController.class).withRel("reservas"));

        // Link to client
        if (reserva.getCliente() != null) {
            reservaModel.add(linkTo(methodOn(ClienteController.class)
                    .obtenerPorId(reserva.getCliente().getId())).withRel("cliente"));
        }

        // Link to product
        if (reserva.getProducto() != null) {
            reservaModel.add(linkTo(methodOn(ProductoController.class)
                    .obtenerPorId(reserva.getProducto().getId())).withRel("producto"));
        }

        // Link to update reservation
        reservaModel.add(linkTo(methodOn(ReservaController.class)
                .actualizarReserva(reserva.getId(), reserva)).withRel("update"));

        // Link to delete reservation
        reservaModel.add(linkTo(methodOn(ReservaController.class)
                .eliminarReserva(reserva.getId())).withRel("delete"));

        // Conditional action links based on reservation state
        if ("PENDIENTE".equals(reserva.getEstado())) {
            reservaModel.add(linkTo(methodOn(ReservaController.class)
                    .confirmarReserva(reserva.getId())).withRel("confirmar"));
            reservaModel.add(linkTo(methodOn(ReservaController.class)
                    .cancelarReserva(reserva.getId())).withRel("cancelar"));
        }

        return reservaModel;
    }
}
