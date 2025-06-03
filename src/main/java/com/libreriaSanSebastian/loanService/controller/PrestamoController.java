package com.libreriaSanSebastian.loanService.controller;

import com.libreriaSanSebastian.loanService.modelo.Prestamo;
import com.libreriaSanSebastian.loanService.service.PrestamoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/prestamos")
public class PrestamoController {

    @Autowired
    private PrestamoService prestamoService;

    @GetMapping
    public List<Prestamo> listarTodos() {
        return prestamoService.listarTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Prestamo> obtenerPorId(@PathVariable Long id) {
        return prestamoService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<Prestamo>> obtenerPorUsuario(@PathVariable Long usuarioId) {
        List<Prestamo> prestamos = prestamoService.buscarPorUsuario(usuarioId);
        return ResponseEntity.ok(prestamos);
    }

    @PostMapping
    public ResponseEntity<?> crearPrestamo(@RequestBody Prestamo prestamo) {
        try {
            // Validación básica
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

    @PatchMapping("/devolver/{id}")
    public ResponseEntity<?> devolverLibro(@PathVariable Long id) {
        try {
            Prestamo prestamo = prestamoService.devolverLibro(id);
            return ResponseEntity.ok(prestamo);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (prestamoService.buscarPorId(id).isPresent()) {
            prestamoService.eliminar(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}