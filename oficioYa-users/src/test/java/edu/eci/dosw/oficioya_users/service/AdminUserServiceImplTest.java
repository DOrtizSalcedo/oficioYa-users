package edu.eci.dosw.oficioya_users.service;

import edu.eci.dosw.oficioya_users.TestData;
import edu.eci.dosw.oficioya_users.dto.AdminUserDTO;
import edu.eci.dosw.oficioya_users.entity.User;
import edu.eci.dosw.oficioya_users.enums.AccountStatus;
import edu.eci.dosw.oficioya_users.exception.ConflictException;
import edu.eci.dosw.oficioya_users.exception.ForbiddenOperationException;
import edu.eci.dosw.oficioya_users.exception.ResourceNotFoundException;
import edu.eci.dosw.oficioya_users.mapper.impl.PhoneMapperImpl;
import edu.eci.dosw.oficioya_users.mapper.impl.RoleMapperImpl;
import edu.eci.dosw.oficioya_users.mapper.impl.UserMapperImpl;
import edu.eci.dosw.oficioya_users.repository.UserRepository;
import edu.eci.dosw.oficioya_users.service.impl.AdminUserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminUserServiceImplTest {

    private static final String REASON = "Reporte de mal uso";

    @Mock
    private UserRepository userRepository;

    private AdminUserService adminUserService;
    private final UUID adminId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        adminUserService = new AdminUserServiceImpl(userRepository,
                new UserMapperImpl(new PhoneMapperImpl(), new RoleMapperImpl()));
    }

    @Test
    void findAllReturnsFullDataPage() {
        Pageable pageable = PageRequest.of(0, 10);
        User user = TestData.user(AccountStatus.ACTIVE, "TRABAJADOR");
        when(userRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(user), pageable, 1));

        Page<AdminUserDTO> result = adminUserService.findAll(pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getEmail()).isEqualTo("ana@mail.com");
    }

    @Test
    void findByIdReturnsFullData() {
        User user = TestData.user(AccountStatus.INACTIVE, "CONTRATANTE");
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        assertThat(adminUserService.findById(user.getId()).getPhones().get(0).getNumber()).isEqualTo("3001234567");
    }

    @Test
    void findByIdFailsWhenMissing() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminUserService.findById(id)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deactivateChangesStatusToInactive() {
        User user = TestData.user(AccountStatus.ACTIVE, "TRABAJADOR");
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        AdminUserDTO result = adminUserService.deactivate(adminId, user.getId(), REASON);

        assertThat(result.getAccountStatus()).isEqualTo(AccountStatus.INACTIVE);
    }

    @Test
    void deactivateOwnAccountIsForbidden() {
        assertThatThrownBy(() -> adminUserService.deactivate(adminId, adminId, REASON))
                .isInstanceOf(ForbiddenOperationException.class);
        verify(userRepository, never()).save(any());
    }

    @Test
    void deactivateAlreadyInactiveAccountIsConflict() {
        User user = TestData.user(AccountStatus.INACTIVE, "TRABAJADOR");
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> adminUserService.deactivate(adminId, user.getId(), REASON))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void activateChangesStatusToActive() {
        User user = TestData.user(AccountStatus.INACTIVE, "TRABAJADOR");
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        AdminUserDTO result = adminUserService.activate(adminId, user.getId(), REASON);

        assertThat(result.getAccountStatus()).isEqualTo(AccountStatus.ACTIVE);
    }

    @Test
    void activateAlreadyActiveAccountIsConflict() {
        User user = TestData.user(AccountStatus.ACTIVE, "TRABAJADOR");
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> adminUserService.activate(adminId, user.getId(), REASON))
                .isInstanceOf(ConflictException.class);
    }
}
