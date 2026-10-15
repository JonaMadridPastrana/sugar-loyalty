package com.sugarpop.loyalty.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransaccionRegistroDTO {

    @NotBlank(message = "El teléfono del cliente es obligatorio")
    @Pattern(regexp = "^[0-9]{10}$", message = "El teléfono debe contener exactamente 10 dígitos numéricos")
    private String telefonoCliente;

    @NotNull(message = "El monto de la compra es obligatorio")
    @DecimalMin(value = "1.00", message = "El monto mínimo de compra debe ser de $1.00")
    private BigDecimal montoCompra;
}