package edu.eci.dosw.oficioya_users.dto;

import lombok.Data;

import java.util.Set;
import java.util.UUID;

@Data
public class RoleDTO {

    private UUID id;
    private String name;
    private Set<String> permissions;
}
