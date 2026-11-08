package edu.eci.dosw.oficioya_users.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.UUID;

@Data
public class PhoneDTO {
    // Solo de respuesta: identifica el teléfono para editarlo o eliminarlo
    private UUID id;

    @NotBlank(message = "El número es obligatorio")
    @Pattern(regexp = "^3[0-9]{9}$", message = "Ingrese un teléfono válido")
    private String number;

    private boolean principal;
}
