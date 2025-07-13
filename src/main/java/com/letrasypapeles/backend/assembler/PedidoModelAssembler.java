package com.letrasypapeles.backend.assembler;

import com.letrasypapeles.backend.controller.PedidoController;
import com.letrasypapeles.backend.controller.ClienteController;
import com.letrasypapeles.backend.controller.ProductoController;
import com.letrasypapeles.backend.entity.Pedido;
import com.letrasypapeles.backend.entity.Producto;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
public class PedidoModelAssembler implements RepresentationModelAssembler<Pedido, EntityModel<Pedido>> {

    @Override
    public EntityModel<Pedido> toModel(Pedido pedido) {
        EntityModel<Pedido> pedidoModel = EntityModel.of(pedido);

        // Self link
        pedidoModel.add(linkTo(methodOn(PedidoController.class)
                .obtenerPorId(pedido.getId())).withSelfRel());

        // Link to all orders
        pedidoModel.add(linkTo(PedidoController.class).withRel("pedidos"));

        // Link to client
        if (pedido.getCliente() != null) {
            pedidoModel.add(linkTo(methodOn(ClienteController.class)
                    .obtenerPorId(pedido.getCliente().getId())).withRel("cliente"));
        }

        // Links to products in the order
        if (pedido.getListaProductos() != null && !pedido.getListaProductos().isEmpty()) {
            pedidoModel.add(linkTo(ProductoController.class).withRel("productos"));
            
            // Add individual product links
            for (Producto producto : pedido.getListaProductos()) {
                pedidoModel.add(linkTo(methodOn(ProductoController.class)
                        .obtenerPorId(producto.getId())).withRel("producto-" + producto.getId()));
            }
        }

        // Link to update order
        pedidoModel.add(linkTo(methodOn(PedidoController.class)
                .actualizarPedido(pedido.getId(), pedido)).withRel("update"));

        // Link to delete order
        pedidoModel.add(linkTo(methodOn(PedidoController.class)
                .eliminarPedido(pedido.getId())).withRel("delete"));

        return pedidoModel;
    }
}
