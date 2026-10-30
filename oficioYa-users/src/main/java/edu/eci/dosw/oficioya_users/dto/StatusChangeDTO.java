package edu.eci.dosw.oficioya_users.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** Motivo obligatorio para desactivar o reactivar una cuenta (RF-06, RF-07). */
@Data
public class StatusChangeDTO {
    @NotBlank(message = "El motivo es obligatorio")
    @Size(max = 500, message = "El motivo admite máximo 500 caracteres")
    private String reason;
}
