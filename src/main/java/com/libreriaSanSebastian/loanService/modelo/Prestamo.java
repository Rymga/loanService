package com.libreriaSanSebastian.loanService.modelo;

import lombok.*;
import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "prestamos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Prestamo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long usuarioId;

    @Column(nullable = false)
    private Long libroId;

    @Column(nullable = false)
    private Date fechaPrestamo;

    @Column
    private Date fechaDevolucion;

    @Column(nullable = false)
    private String estado; // ACTIVO, DEVUELTO

    @PrePersist
    protected void onCreate() {
        this.fechaPrestamo = new Date();
        this.estado = "ACTIVO";
    }
}