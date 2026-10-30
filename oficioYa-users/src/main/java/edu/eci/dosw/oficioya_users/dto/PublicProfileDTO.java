package edu.eci.dosw.oficioya_users.dto;

import lombok.Data;

import java.util.Set;
import java.util.UUID;

/** Perfil visible para otros usuarios (RF-05): sin correo ni teléfonos (RNF-01). */
@Data
public class PublicProfileDTO {
    private UUID id;
    private String name;
    private Set<String> roles;
}
