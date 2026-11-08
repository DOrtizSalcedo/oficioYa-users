package edu.eci.dosw.oficioya_users.config;

import edu.eci.dosw.oficioya_users.entity.RoleEntity;
import edu.eci.dosw.oficioya_users.entity.UserEntity;
import edu.eci.dosw.oficioya_users.enums.AccountStatus;
import edu.eci.dosw.oficioya_users.repository.RoleRepository;
import edu.eci.dosw.oficioya_users.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.CommandLineRunner;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminSeederTest {

    private static final String EMAIL = "admin@oficioya.com";

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;

    private CommandLineRunner runner(String email, String phone) {
        return new AdminSeeder().seedAdmin(userRepository, roleRepository, email, " Administrador ", phone);
    }

    private static RoleEntity adminRole() {
        RoleEntity role = new RoleEntity();
        role.setName("ADMIN");
        return role;
    }

    @Test
    void createsActiveAdminWithNormalizedEmailAndPrincipalPhone() throws Exception {
        when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(roleRepository.findByName("ADMIN")).thenReturn(Optional.of(adminRole()));
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        runner("  Admin@OficioYA.com ", "3000000000").run();

        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(captor.capture());
        UserEntity admin = captor.getValue();
        assertThat(admin.getEmail()).isEqualTo(EMAIL);
        assertThat(admin.getName()).isEqualTo("Administrador");
        assertThat(admin.getAccountStatus()).isEqualTo(AccountStatus.ACTIVE);
        assertThat(admin.getRoles()).extracting(RoleEntity::getName).containsExactly("ADMIN");
        assertThat(admin.getPhones()).hasSize(1);
        assertThat(admin.getPhones().get(0).getNumber()).isEqualTo("3000000000");
        assertThat(admin.getPhones().get(0).isPrincipal()).isTrue();
    }

    @Test
    void doesNothingWhenAdminAlreadyExists() throws Exception {
        when(userRepository.existsByEmail(EMAIL)).thenReturn(true);

        runner(EMAIL, "3000000000").run();

        verify(userRepository, never()).save(any());
        verify(roleRepository, never()).findByName(any());
    }

    @Test
    void failsWhenPhoneIsMissing() {
        when(userRepository.existsByEmail(EMAIL)).thenReturn(false);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> runner(EMAIL, " ").run());
        assertThat(exception.getMessage()).contains("ADMIN_PHONE");
        verify(userRepository, never()).save(any());
    }

    @Test
    void failsWhenAdminRoleIsNotSeeded() {
        when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(roleRepository.findByName("ADMIN")).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> runner(EMAIL, "3000000000").run());
        verify(userRepository, never()).save(any());
    }
}