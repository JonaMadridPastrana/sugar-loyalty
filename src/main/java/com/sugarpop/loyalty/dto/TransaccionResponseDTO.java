package com.sugarpop.loyalty.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TransaccionResponseDTO {

    private Long id;
    private BigDecimal montoCompra;
    private Integer puntosObtenidos;
    private LocalDateTime fechaTransaccion;
    private String telefonoCliente;
    private String nombreCliente;
    private Integer puntosTotalesCliente;

}