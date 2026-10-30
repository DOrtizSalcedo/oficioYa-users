package edu.eci.dosw.oficioya_users.service.impl;

import edu.eci.dosw.oficioya_users.dto.OwnProfileDTO;
import edu.eci.dosw.oficioya_users.dto.PhoneDTO;
import edu.eci.dosw.oficioya_users.dto.PublicProfileDTO;
import edu.eci.dosw.oficioya_users.dto.UserRequestDTO;
import edu.eci.dosw.oficioya_users.dto.UserUpdateDTO;
import edu.eci.dosw.oficioya_users.entity.Role;
import edu.eci.dosw.oficioya_users.entity.User;
import edu.eci.dosw.oficioya_users.enums.AccountStatus;
import edu.eci.dosw.oficioya_users.enums.RoleName;
import edu.eci.dosw.oficioya_users.exception.BusinessRuleException;
import edu.eci.dosw.oficioya_users.exception.ConflictException;
import edu.eci.dosw.oficioya_users.exception.ForbiddenOperationException;
import edu.eci.dosw.oficioya_users.exception.ResourceNotFoundException;
import edu.eci.dosw.oficioya_users.mapper.UserMapper;
import edu.eci.dosw.oficioya_users.repository.RoleRepository;
import edu.eci.dosw.oficioya_users.repository.UserRepository;
import edu.eci.dosw.oficioya_users.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private static final Set<RoleName> SELF_ASSIGNABLE_ROLES = Set.of(RoleName.TRABAJADOR, RoleName.CONTRATANTE);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;

    @Override
    public OwnProfileDTO register(UserRequestDTO request) {
        String email = normalizeEmail(request.getEmail());
        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("Ya existe una cuenta con ese correo");
        }
        request.getRoles().forEach(this::checkSelfAssignable);
        validatePhones(request.getPhones());

        User user = userMapper.toEntity(request);
        user.setEmail(email);
        user.setAccountStatus(AccountStatus.ACTIVE);
        Set<Role> roles = new HashSet<>();
        request.getRoles().forEach(name -> roles.add(findRole(name)));
        user.setRoles(roles);

        User saved = userRepository.save(user);
        log.info("Cuenta registrada {} con roles {}", saved.getId(), request.getRoles());
        return userMapper.toOwnProfile(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public OwnProfileDTO getOwnProfile(UUID userId) {
        return userMapper.toOwnProfile(getActiveUser(userId));
    }

    @Override
    public OwnProfileDTO updateOwnProfile(UUID userId, UserUpdateDTO request) {
        User user = getActiveUser(userId);
        if (request.getPhones() != null) {
            validatePhones(request.getPhones());
        }
        userMapper.updateEntity(request, user);
        log.info("Perfil actualizado {}", userId);
        return userMapper.toOwnProfile(userRepository.save(user));
    }

    @Override
    public OwnProfileDTO addRole(UUID userId, RoleName roleName) {
        checkSelfAssignable(roleName);
        User user = getActiveUser(userId);
        boolean alreadyHasRole = user.getRoles().stream()
                .anyMatch(role -> role.getName().equals(roleName.name()));
        if (alreadyHasRole) {
            throw new ConflictException("La cuenta ya tiene el rol " + roleName);
        }
        user.getRoles().add(findRole(roleName));
        log.info("Rol {} agregado a la cuenta {}", roleName, userId);
        return userMapper.toOwnProfile(userRepository.save(user));
    }

    @Override
    @Transactional(readOnly = true)
    public PublicProfileDTO getPublicProfile(UUID id) {
        return userRepository.findByIdAndAccountStatus(id, AccountStatus.ACTIVE)
                .map(userMapper::toPublicProfile)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + id));
    }

    private User getActiveUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + id));
        if (user.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new ForbiddenOperationException("La cuenta está inactiva");
        }
        return user;
    }

    private void checkSelfAssignable(RoleName roleName) {
        if (!SELF_ASSIGNABLE_ROLES.contains(roleName)) {
            throw new ForbiddenOperationException("El rol " + roleName + " no se puede asignar desde el registro");
        }
    }

    private Role findRole(RoleName roleName) {
        return roleRepository.findByName(roleName.name())
                .orElseThrow(() -> new IllegalStateException("Rol no configurado en la base de datos: " + roleName));
    }

    /** Exactamente un teléfono principal y sin números repetidos. */
    private void validatePhones(List<PhoneDTO> phones) {
        long principals = phones.stream().filter(PhoneDTO::isPrincipal).count();
        if (principals != 1) {
            throw new BusinessRuleException("Debe haber exactamente un teléfono principal");
        }
        long distinct = phones.stream().map(PhoneDTO::getNumber).distinct().count();
        if (distinct != phones.size()) {
            throw new BusinessRuleException("Hay números de teléfono repetidos");
        }
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
