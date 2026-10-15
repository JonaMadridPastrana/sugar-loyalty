package com.sugarpop.loyalty.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({
        "id",
        "nombre",
        "telefono",
        "email",
        "fechaNacimiento",
        "sistemaOperativo",
        "puntosActuales",
        "fechaRegistro"
})
@Data
public class ClienteResponseDTO {


    private Long id;
    private String nombre;
    private String telefono;
    private String email;
    private LocalDate fechaNacimiento;
    private Integer puntosActuales;
    private String sistemaOperativo;
    private LocalDateTime fechaRegistro;
}