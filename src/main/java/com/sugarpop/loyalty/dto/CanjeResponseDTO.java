package com.sugarpop.loyalty.dto;

import lombok.Data;

@Data
public class CanjeResponseDTO {

    private String telefonoCliente;
    private String nombreCliente;
    private String nombreRecompensa;
    private Integer puntosCanjeados;
    private Integer puntosRestantes;
    private String mensaje;
}