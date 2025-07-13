package com.letrasypapeles.backend.controller;

import com.letrasypapeles.backend.assembler.CategoriaModelAssembler;
import com.letrasypapeles.backend.entity.Categoria;
import com.letrasypapeles.backend.service.CategoriaService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/categorias")
@Tag(name = "Categorías", description = "Operaciones para gestionar categorías de productos")
public class CategoriaController {

    @Autowired
    private CategoriaService categoriaService;

    @Autowired
    private CategoriaModelAssembler categoriaModelAssembler;

    @GetMapping
    @Operation(summary = "Obtiene todas las categorías", description = "Devuelve la lista completa de categorías registradas")
    @ApiResponse(responseCode = "200", description = "Categorías recuperadas exitosamente")
    public ResponseEntity<CollectionModel<EntityModel<Categoria>>> obtenerTodas() {
        List<Categoria> categorias = categoriaService.obtenerTodas();
        List<EntityModel<Categoria>> categoriasModel = categorias.stream()
                .map(categoriaModelAssembler::toModel)
                .collect(Collectors.toList());

        CollectionModel<EntityModel<Categoria>> collectionModel = CollectionModel.of(categoriasModel);
        collectionModel.add(linkTo(CategoriaController.class).withSelfRel());

        return ResponseEntity.ok(collectionModel);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtiene una categoría por ID", description = "Devuelve la categoría asociada al ID proporcionado")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Categoría recuperada exitosamente"),
        @ApiResponse(responseCode = "404", description = "Categoría no encontrada")
    })
    public ResponseEntity<EntityModel<Categoria>> obtenerPorId(@PathVariable Long id) {
        return categoriaService.obtenerPorId(id)
                .map(categoriaModelAssembler::toModel)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Crea una nueva categoría", description = "Guarda una nueva categoría en el sistema")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Categoría creada exitosamente"),
        @ApiResponse(responseCode = "400", description = "Solicitud inválida")
    })
    public ResponseEntity<EntityModel<Categoria>> crearCategoria(@RequestBody Categoria categoria) {
        Categoria nuevaCategoria = categoriaService.guardar(categoria);
        EntityModel<Categoria> categoriaModel = categoriaModelAssembler.toModel(nuevaCategoria);
        return ResponseEntity.ok(categoriaModel);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualiza una categoría existente", description = "Modifica una categoría con el ID proporcionado")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Categoría actualizada exitosamente"),
        @ApiResponse(responseCode = "404", description = "Categoría no encontrada")
    })
    public ResponseEntity<EntityModel<Categoria>> actualizarCategoria(@PathVariable Long id, @RequestBody Categoria categoria) {
        return categoriaService.obtenerPorId(id)
                .map(c -> {
                    categoria.setId(id);
                    Categoria actualizada = categoriaService.guardar(categoria);
                    EntityModel<Categoria> categoriaModel = categoriaModelAssembler.toModel(actualizada);
                    return ResponseEntity.ok(categoriaModel);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Elimina una categoría", description = "Elimina la categoría con el ID especificado")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Categoría eliminada exitosamente"),
        @ApiResponse(responseCode = "404", description = "Categoría no encontrada")
    })
    public ResponseEntity<Void> eliminarCategoria(@PathVariable Long id) {
        return categoriaService.obtenerPorId(id)
                .map(c -> {
                    categoriaService.eliminar(id);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
