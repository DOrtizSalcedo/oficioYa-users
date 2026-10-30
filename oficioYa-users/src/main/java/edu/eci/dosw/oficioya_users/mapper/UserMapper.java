package edu.eci.dosw.oficioya_users.mapper;

import edu.eci.dosw.oficioya_users.dto.AdminUserDTO;
import edu.eci.dosw.oficioya_users.dto.OwnProfileDTO;
import edu.eci.dosw.oficioya_users.dto.PublicProfileDTO;
import edu.eci.dosw.oficioya_users.dto.UserRequestDTO;
import edu.eci.dosw.oficioya_users.dto.UserUpdateDTO;
import edu.eci.dosw.oficioya_users.entity.User;

public interface UserMapper {
    User toEntity(UserRequestDTO request);

    PublicProfileDTO toPublicProfile(User user);

    OwnProfileDTO toOwnProfile(User user);

    AdminUserDTO toAdminView(User user);

    void updateEntity(UserUpdateDTO request, User user);
}
