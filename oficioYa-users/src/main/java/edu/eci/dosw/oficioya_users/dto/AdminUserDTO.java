package edu.eci.dosw.oficioya_users.dto;

import edu.eci.dosw.oficioya_users.enums.AccountStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Vista completa de una cuenta, solo para el administrador (RF-08, RNF-02). */
@Data
public class AdminUserDTO {
    private UUID id;
    private String name;
    private String email;
    private AccountStatus accountStatus;
    private List<PhoneDTO> phones;
    private Set<RoleDTO> roles;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
