package com.letrasypapeles.backend.assembler;

import com.letrasypapeles.backend.controller.ProductoController;
import com.letrasypapeles.backend.controller.CategoriaController;
import com.letrasypapeles.backend.entity.Producto;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
public class ProductoModelAssembler implements RepresentationModelAssembler<Producto, EntityModel<Producto>> {

    @Override
    public EntityModel<Producto> toModel(Producto producto) {
        EntityModel<Producto> productoModel = EntityModel.of(producto);

        // Self link
        productoModel.add(linkTo(methodOn(ProductoController.class)
                .obtenerPorId(producto.getId())).withSelfRel());

        // Link to all products
        productoModel.add(linkTo(ProductoController.class).withRel("productos"));

        // Link to category if exists
        if (producto.getCategoria() != null) {
            productoModel.add(linkTo(methodOn(CategoriaController.class)
                    .obtenerPorId(producto.getCategoria().getId())).withRel("categoria"));
        }

        // Link to update product
        productoModel.add(linkTo(methodOn(ProductoController.class)
                .actualizarProducto(producto.getId(), producto)).withRel("update"));

        // Link to delete product
        productoModel.add(linkTo(methodOn(ProductoController.class)
                .eliminarProducto(producto.getId())).withRel("delete"));

        return productoModel;
    }
}
