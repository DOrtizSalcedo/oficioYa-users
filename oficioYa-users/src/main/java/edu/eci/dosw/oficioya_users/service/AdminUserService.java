package edu.eci.dosw.oficioya_users.service;

import edu.eci.dosw.oficioya_users.dto.AdminUserDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/** Operaciones exclusivas del administrador (RF-06, RF-07, RF-08). */
public interface AdminUserService {
    Page<AdminUserDTO> findAll(Pageable pageable);

    AdminUserDTO findById(UUID id);

    AdminUserDTO deactivate(UUID adminId, UUID userId, String reason);

    AdminUserDTO activate(UUID adminId, UUID userId, String reason);
}
