package com.sugarpop.loyalty.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import lombok.Data;
import jakarta.persistence.GenerationType;


@Entity 
@Table(name = "recompensas")
@Data 
public class Recompensa {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column (nullable = false, length = 100)
    private String nombre;

    @Column (nullable = false)
    @Min(1) 
    private Integer puntosRequeridos;

    @Column (nullable = false)
    private boolean activa = true;     

}
