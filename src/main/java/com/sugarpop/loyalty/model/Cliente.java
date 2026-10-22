package com.sugarpop.loyalty.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "clientes")
@Data
@NoArgsConstructor
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Identificador único autonumerado (1, 2, 3...)

    @Column(nullable = false, length = 100)
    private String nombre; // Nombre completo del cliente

    @Column(nullable = false, unique = true, length = 15)
    private String telefono; // Número de teléfono (sirve como ID de búsqueda)

    @Column(unique = true)
    private String email;

    @Column(nullable = false)
    private LocalDate fechaNacimiento;

    @Column(name = "puntos_actuales")
    private Integer puntosActuales = 0; // Inicia siempre con 0 puntos

    @Column(name = "fecha_registro", updatable = false)
    private LocalDateTime fechaRegistro;

    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Transaccion> transacciones = new ArrayList<>();

    // Se asigna la fecha y hora actual justo antes de guardarse en la BD
    @PrePersist
    protected void onCreate() {
        this.fechaRegistro = LocalDateTime.now();
    }
}
