package edu.eci.dosw.oficioya_users.service;

import edu.eci.dosw.oficioya_users.dto.OwnProfileDTO;
import edu.eci.dosw.oficioya_users.dto.PublicProfileDTO;
import edu.eci.dosw.oficioya_users.dto.UserRequestDTO;
import edu.eci.dosw.oficioya_users.dto.UserUpdateDTO;
import edu.eci.dosw.oficioya_users.enums.RoleName;

import java.util.UUID;

/** Operaciones del usuario sobre su propia cuenta y consulta de perfiles públicos. */
public interface UserService {
    OwnProfileDTO register(UserRequestDTO request);

    OwnProfileDTO getOwnProfile(UUID userId);

    OwnProfileDTO updateOwnProfile(UUID userId, UserUpdateDTO request);

    OwnProfileDTO addRole(UUID userId, RoleName role);

    PublicProfileDTO getPublicProfile(UUID id);
}
