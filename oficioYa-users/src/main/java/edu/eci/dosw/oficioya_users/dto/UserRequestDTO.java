package edu.eci.dosw.oficioya_users.dto;

import edu.eci.dosw.oficioya_users.enums.RoleName;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;
import java.util.Set;

/** Datos de registro (RF-01). */
@Data
public class UserRequestDTO {
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre admite máximo 100 caracteres")
    private String name;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no tiene un formato válido")
    @Size(max = 150, message = "El correo admite máximo 150 caracteres")
    private String email;

    @NotEmpty(message = "Debe registrar al menos un teléfono")
    @Size(max = 3, message = "Se admiten máximo 3 teléfonos")
    private List<@Valid PhoneDTO> phones;

    @NotEmpty(message = "Debe elegir al menos un rol")
    private Set<RoleName> roles;
}
