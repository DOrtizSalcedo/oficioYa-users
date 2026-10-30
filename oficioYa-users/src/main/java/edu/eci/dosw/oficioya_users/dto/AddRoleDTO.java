package edu.eci.dosw.oficioya_users.dto;

import edu.eci.dosw.oficioya_users.enums.RoleName;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** Rol que se agrega a la cuenta propia (RF-02). */
@Data
public class AddRoleDTO {
    @NotNull(message = "El rol es obligatorio")
    private RoleName role;
}
