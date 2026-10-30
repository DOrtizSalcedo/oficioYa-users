package edu.eci.dosw.oficioya_users.service;

import edu.eci.dosw.oficioya_users.dto.RoleDTO;

import java.util.List;
import java.util.UUID;

public interface RoleService {

    List<RoleDTO> findAll();

    RoleDTO findById(UUID id);
}
