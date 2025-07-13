package com.letrasypapeles.backend.assembler;

import com.letrasypapeles.backend.controller.CategoriaController;
import com.letrasypapeles.backend.controller.ProductoController;
import com.letrasypapeles.backend.entity.Categoria;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
public class CategoriaModelAssembler implements RepresentationModelAssembler<Categoria, EntityModel<Categoria>> {

    @Override
    public EntityModel<Categoria> toModel(Categoria categoria) {
        EntityModel<Categoria> categoriaModel = EntityModel.of(categoria);

        // Self link
        categoriaModel.add(linkTo(methodOn(CategoriaController.class)
                .obtenerPorId(categoria.getId())).withSelfRel());

        // Link to all categories
        categoriaModel.add(linkTo(CategoriaController.class).withRel("categorias"));

        // Link to products in this category
        categoriaModel.add(linkTo(ProductoController.class).withRel("productos"));

        // Link to update category
        categoriaModel.add(linkTo(methodOn(CategoriaController.class)
                .actualizarCategoria(categoria.getId(), categoria)).withRel("update"));

        // Link to delete category
        categoriaModel.add(linkTo(methodOn(CategoriaController.class)
                .eliminarCategoria(categoria.getId())).withRel("delete"));

        return categoriaModel;
    }
}
