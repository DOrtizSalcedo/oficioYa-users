package edu.eci.dosw.oficioya_users.mapper.impl;

import edu.eci.dosw.oficioya_users.dto.RoleDTO;
import edu.eci.dosw.oficioya_users.entity.Permission;
import edu.eci.dosw.oficioya_users.entity.Role;
import edu.eci.dosw.oficioya_users.mapper.RoleMapper;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class RoleMapperImpl implements RoleMapper {

    @Override
    public RoleDTO toDTO(Role role) {
        if (role == null) {
            return null;
        }
        RoleDTO dto = new RoleDTO();
        dto.setId(role.getId());
        dto.setName(role.getName());
        dto.setPermissions(role.getPermissions().stream()
                .map(Permission::getName)
                .collect(Collectors.toSet()));
        return dto;
    }
}
