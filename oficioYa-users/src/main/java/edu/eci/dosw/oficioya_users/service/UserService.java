package edu.eci.dosw.oficioya_users.service;

import edu.eci.dosw.oficioya_users.dto.PhoneDTO;
import edu.eci.dosw.oficioya_users.dto.UserRequestDTO;
import edu.eci.dosw.oficioya_users.dto.UserResponseDTO;
import edu.eci.dosw.oficioya_users.dto.UserUpdateDTO;
import edu.eci.dosw.oficioya_users.entity.Role;
import edu.eci.dosw.oficioya_users.entity.User;
import edu.eci.dosw.oficioya_users.enums.AccountStatus;
import edu.eci.dosw.oficioya_users.enums.RoleName;
import edu.eci.dosw.oficioya_users.mapper.UserMapper;
import edu.eci.dosw.oficioya_users.repository.RoleRepository;
import edu.eci.dosw.oficioya_users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;

    public UserResponseDTO register(UserRequestDTO request) {
        String email = request.getEmail().trim().toLowerCase(Locale.ROOT);
        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una cuenta con ese correo");
        }
        request.getRoles().forEach(this::checkNotAdmin);
        validatePhones(request.getPhones());

        User user = new User();
        user.setName(request.getName().trim());
        user.setEmail(email);
        user.setAccountStatus(AccountStatus.ACTIVE);
        user.setPhones(userMapper.toPhones(request.getPhones()));
        user.setRoles(request.getRoles().stream().map(this::findRole).collect(Collectors.toSet()));

        User saved = userRepository.save(user);
        log.info("Cuenta registrada {} con roles {}", saved.getId(), request.getRoles());
        return userMapper.toOwn(saved);
    }

    /** RF-03 */
    @Transactional(readOnly = true)
    public UserResponseDTO getOwnProfile(UUID userId) {
        return userMapper.toOwn(getActiveUser(userId));
    }

    /** RF-04 */
    public UserResponseDTO updateOwnProfile(UUID userId, UserUpdateDTO request) {
        User user = getActiveUser(userId);
        if (request.getName() != null) {
            user.setName(request.getName().trim());
        }
        if (request.getPhones() != null) {
            validatePhones(request.getPhones());
            user.setPhones(userMapper.toPhones(request.getPhones()));
        }
        log.info("Perfil actualizado {}", userId);
        return userMapper.toOwn(userRepository.save(user));
    }

    /** RF-02 */
    public UserResponseDTO addRole(UUID userId, RoleName roleName) {
        checkNotAdmin(roleName);
        User user = getActiveUser(userId);
        if (user.getRoles().stream().anyMatch(role -> role.getName().equals(roleName.name()))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La cuenta ya tiene el rol " + roleName);
        }
        user.getRoles().add(findRole(roleName));
        log.info("Rol {} agregado a la cuenta {}", roleName, userId);
        return userMapper.toOwn(userRepository.save(user));
    }

    /** RF-05 */
    @Transactional(readOnly = true)
    public UserResponseDTO getPublicProfile(UUID id) {
        return userRepository.findByIdAndAccountStatus(id, AccountStatus.ACTIVE)
                .map(userMapper::toPublic)
                .orElseThrow(() -> notFound(id));
    }

    @Transactional(readOnly = true)
    public List<UserResponseDTO> findAll() {
        return userRepository.findAll().stream().map(userMapper::toAdmin).toList();
    }

    /** RF-08 */
    @Transactional(readOnly = true)
    public UserResponseDTO findById(UUID id) {
        return userMapper.toAdmin(userRepository.findById(id).orElseThrow(() -> notFound(id)));
    }

    /** RF-06, RF-07 */
    public UserResponseDTO changeStatus(UUID adminId, UUID userId, AccountStatus status, String reason) {
        if (reason == null || reason.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El motivo es obligatorio");
        }
        if (adminId.equals(userId) && status == AccountStatus.INACTIVE) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Un administrador no puede desactivar su propia cuenta");
        }
        User user = userRepository.findById(userId).orElseThrow(() -> notFound(userId));
        if (user.getAccountStatus() == status) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La cuenta ya está en estado " + status);
        }
        user.setAccountStatus(status);
        log.info("Admin {} cambió la cuenta {} a {}. Motivo: {}", adminId, userId, status, reason.trim());
        return userMapper.toAdmin(userRepository.save(user));
    }

    private User getActiveUser(UUID id) {
        User user = userRepository.findById(id).orElseThrow(() -> notFound(id));
        if (user.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "La cuenta está inactiva");
        }
        return user;
    }

    private void checkNotAdmin(RoleName roleName) {
        if (roleName == RoleName.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "El rol ADMIN no se puede asignar desde la cuenta");
        }
    }

    private Role findRole(RoleName roleName) {
        return roleRepository.findByName(roleName.name())
                .orElseThrow(() -> new IllegalStateException("Rol no configurado: " + roleName));
    }

    private void validatePhones(List<PhoneDTO> phones) {
        if (phones.stream().filter(PhoneDTO::isPrincipal).count() != 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debe haber exactamente un teléfono principal");
        }
        if (phones.stream().map(PhoneDTO::getNumber).distinct().count() != phones.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Hay números de teléfono repetidos");
        }
    }

    private ResponseStatusException notFound(UUID id) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado: " + id);
    }
}
