package com.sugarpop.loyalty.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;


@Data
public class RecompensaRequestDTO {
    
    @Size (max = 100, message = "El nombre de la recompensa no puede exceder los 100 caracteres")
    @NotBlank(message = "El nombre de la recompensa es obligatorio")
    private String nombre;

    @NotNull (message = "Los puntos de la recompensa es obligatoria")
    @Min(value = 1, message = "Debe tener al menos 1 punto")
    private Integer puntosRequeridos;

}
