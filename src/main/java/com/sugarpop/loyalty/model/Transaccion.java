package com.sugarpop.loyalty.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transacciones")
@Data
public class Transaccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal montoCompra;

    @Column(nullable = false)
    private Integer puntosObtenidos;

    @Column(length = 255)
    private String descripcion;

    @Column(nullable = false)
    private LocalDateTime fechaTransaccion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @PrePersist
    public void prePersist() {
        this.fechaTransaccion = LocalDateTime.now();
    }
}