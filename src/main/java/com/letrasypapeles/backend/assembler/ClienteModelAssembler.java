package com.letrasypapeles.backend.assembler;

import com.letrasypapeles.backend.controller.ClienteController;
import com.letrasypapeles.backend.controller.PedidoController;
import com.letrasypapeles.backend.controller.ReservaController;
import com.letrasypapeles.backend.entity.Cliente;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
public class ClienteModelAssembler implements RepresentationModelAssembler<Cliente, EntityModel<Cliente>> {

    @Override
    public EntityModel<Cliente> toModel(Cliente cliente) {
        EntityModel<Cliente> clienteModel = EntityModel.of(cliente);

        // Self link
        clienteModel.add(linkTo(methodOn(ClienteController.class)
                .obtenerPorId(cliente.getId())).withSelfRel());

        // Link to all clients
        clienteModel.add(linkTo(ClienteController.class).withRel("clientes"));

        // Link to client's orders
        clienteModel.add(linkTo(methodOn(PedidoController.class)
                .obtenerPorClienteId(cliente.getId())).withRel("pedidos"));

        // Link to client's reservations
        clienteModel.add(linkTo(ReservaController.class).withRel("reservas"));

        // Link to update client
        clienteModel.add(linkTo(methodOn(ClienteController.class)
                .actualizarCliente(cliente.getId(), cliente)).withRel("update"));

        // Link to delete client
        clienteModel.add(linkTo(methodOn(ClienteController.class)
                .eliminarCliente(cliente.getId())).withRel("delete"));

        return clienteModel;
    }
}
