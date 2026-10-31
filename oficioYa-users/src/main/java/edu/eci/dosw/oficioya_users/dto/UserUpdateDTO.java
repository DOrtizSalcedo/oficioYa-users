package edu.eci.dosw.oficioya_users.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/** Campos editables del perfil (RF-04). Los campos nulos no se modifican. El correo no es editable. */
@Data
public class UserUpdateDTO {
    @Size(max = 100, message = "El nombre admite máximo 100 caracteres")
    @Pattern(regexp = ".*\\S.*", message = "El nombre no puede estar vacío")
    private String name;

    @Size(min = 1, max = 3, message = "Debe tener entre 1 y 3 teléfonos")
    private List<@Valid PhoneDTO> phones;
}
