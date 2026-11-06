package com.sugarpop.loyalty.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ClienteLoginDTO {

    @NotBlank(message = "El teléfono no puede estar vacío")
    @Pattern(regexp = "^[0-9]{10}$", message = "El teléfono debe contener exactamente 10 dígitos numéricos")
    private String telefono;

    @NotBlank(message = "El correo electrónico no puede estar vacío")
    @Email(message = "Debe proporcionar un correo electrónico válido")
    private String email;
}

