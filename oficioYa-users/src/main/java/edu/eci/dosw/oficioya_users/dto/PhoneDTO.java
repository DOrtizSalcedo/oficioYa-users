package edu.eci.dosw.oficioya_users.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class PhoneDTO {
    @NotBlank(message = "El número es obligatorio")
    @Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "El número debe tener entre 7 y 15 dígitos")
    private String number;

    private boolean principal;
}
