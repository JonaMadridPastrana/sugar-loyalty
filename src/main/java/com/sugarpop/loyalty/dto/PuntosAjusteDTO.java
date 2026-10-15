package com.sugarpop.loyalty.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PuntosAjusteDTO {

    @NotNull(message = "El campo puntos es obligatorio")
    @Min(value = 0, message = "Los puntos no pueden ser negativos")
    private Integer puntos;
}