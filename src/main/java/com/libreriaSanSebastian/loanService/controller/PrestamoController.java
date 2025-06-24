package com.libreriaSanSebastian.loanService.controller;

import com.libreriaSanSebastian.loanService.modelo.Prestamo;
import com.libreriaSanSebastian.loanService.service.PrestamoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/prestamos")
@Tag(name = "Préstamos", description = "Operaciones relacionadas con la gestión de préstamos de libros")
public class PrestamoController {

    @Autowired
    private PrestamoService prestamoService;

    @Operation(
        summary = "Listar todos los préstamos",
        description = "Obtiene una lista completa de todos los préstamos registrados en el sistema"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Lista de préstamos obtenida exitosamente",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = Prestamo.class))
    )
    @GetMapping
    public List<Prestamo> listarTodos() {
        return prestamoService.listarTodos();
    }

    @Operation(
        summary = "Obtener préstamo por ID",
        description = "Busca y retorna un préstamo específico por su identificador único"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Préstamo encontrado exitosamente",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Prestamo.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Préstamo no encontrado",
            content = @Content
        )
    })
    @GetMapping("/{id}")
    public ResponseEntity<Prestamo> obtenerPorId(
            @Parameter(description = "ID único del préstamo", required = true, example = "1")
            @PathVariable Long id) {
        return prestamoService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
        summary = "Obtener préstamos por usuario",
        description = "Busca y retorna todos los préstamos asociados a un usuario específico"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de préstamos del usuario obtenida exitosamente",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Prestamo.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Usuario no encontrado",
            content = @Content
        )
    })
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<Prestamo>> obtenerPorUsuario(
            @Parameter(description = "ID único del usuario", required = true, example = "1")
            @PathVariable Long usuarioId) {
        List<Prestamo> prestamos = prestamoService.buscarPorUsuario(usuarioId);
        return ResponseEntity.ok(prestamos);
    }

    @Operation(
        summary = "Crear nuevo préstamo",
        description = "Registra un nuevo préstamo de libro, verificando la disponibilidad del libro y la existencia del usuario"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Préstamo creado exitosamente",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Prestamo.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Datos de entrada inválidos o libro no disponible",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Error interno del servidor",
            content = @Content(mediaType = "application/json")
        )
    })
    @PostMapping
    public ResponseEntity<?> crearPrestamo(
            @Parameter(description = "Datos del préstamo a crear", required = true)
            @RequestBody Prestamo prestamo) {
        try {
            if (prestamo.getUsuarioId() == null || prestamo.getLibroId() == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Usuario ID y Libro ID son requeridos"));
            }

            Prestamo nuevoPrestamo = prestamoService.crearPrestamo(prestamo);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevoPrestamo);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error interno del servidor: " + e.getMessage()));
        }
    }

    @Operation(
        summary = "Devolver libro prestado",
        description = "Marca un préstamo como devuelto y actualiza la fecha de devolución"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Libro devuelto exitosamente",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Prestamo.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "El libro ya fue devuelto o préstamo no válido",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Préstamo no encontrado",
            content = @Content(mediaType = "application/json")
        )
    })
    @PatchMapping("/devolver/{id}")
    public ResponseEntity<?> devolverLibro(
            @Parameter(description = "ID único del préstamo", required = true, example = "1")
            @PathVariable Long id) {
        try {
            Prestamo prestamo = prestamoService.devolverLibro(id);
            return ResponseEntity.ok(prestamo);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(
        summary = "Eliminar préstamo",
        description = "Elimina permanentemente un préstamo del sistema"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "Préstamo eliminado exitosamente"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Préstamo no encontrado"
        )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @Parameter(description = "ID único del préstamo", required = true, example = "1")
            @PathVariable Long id) {
        if (prestamoService.buscarPorId(id).isPresent()) {
            prestamoService.eliminar(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}