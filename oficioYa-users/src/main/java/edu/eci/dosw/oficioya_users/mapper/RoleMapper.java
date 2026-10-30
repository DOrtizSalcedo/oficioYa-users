package edu.eci.dosw.oficioya_users.mapper;

import edu.eci.dosw.oficioya_users.dto.RoleDTO;
import edu.eci.dosw.oficioya_users.entity.Role;

public interface RoleMapper {

    RoleDTO toDTO(Role role);
}
