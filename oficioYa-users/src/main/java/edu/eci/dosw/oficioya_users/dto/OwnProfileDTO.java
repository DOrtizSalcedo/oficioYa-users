package edu.eci.dosw.oficioya_users.dto;

import edu.eci.dosw.oficioya_users.enums.AccountStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Perfil propio (RF-03): correo y teléfonos ocultos de manera parcial. */
@Data
public class OwnProfileDTO {
    private UUID id;
    private String name;
    private String email;
    private List<PhoneDTO> phones;
    private Set<String> roles;
    private AccountStatus accountStatus;
    private LocalDateTime createdAt;
}
