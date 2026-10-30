package edu.eci.dosw.oficioya_users.service.impl;

import edu.eci.dosw.oficioya_users.dto.AdminUserDTO;
import edu.eci.dosw.oficioya_users.entity.User;
import edu.eci.dosw.oficioya_users.enums.AccountStatus;
import edu.eci.dosw.oficioya_users.exception.ConflictException;
import edu.eci.dosw.oficioya_users.exception.ForbiddenOperationException;
import edu.eci.dosw.oficioya_users.exception.ResourceNotFoundException;
import edu.eci.dosw.oficioya_users.mapper.UserMapper;
import edu.eci.dosw.oficioya_users.repository.UserRepository;
import edu.eci.dosw.oficioya_users.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<AdminUserDTO> findAll(Pageable pageable) {
        return userRepository.findAll(pageable).map(userMapper::toAdminView);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminUserDTO findById(UUID id) {
        return userMapper.toAdminView(getUser(id));
    }

    @Override
    public AdminUserDTO deactivate(UUID adminId, UUID userId, String reason) {
        if (adminId.equals(userId)) {
            throw new ForbiddenOperationException("Un administrador no puede desactivar su propia cuenta");
        }
        return changeStatus(adminId, userId, AccountStatus.INACTIVE, reason);
    }

    @Override
    public AdminUserDTO activate(UUID adminId, UUID userId, String reason) {
        return changeStatus(adminId, userId, AccountStatus.ACTIVE, reason);
    }

    private AdminUserDTO changeStatus(UUID adminId, UUID userId, AccountStatus target, String reason) {
        User user = getUser(userId);
        if (user.getAccountStatus() == target) {
            throw new ConflictException("La cuenta ya está en estado " + target);
        }
        user.setAccountStatus(target);
        User saved = userRepository.save(user);
        // TODO: persistir en la tabla de auditoría de acciones del administrador (RNF-03)
        log.info("Admin {} cambió la cuenta {} a {}. Motivo: {}", adminId, userId, target, reason.trim());
        return userMapper.toAdminView(saved);
    }

    private User getUser(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + id));
    }
}
