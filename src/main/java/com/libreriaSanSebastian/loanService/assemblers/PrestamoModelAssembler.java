package com.libreriaSanSebastian.loanService.assemblers;

import com.libreriaSanSebastian.loanService.controller.PrestamoController;
import com.libreriaSanSebastian.loanService.modelo.Prestamo;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
public class PrestamoModelAssembler implements RepresentationModelAssembler<Prestamo, EntityModel<Prestamo>> {

    @Override
    public EntityModel<Prestamo> toModel(Prestamo prestamo) {
        EntityModel<Prestamo> prestamoModel = EntityModel.of(prestamo,
                linkTo(methodOn(PrestamoController.class).obtenerPorId(prestamo.getId())).withSelfRel(),
                linkTo(methodOn(PrestamoController.class).listarTodos()).withRel("prestamos"));

        // Agregar enlace de devolución solo si el préstamo está activo
        if ("ACTIVO".equals(prestamo.getEstado())) {
            prestamoModel.add(linkTo(methodOn(PrestamoController.class).devolverLibro(prestamo.getId())).withRel("devolver"));
        }

        // Agregar enlace para obtener préstamos del usuario
        prestamoModel.add(linkTo(methodOn(PrestamoController.class).obtenerPorUsuario(prestamo.getUsuarioId())).withRel("prestamos-usuario"));

        // Agregar enlace para eliminar
        prestamoModel.add(linkTo(methodOn(PrestamoController.class).eliminar(prestamo.getId())).withRel("delete"));

        return prestamoModel;
    }
}