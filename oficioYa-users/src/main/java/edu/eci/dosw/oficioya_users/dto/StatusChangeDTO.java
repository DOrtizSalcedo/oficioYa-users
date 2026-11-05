package edu.eci.dosw.oficioya_users.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class StatusChangeDTO {
    @NotBlank(message = "El motivo es obligatorio")
    @Size(max = 500, message = "El motivo solo puede tener como máximo 500 caracteres")
    private String reason;
}
