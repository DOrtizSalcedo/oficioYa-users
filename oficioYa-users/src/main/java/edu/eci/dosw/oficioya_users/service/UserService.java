package edu.eci.dosw.oficioya_users.service;

import edu.eci.dosw.oficioya_users.dto.PhoneDTO;
import edu.eci.dosw.oficioya_users.dto.UserRequestDTO;
import edu.eci.dosw.oficioya_users.dto.UserResponseDTO;
import edu.eci.dosw.oficioya_users.dto.UserUpdateDTO;
import edu.eci.dosw.oficioya_users.enums.AccountStatus;
import edu.eci.dosw.oficioya_users.enums.RoleName;
import edu.eci.dosw.oficioya_users.exception.BadRequestException;
import edu.eci.dosw.oficioya_users.exception.ConflictException;
import edu.eci.dosw.oficioya_users.exception.ForbiddenOperationException;
import edu.eci.dosw.oficioya_users.exception.ResourceNotFoundException;
import edu.eci.dosw.oficioya_users.mapper.UserEntityMapper;
import edu.eci.dosw.oficioya_users.mapper.UserMapper;
import edu.eci.dosw.oficioya_users.model.PhoneModel;
import edu.eci.dosw.oficioya_users.model.RoleModel;
import edu.eci.dosw.oficioya_users.model.UserModel;
import edu.eci.dosw.oficioya_users.repository.RoleRepository;
import edu.eci.dosw.oficioya_users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private static final int MAX_PHONES = 3;
    private static final int MAX_IDS = 50;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserEntityMapper userEntityMapper;
    private final UserMapper userMapper;

    /** RF-01 */
    public UserResponseDTO register(UserRequestDTO request) {
        request.getRoles().forEach(this::checkNotAdmin);
        validatePhones(request.getPhones());
        String email = request.getEmail().trim().toLowerCase(Locale.ROOT);
        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("Ya existe una cuenta con ese correo");
        }

        UserModel user = new UserModel();
        user.setName(request.getName().trim());
        user.setEmail(email);
        user.setAccountStatus(AccountStatus.ACTIVE);
        user.setPhones(userMapper.toPhones(request.getPhones()));
        user.setRoles(request.getRoles().stream().map(this::findRole).collect(Collectors.toSet()));

        UserModel saved = save(user);
        log.info("Cuenta registrada {} con roles {}", saved.getId(), request.getRoles());
        return userMapper.toOwn(saved);
    }

    /** RF-02 */
    public UserResponseDTO addRole(UUID userId, RoleName roleName) {
        checkNotAdmin(roleName);
        UserModel user = getActiveUser(userId);

        if (user.getRoles().stream().anyMatch(role -> role.getName().equals(RoleName.ADMIN.name()))) {
            throw new ForbiddenOperationException("El administrador no puede agregarse roles");
        }

        if (user.getRoles().stream().anyMatch(role -> role.getName().equals(roleName.name()))) {
            throw new ConflictException("La cuenta ya tiene el rol " + roleName);
        }
        user.getRoles().add(findRole(roleName));
        log.info("Rol {} agregado a la cuenta {}", roleName, userId);
        return userMapper.toOwn(save(user));
    }

    /** RF-03 */
    @Transactional(readOnly = true)
    public UserResponseDTO getOwnProfile(UUID userId) {
        return userMapper.toOwn(getActiveUser(userId));
    }

    /** RF-04 */
    public UserResponseDTO updateOwnProfile(UUID userId, UserUpdateDTO request) {
        UserModel user = getActiveUser(userId);
        if (request.getName() != null) {
            user.setName(request.getName().trim());
        }
        if (request.getPhones() != null) {
            validatePhones(request.getPhones());
            user.setPhones(userMapper.toPhones(request.getPhones()));
        }
        log.info("Perfil actualizado {}", userId);
        return userMapper.toOwn(save(user));
    }

    /** RF-04: teléfonos completos del dueño, para editarlos */
    @Transactional(readOnly = true)
    public List<PhoneDTO> getOwnPhones(UUID userId) {
        return userMapper.toPhoneDTOs(getActiveUser(userId).getPhones());
    }

    /** RF-04: agrega un teléfono adicional */
    public List<PhoneDTO> addPhone(UUID userId, PhoneDTO request) {
        UserModel user = getActiveUser(userId);
        if (user.getPhones().size() >= MAX_PHONES) {
            throw new BadRequestException("Se admiten máximo " + MAX_PHONES + " teléfonos");
        }
        checkNumberAvailable(user, request.getNumber(), null);

        PhoneModel phone = new PhoneModel();
        phone.setNumber(request.getNumber());
        phone.setPrincipal(false);
        List<PhoneModel> phones = new ArrayList<>(user.getPhones());
        phones.add(phone);
        user.setPhones(phones);
        log.info("Teléfono adicional agregado a la cuenta {}", userId);
        return userMapper.toPhoneDTOs(save(user).getPhones());
    }

    /** RF-04: cambia el número de un teléfono (principal o adicional) */
    public List<PhoneDTO> updatePhone(UUID userId, UUID phoneId, PhoneDTO request) {
        UserModel user = getActiveUser(userId);
        PhoneModel phone = findPhone(user, phoneId);
        checkNumberAvailable(user, request.getNumber(), phone);
        phone.setNumber(request.getNumber());
        log.info("Teléfono {} actualizado en la cuenta {}", phoneId, userId);
        return userMapper.toPhoneDTOs(save(user).getPhones());
    }

    /** RF-04: elimina un teléfono adicional */
    public List<PhoneDTO> removePhone(UUID userId, UUID phoneId) {
        UserModel user = getActiveUser(userId);
        PhoneModel phone = findPhone(user, phoneId);
        if (phone.isPrincipal()) {
            throw new BadRequestException("No se puede eliminar el teléfono principal");
        }
        List<PhoneModel> phones = new ArrayList<>(user.getPhones());
        phones.remove(phone);
        user.setPhones(phones);
        log.info("Teléfono {} eliminado de la cuenta {}", phoneId, userId);
        return userMapper.toPhoneDTOs(save(user).getPhones());
    }

    /** RF-05 */
    @Transactional(readOnly = true)
    public UserResponseDTO getPublicProfile(UUID requesterId, UUID id) {
        getActiveUser(requesterId);
        return userRepository.findByIdAndAccountStatus(id, AccountStatus.ACTIVE)
                .map(userEntityMapper::toModel)
                .map(userMapper::toPublic)
                .orElseThrow(() -> notFound(id));
    }

    /** RF-10 */
    @Transactional(readOnly = true)
    public List<UserResponseDTO> findPublicByIds(List<UUID> ids) {
        Set<UUID> unique = new HashSet<>(ids);
        if (unique.size() > MAX_IDS) {
            throw new BadRequestException("Se pueden consultar máximo " + MAX_IDS + " usuarios por petición");
        }
        return userRepository.findByIdInAndAccountStatus(unique, AccountStatus.ACTIVE).stream()
                .map(userEntityMapper::toModel)
                .map(userMapper::toPublic)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UserResponseDTO> findAll() {
        return userRepository.findAll().stream()
                .map(userEntityMapper::toModel)
                .map(userMapper::toAdmin)
                .toList();
    }

    /** RF-08 */
    @Transactional(readOnly = true)
    public UserResponseDTO findById(UUID id) {
        return userMapper.toAdmin(findUser(id));
    }

    /** RF-06, RF-07 */
    public UserResponseDTO changeStatus(UUID adminId, UUID userId, AccountStatus status, String reason) {
        if (adminId.equals(userId) && status == AccountStatus.INACTIVE) {
            throw new ForbiddenOperationException("Un administrador no puede desactivar su propia cuenta");
        }
        UserModel user = findUser(userId);
        if (user.getAccountStatus() == status) {
            throw new ConflictException("La cuenta ya está en estado " + status);
        }
        user.setAccountStatus(status);
        log.info("Admin {} cambió la cuenta {} a {}. Motivo: {}", adminId, userId, status, reason);
        return userMapper.toAdmin(save(user));
    }

    public void deleteById(UUID id) {
        if (!userRepository.existsById(id)) {
            throw notFound(id);
        }
        userRepository.deleteById(id);
        log.info("Perfil {} eliminado por compensación del registro", id);
    }

    // Persistencia

    private UserModel findUser(UUID id) {
        return userRepository.findById(id)
                .map(userEntityMapper::toModel)
                .orElseThrow(() -> notFound(id));
    }

    private UserModel save(UserModel user) {
        UserModel saved = userEntityMapper.toModel(userRepository.save(userEntityMapper.toEntity(user)));
        // Hibernate 6.5 (HHH-5855) puede repetir en memoria un teléfono nuevo al hacer merge; en BD queda una vez
        Set<UUID> ids = new HashSet<>();
        saved.setPhones(saved.getPhones().stream()
                .filter(phone -> phone.getId() == null || ids.add(phone.getId()))
                .toList());
        return saved;
    }

    private RoleModel findRole(RoleName roleName) {
        return roleRepository.findByName(roleName.name())
                .map(userEntityMapper::toModel)
                .orElseThrow(() -> new IllegalStateException("Rol no configurado: " + roleName));
    }

    // Reglas de Negocio

    private UserModel getActiveUser(UUID id) {
        UserModel user = findUser(id);
        if (user.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new ForbiddenOperationException("La cuenta está inactiva");
        }
        return user;
    }

    private void checkNotAdmin(RoleName roleName) {
        if (roleName == RoleName.ADMIN) {
            throw new BadRequestException("El rol ADMIN no se puede asignar desde la cuenta");
        }
    }

    private void validatePhones(List<PhoneDTO> phones) {
        if (phones.stream().filter(PhoneDTO::isPrincipal).count() != 1) {
            throw new BadRequestException("Debe haber exactamente un teléfono principal");
        }
        if (phones.stream().map(PhoneDTO::getNumber).distinct().count() != phones.size()) {
            throw new BadRequestException("Hay números de teléfono repetidos");
        }
    }

    private PhoneModel findPhone(UserModel user, UUID phoneId) {
        return user.getPhones().stream()
                .filter(phone -> phoneId.equals(phone.getId()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Teléfono no encontrado: " + phoneId));
    }

    private void checkNumberAvailable(UserModel user, String number, PhoneModel excluded) {
        if (user.getPhones().stream().anyMatch(phone -> phone != excluded && phone.getNumber().equals(number))) {
            throw new ConflictException("Ese número ya está registrado en la cuenta");
        }
    }

    private ResourceNotFoundException notFound(UUID id) {
        return new ResourceNotFoundException("Usuario no encontrado: " + id);
    }
}