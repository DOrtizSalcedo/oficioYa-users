package edu.eci.dosw.oficioya_users.service.impl;

import edu.eci.dosw.oficioya_users.dto.RoleDTO;
import edu.eci.dosw.oficioya_users.mapper.RoleMapper;
import edu.eci.dosw.oficioya_users.repository.RoleRepository;
import edu.eci.dosw.oficioya_users.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import edu.eci.dosw.oficioya_users.exception.ResourceNotFoundException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;

    @Override
    public List<RoleDTO> findAll() {
        return roleRepository.findAll().stream().map(roleMapper::toDTO).toList();
    }

    @Override
    public RoleDTO findById(UUID id) {
        return roleRepository.findById(id)
                .map(roleMapper::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado: " + id));
    }
}
