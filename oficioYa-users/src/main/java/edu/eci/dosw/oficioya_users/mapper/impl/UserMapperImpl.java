package edu.eci.dosw.oficioya_users.mapper.impl;

import edu.eci.dosw.oficioya_users.dto.AdminUserDTO;
import edu.eci.dosw.oficioya_users.dto.OwnProfileDTO;
import edu.eci.dosw.oficioya_users.dto.PhoneDTO;
import edu.eci.dosw.oficioya_users.dto.PublicProfileDTO;
import edu.eci.dosw.oficioya_users.dto.UserRequestDTO;
import edu.eci.dosw.oficioya_users.dto.UserUpdateDTO;
import edu.eci.dosw.oficioya_users.entity.Phone;
import edu.eci.dosw.oficioya_users.entity.Role;
import edu.eci.dosw.oficioya_users.entity.User;
import edu.eci.dosw.oficioya_users.mapper.PhoneMapper;
import edu.eci.dosw.oficioya_users.mapper.RoleMapper;
import edu.eci.dosw.oficioya_users.mapper.UserMapper;
import edu.eci.dosw.oficioya_users.util.MaskingUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UserMapperImpl implements UserMapper {

    private final PhoneMapper phoneMapper;
    private final RoleMapper roleMapper;

    /** Roles, correo normalizado y estado los asigna el servicio. */
    @Override
    public User toEntity(UserRequestDTO request) {
        if (request == null) {
            return null;
        }
        User user = new User();
        user.setName(request.getName().trim());
        user.setPhones(toPhones(request.getPhones()));
        return user;
    }

    @Override
    public PublicProfileDTO toPublicProfile(User user) {
        if (user == null) {
            return null;
        }
        PublicProfileDTO dto = new PublicProfileDTO();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setRoles(roleNames(user));
        return dto;
    }

    @Override
    public OwnProfileDTO toOwnProfile(User user) {
        if (user == null) {
            return null;
        }
        OwnProfileDTO dto = new OwnProfileDTO();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(MaskingUtils.maskEmail(user.getEmail()));
        dto.setPhones(user.getPhones().stream().map(this::toMaskedPhone).toList());
        dto.setRoles(roleNames(user));
        dto.setAccountStatus(user.getAccountStatus());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }

    @Override
    public AdminUserDTO toAdminView(User user) {
        if (user == null) {
            return null;
        }
        AdminUserDTO dto = new AdminUserDTO();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setAccountStatus(user.getAccountStatus());
        dto.setPhones(user.getPhones().stream().map(phoneMapper::toDTO).toList());
        dto.setRoles(user.getRoles().stream().map(roleMapper::toDTO).collect(Collectors.toSet()));
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        return dto;
    }

    /** Actualización parcial: solo se modifican los campos que llegan no nulos. */
    @Override
    public void updateEntity(UserUpdateDTO request, User user) {
        if (request == null || user == null) {
            return;
        }
        if (request.getName() != null) {
            user.setName(request.getName().trim());
        }
        if (request.getPhones() != null) {
            user.setPhones(toPhones(request.getPhones()));
        }
    }

    private PhoneDTO toMaskedPhone(Phone phone) {
        PhoneDTO dto = phoneMapper.toDTO(phone);
        dto.setNumber(MaskingUtils.maskPhone(dto.getNumber()));
        return dto;
    }

    private Set<String> roleNames(User user) {
        return user.getRoles().stream().map(Role::getName).collect(Collectors.toSet());
    }

    private List<Phone> toPhones(List<PhoneDTO> phones) {
        return phones == null ? List.of() : phones.stream().map(phoneMapper::toEntity).toList();
    }
}
