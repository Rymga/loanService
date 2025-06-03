package com.libreriaSanSebastian.loanService.controller;

import com.libreriaSanSebastian.loanService.modelo.Prestamo;
import com.libreriaSanSebastian.loanService.service.PrestamoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

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
    public ResponseEntity<Prestamo> crearPrestamo(@RequestBody Prestamo prestamo) {
        try {
            Prestamo nuevoPrestamo = prestamoService.crearPrestamo(prestamo);
            return ResponseEntity.ok(nuevoPrestamo);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PatchMapping("/devolver/{id}")
    public ResponseEntity<Prestamo> devolverLibro(@PathVariable Long id) {
        try {
            Prestamo prestamo = prestamoService.devolverLibro(id);
            return ResponseEntity.ok(prestamo);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
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