package edu.eci.dosw.oficioya_users.service;

import edu.eci.dosw.oficioya_users.dto.PhoneDTO;
import edu.eci.dosw.oficioya_users.dto.UserRequestDTO;
import edu.eci.dosw.oficioya_users.dto.UserResponseDTO;
import edu.eci.dosw.oficioya_users.dto.UserUpdateDTO;
import edu.eci.dosw.oficioya_users.entity.Phone;
import edu.eci.dosw.oficioya_users.entity.Role;
import edu.eci.dosw.oficioya_users.entity.User;
import edu.eci.dosw.oficioya_users.enums.AccountStatus;
import edu.eci.dosw.oficioya_users.enums.RoleName;
import edu.eci.dosw.oficioya_users.mapper.UserMapper;
import edu.eci.dosw.oficioya_users.repository.RoleRepository;
import edu.eci.dosw.oficioya_users.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;

    private UserService userService;
    private final UUID adminId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, roleRepository, new UserMapper());
    }

    // ---------- datos de prueba ----------

    private static Role role(String name) {
        Role role = new Role();
        role.setId(UUID.randomUUID());
        role.setName(name);
        return role;
    }

    private static PhoneDTO phoneDTO(String number, boolean principal) {
        PhoneDTO dto = new PhoneDTO();
        dto.setNumber(number);
        dto.setPrincipal(principal);
        return dto;
    }

    private static User user(AccountStatus status, String... roles) {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setName("Ana Pérez");
        user.setEmail("ana@mail.com");
        user.setAccountStatus(status);
        user.setCreatedAt(LocalDateTime.now());
        Phone phone = new Phone();
        phone.setNumber("3001234567");
        phone.setPrincipal(true);
        user.setPhones(List.of(phone));
        Set<Role> set = new HashSet<>();
        for (String name : roles) {
            set.add(role(name));
        }
        user.setRoles(set);
        return user;
    }

    private static UserRequestDTO request(RoleName... roles) {
        UserRequestDTO request = new UserRequestDTO();
        request.setName("  Ana Pérez ");
        request.setEmail("Ana@Mail.com");
        request.setPhones(new ArrayList<>(List.of(phoneDTO("3001234567", true))));
        request.setRoles(Set.of(roles));
        return request;
    }

    private void saveReturnsArgument() {
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private void assertStatus(Runnable action, HttpStatus status) {
        assertThatThrownBy(action::run).isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode()).isEqualTo(status);
    }

    // ---------- RF-01 registro ----------

    @Test
    void registerCreatesActiveAccountWithMaskedData() {
        when(userRepository.existsByEmail("ana@mail.com")).thenReturn(false);
        when(roleRepository.findByName("TRABAJADOR")).thenReturn(Optional.of(role("TRABAJADOR")));
        saveReturnsArgument();

        UserResponseDTO result = userService.register(request(RoleName.TRABAJADOR));

        assertThat(result.getName()).isEqualTo("Ana Pérez");
        assertThat(result.getEmail()).isEqualTo("*****@mail.com");
        assertThat(result.getPhones().get(0).getNumber()).isEqualTo("******4567");
        assertThat(result.getRoles()).containsExactly("TRABAJADOR");
        assertThat(result.getAccountStatus()).isEqualTo(AccountStatus.ACTIVE);
    }

    @Test
    void registerRejectsDuplicatedEmail() {
        when(userRepository.existsByEmail("ana@mail.com")).thenReturn(true);

        assertStatus(() -> userService.register(request(RoleName.TRABAJADOR)), HttpStatus.CONFLICT);
        verify(userRepository, never()).save(any());
    }

    @Test
    void registerRejectsAdminRole() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);

        assertStatus(() -> userService.register(request(RoleName.ADMIN)), HttpStatus.FORBIDDEN);
    }

    @Test
    void registerRequiresExactlyOnePrincipalPhone() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        UserRequestDTO request = request(RoleName.CONTRATANTE);
        request.setPhones(List.of(phoneDTO("3001234567", false)));

        assertStatus(() -> userService.register(request), HttpStatus.BAD_REQUEST);
    }

    @Test
    void registerRejectsRepeatedPhones() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        UserRequestDTO request = request(RoleName.CONTRATANTE);
        request.setPhones(List.of(phoneDTO("3001234567", true), phoneDTO("3001234567", false)));

        assertStatus(() -> userService.register(request), HttpStatus.BAD_REQUEST);
    }

    @Test
    void registerFailsWhenRoleIsNotSeeded() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(roleRepository.findByName("CONTRATANTE")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.register(request(RoleName.CONTRATANTE)))
                .isInstanceOf(IllegalStateException.class);
    }

    // ---------- RF-03 y RF-04 perfil propio ----------

    @Test
    void getOwnProfileReturnsMaskedData() {
        User user = user(AccountStatus.ACTIVE, "CONTRATANTE");
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        assertThat(userService.getOwnProfile(user.getId()).getEmail()).isEqualTo("*****@mail.com");
    }

    @Test
    void getOwnProfileFailsWhenUserDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertStatus(() -> userService.getOwnProfile(id), HttpStatus.NOT_FOUND);
    }

    @Test
    void getOwnProfileFailsWhenAccountIsInactive() {
        User user = user(AccountStatus.INACTIVE);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        assertStatus(() -> userService.getOwnProfile(user.getId()), HttpStatus.FORBIDDEN);
    }

    @Test
    void updateOwnProfileChangesNameAndPhones() {
        User user = user(AccountStatus.ACTIVE, "TRABAJADOR");
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        saveReturnsArgument();
        UserUpdateDTO request = new UserUpdateDTO();
        request.setName(" Ana María ");
        request.setPhones(List.of(phoneDTO("3109876543", true), phoneDTO("12", false)));

        UserResponseDTO result = userService.updateOwnProfile(user.getId(), request);

        assertThat(result.getName()).isEqualTo("Ana María");
        assertThat(result.getPhones()).extracting(PhoneDTO::getNumber).containsExactly("******6543", "12");
    }

    @Test
    void updateOwnProfileWithoutChangesKeepsData() {
        User user = user(AccountStatus.ACTIVE, "TRABAJADOR");
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        saveReturnsArgument();

        assertThat(userService.updateOwnProfile(user.getId(), new UserUpdateDTO()).getName()).isEqualTo("Ana Pérez");
    }

    // ---------- RF-02 segundo rol ----------

    @Test
    void addRoleAddsMissingRole() {
        User user = user(AccountStatus.ACTIVE, "TRABAJADOR");
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(roleRepository.findByName("CONTRATANTE")).thenReturn(Optional.of(role("CONTRATANTE")));
        saveReturnsArgument();

        assertThat(userService.addRole(user.getId(), RoleName.CONTRATANTE).getRoles())
                .containsExactlyInAnyOrder("TRABAJADOR", "CONTRATANTE");
    }

    @Test
    void addRoleRejectsRoleAlreadyAssigned() {
        User user = user(AccountStatus.ACTIVE, "TRABAJADOR");
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        assertStatus(() -> userService.addRole(user.getId(), RoleName.TRABAJADOR), HttpStatus.CONFLICT);
    }

    @Test
    void addRoleRejectsAdmin() {
        assertStatus(() -> userService.addRole(UUID.randomUUID(), RoleName.ADMIN), HttpStatus.FORBIDDEN);
    }

    // ---------- RF-05 perfil público ----------

    @Test
    void getPublicProfileHidesContactData() {
        User user = user(AccountStatus.ACTIVE, "TRABAJADOR");
        when(userRepository.findByIdAndAccountStatus(user.getId(), AccountStatus.ACTIVE)).thenReturn(Optional.of(user));

        UserResponseDTO result = userService.getPublicProfile(user.getId());

        assertThat(result.getName()).isEqualTo("Ana Pérez");
        assertThat(result.getEmail()).isNull();
        assertThat(result.getPhones()).isNull();
    }

    @Test
    void getPublicProfileFailsForMissingOrInactiveUser() {
        UUID id = UUID.randomUUID();
        when(userRepository.findByIdAndAccountStatus(id, AccountStatus.ACTIVE)).thenReturn(Optional.empty());

        assertStatus(() -> userService.getPublicProfile(id), HttpStatus.NOT_FOUND);
    }

    // ---------- RF-06, RF-07, RF-08 administrador ----------

    @Test
    void adminSeesFullData() {
        User user = user(AccountStatus.INACTIVE, "TRABAJADOR");
        when(userRepository.findAll()).thenReturn(List.of(user));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        assertThat(userService.findAll().get(0).getEmail()).isEqualTo("ana@mail.com");
        assertThat(userService.findById(user.getId()).getPhones().get(0).getNumber()).isEqualTo("3001234567");
    }

    @Test
    void findByIdFailsWhenMissing() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertStatus(() -> userService.findById(id), HttpStatus.NOT_FOUND);
    }

    @Test
    void deactivateAndActivateChangeStatus() {
        User user = user(AccountStatus.ACTIVE, "TRABAJADOR");
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        saveReturnsArgument();

        assertThat(userService.changeStatus(adminId, user.getId(), AccountStatus.INACTIVE, "Mal uso")
                .getAccountStatus()).isEqualTo(AccountStatus.INACTIVE);
        assertThat(userService.changeStatus(adminId, user.getId(), AccountStatus.ACTIVE, "Resuelto")
                .getAccountStatus()).isEqualTo(AccountStatus.ACTIVE);
    }

    @Test
    void changeStatusRequiresReason() {
        UUID id = UUID.randomUUID();
        assertStatus(() -> userService.changeStatus(adminId, id, AccountStatus.INACTIVE, " "), HttpStatus.BAD_REQUEST);
        assertStatus(() -> userService.changeStatus(adminId, id, AccountStatus.INACTIVE, null), HttpStatus.BAD_REQUEST);
    }

    @Test
    void adminCannotDeactivateOwnAccount() {
        assertStatus(() -> userService.changeStatus(adminId, adminId, AccountStatus.INACTIVE, "Prueba"),
                HttpStatus.FORBIDDEN);
    }

    @Test
    void changeStatusToSameStatusIsConflict() {
        User user = user(AccountStatus.ACTIVE);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        assertStatus(() -> userService.changeStatus(adminId, user.getId(), AccountStatus.ACTIVE, "Prueba"),
                HttpStatus.CONFLICT);
    }

    @Test
    void changeStatusFailsWhenUserMissing() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertStatus(() -> userService.changeStatus(adminId, id, AccountStatus.INACTIVE, "Prueba"),
                HttpStatus.NOT_FOUND);
    }
}
