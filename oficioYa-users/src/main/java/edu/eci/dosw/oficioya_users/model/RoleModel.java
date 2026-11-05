package edu.eci.dosw.oficioya_users.model;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class RoleModel {
    private UUID id;
    private String name;
}