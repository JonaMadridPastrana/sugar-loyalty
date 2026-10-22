package com.sugarpop.loyalty.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CanjeRegistroDTO {

    @NotBlank(message = "El teléfono del cliente es obligatorio")
    @Pattern(regexp = "^[0-9]{10}$", message = "El teléfono debe contener exactamente 10 dígitos numéricos")
    private String telefonoCliente;

    @NotNull(message = "Debes seleccionar una recompensa")
    @Min(value = 1, message = "El ID de la recompensa debe ser válido")
    private Long recompensaId;

    }