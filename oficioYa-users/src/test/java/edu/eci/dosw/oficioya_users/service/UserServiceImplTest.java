package edu.eci.dosw.oficioya_users.service;

import edu.eci.dosw.oficioya_users.TestData;
import edu.eci.dosw.oficioya_users.dto.OwnProfileDTO;
import edu.eci.dosw.oficioya_users.dto.PublicProfileDTO;
import edu.eci.dosw.oficioya_users.dto.UserRequestDTO;
import edu.eci.dosw.oficioya_users.dto.UserUpdateDTO;
import edu.eci.dosw.oficioya_users.entity.User;
import edu.eci.dosw.oficioya_users.enums.AccountStatus;
import edu.eci.dosw.oficioya_users.enums.RoleName;
import edu.eci.dosw.oficioya_users.exception.BusinessRuleException;
import edu.eci.dosw.oficioya_users.exception.ConflictException;
import edu.eci.dosw.oficioya_users.exception.ForbiddenOperationException;
import edu.eci.dosw.oficioya_users.exception.ResourceNotFoundException;
import edu.eci.dosw.oficioya_users.mapper.impl.PhoneMapperImpl;
import edu.eci.dosw.oficioya_users.mapper.impl.RoleMapperImpl;
import edu.eci.dosw.oficioya_users.mapper.impl.UserMapperImpl;
import edu.eci.dosw.oficioya_users.repository.RoleRepository;
import edu.eci.dosw.oficioya_users.repository.UserRepository;
import edu.eci.dosw.oficioya_users.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userRepository, roleRepository,
                new UserMapperImpl(new PhoneMapperImpl(), new RoleMapperImpl()));
    }

    private void saveReturnsArgument() {
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            if (user.getId() == null) {
                user.setId(UUID.randomUUID());
            }
            return user;
        });
    }

    // ---------- register (RF-01) ----------

    @Test
    void registerCreatesActiveAccountWithNormalizedEmail() {
        when(userRepository.existsByEmail("ana@mail.com")).thenReturn(false);
        when(roleRepository.findByName("TRABAJADOR")).thenReturn(Optional.of(TestData.role("TRABAJADOR")));
        saveReturnsArgument();

        OwnProfileDTO result = userService.register(TestData.userRequest(RoleName.TRABAJADOR));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();
        assertThat(saved.getEmail()).isEqualTo("ana@mail.com");
        assertThat(saved.getName()).isEqualTo("Ana Pérez");
        assertThat(saved.getAccountStatus()).isEqualTo(AccountStatus.ACTIVE);
        assertThat(result.getEmail()).isEqualTo("*****@mail.com");
        assertThat(result.getRoles()).containsExactly("TRABAJADOR");
    }

    @Test
    void registerWithBothRolesAssignsBoth() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(roleRepository.findByName("TRABAJADOR")).thenReturn(Optional.of(TestData.role("TRABAJADOR")));
        when(roleRepository.findByName("CONTRATANTE")).thenReturn(Optional.of(TestData.role("CONTRATANTE")));
        saveReturnsArgument();

        OwnProfileDTO result = userService.register(TestData.userRequest(RoleName.TRABAJADOR, RoleName.CONTRATANTE));

        assertThat(result.getRoles()).containsExactlyInAnyOrder("TRABAJADOR", "CONTRATANTE");
    }

    @Test
    void registerRejectsDuplicatedEmail() {
        when(userRepository.existsByEmail("ana@mail.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.register(TestData.userRequest(RoleName.TRABAJADOR)))
                .isInstanceOf(ConflictException.class);
        verify(userRepository, never()).save(any());
    }

    @Test
    void registerRejectsAdminRole() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);

        assertThatThrownBy(() -> userService.register(TestData.userRequest(RoleName.ADMIN)))
                .isInstanceOf(ForbiddenOperationException.class);
        verify(userRepository, never()).save(any());
    }

    @Test
    void registerRequiresExactlyOnePrincipalPhone() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        UserRequestDTO request = TestData.userRequest(RoleName.CONTRATANTE);
        request.setPhones(List.of(TestData.phoneDTO("3001234567", false)));

        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("principal");
    }

    @Test
    void registerRejectsRepeatedPhones() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        UserRequestDTO request = TestData.userRequest(RoleName.CONTRATANTE);
        request.setPhones(List.of(TestData.phoneDTO("3001234567", true), TestData.phoneDTO("3001234567", false)));

        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("repetidos");
    }

    @Test
    void registerFailsWhenRoleIsNotSeeded() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(roleRepository.findByName("TRABAJADOR")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.register(TestData.userRequest(RoleName.TRABAJADOR)))
                .isInstanceOf(IllegalStateException.class);
    }

    // ---------- perfil propio (RF-03, RF-04) ----------

    @Test
    void getOwnProfileReturnsMaskedData() {
        User user = TestData.user(AccountStatus.ACTIVE, "CONTRATANTE");
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        OwnProfileDTO result = userService.getOwnProfile(user.getId());

        assertThat(result.getEmail()).isEqualTo("*****@mail.com");
    }

    @Test
    void getOwnProfileFailsWhenUserDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getOwnProfile(id)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getOwnProfileFailsWhenAccountIsInactive() {
        User user = TestData.user(AccountStatus.INACTIVE, "CONTRATANTE");
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.getOwnProfile(user.getId()))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void updateOwnProfileChangesNameAndPhones() {
        User user = TestData.user(AccountStatus.ACTIVE, "TRABAJADOR");
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        saveReturnsArgument();
        UserUpdateDTO request = new UserUpdateDTO();
        request.setName("Ana María");
        request.setPhones(List.of(TestData.phoneDTO("3109876543", true)));

        OwnProfileDTO result = userService.updateOwnProfile(user.getId(), request);

        assertThat(result.getName()).isEqualTo("Ana María");
        assertThat(user.getPhones()).extracting("number").containsExactly("3109876543");
    }

    @Test
    void updateOwnProfileOnlyNameSkipsPhoneValidation() {
        User user = TestData.user(AccountStatus.ACTIVE, "TRABAJADOR");
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        saveReturnsArgument();
        UserUpdateDTO request = new UserUpdateDTO();
        request.setName("Ana María");

        assertThat(userService.updateOwnProfile(user.getId(), request).getName()).isEqualTo("Ana María");
    }

    @Test
    void updateOwnProfileRejectsInvalidPhones() {
        User user = TestData.user(AccountStatus.ACTIVE, "TRABAJADOR");
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        UserUpdateDTO request = new UserUpdateDTO();
        request.setPhones(List.of(TestData.phoneDTO("3109876543", false)));

        assertThatThrownBy(() -> userService.updateOwnProfile(user.getId(), request))
                .isInstanceOf(BusinessRuleException.class);
        verify(userRepository, never()).save(any());
    }

    // ---------- segundo rol (RF-02) ----------

    @Test
    void addRoleAddsMissingRole() {
        User user = TestData.user(AccountStatus.ACTIVE, "TRABAJADOR");
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(roleRepository.findByName("CONTRATANTE")).thenReturn(Optional.of(TestData.role("CONTRATANTE")));
        saveReturnsArgument();

        OwnProfileDTO result = userService.addRole(user.getId(), RoleName.CONTRATANTE);

        assertThat(result.getRoles()).containsExactlyInAnyOrder("TRABAJADOR", "CONTRATANTE");
    }

    @Test
    void addRoleRejectsRoleAlreadyAssigned() {
        User user = TestData.user(AccountStatus.ACTIVE, "TRABAJADOR");
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.addRole(user.getId(), RoleName.TRABAJADOR))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void addRoleRejectsAdmin() {
        assertThatThrownBy(() -> userService.addRole(UUID.randomUUID(), RoleName.ADMIN))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    // ---------- perfil público (RF-05) ----------

    @Test
    void getPublicProfileReturnsActiveUser() {
        User user = TestData.user(AccountStatus.ACTIVE, "TRABAJADOR");
        when(userRepository.findByIdAndAccountStatus(user.getId(), AccountStatus.ACTIVE)).thenReturn(Optional.of(user));

        PublicProfileDTO result = userService.getPublicProfile(user.getId());

        assertThat(result.getName()).isEqualTo("Ana Pérez");
    }

    @Test
    void getPublicProfileFailsForMissingOrInactiveUser() {
        UUID id = UUID.randomUUID();
        when(userRepository.findByIdAndAccountStatus(id, AccountStatus.ACTIVE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getPublicProfile(id)).isInstanceOf(ResourceNotFoundException.class);
    }
}
