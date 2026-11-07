package edu.eci.dosw.oficioya_users.service;

import edu.eci.dosw.oficioya_users.dto.PhoneDTO;
import edu.eci.dosw.oficioya_users.dto.UserRequestDTO;
import edu.eci.dosw.oficioya_users.dto.UserResponseDTO;
import edu.eci.dosw.oficioya_users.dto.UserUpdateDTO;
import edu.eci.dosw.oficioya_users.entity.PhoneEntity;
import edu.eci.dosw.oficioya_users.entity.RoleEntity;
import edu.eci.dosw.oficioya_users.entity.UserEntity;
import edu.eci.dosw.oficioya_users.enums.AccountStatus;
import edu.eci.dosw.oficioya_users.enums.RoleName;
import edu.eci.dosw.oficioya_users.mapper.UserEntityMapperImpl;
import edu.eci.dosw.oficioya_users.mapper.UserMapper;
import edu.eci.dosw.oficioya_users.repository.RoleRepository;
import edu.eci.dosw.oficioya_users.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import edu.eci.dosw.oficioya_users.exception.ApiException;
import org.junit.jupiter.api.function.Executable;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
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
        userService = new UserService(userRepository, roleRepository, new UserEntityMapperImpl(), new UserMapper());
    }

    private static RoleEntity role(String name) {
        RoleEntity role = new RoleEntity();
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

    private static PhoneEntity phone(String number, boolean principal) {
        PhoneEntity phone = new PhoneEntity();
        phone.setId(UUID.randomUUID());
        phone.setNumber(number);
        phone.setPrincipal(principal);
        return phone;
    }

    private static UserEntity user(AccountStatus status, String... roles) {
        UserEntity user = new UserEntity();
        user.setId(UUID.randomUUID());
        user.setName("Ana Pérez");
        user.setEmail("ana@mail.com");
        user.setAccountStatus(status);
        user.setCreatedAt(LocalDateTime.now());
        user.setPhones(List.of(phone("3001234567", true)));
        Set<RoleEntity> set = new HashSet<>();
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
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private void assertStatus(Executable action, HttpStatus status) {
        ApiException exception = assertThrows(ApiException.class, action);
        assertEquals(status, exception.getStatus());
    }

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
    void registerRejectsAdminRoleBeforeCheckingEmail() {
        assertStatus(() -> userService.register(request(RoleName.ADMIN)), HttpStatus.BAD_REQUEST);
        verifyNoInteractions(userRepository);
    }

    @Test
    void registerRequiresExactlyOnePrincipalPhone() {
        UserRequestDTO request = request(RoleName.CONTRATANTE);
        request.setPhones(List.of(phoneDTO("3001234567", false)));

        assertStatus(() -> userService.register(request), HttpStatus.BAD_REQUEST);
    }

    @Test
    void registerRejectsRepeatedPhones() {
        UserRequestDTO request = request(RoleName.CONTRATANTE);
        request.setPhones(List.of(phoneDTO("3001234567", true), phoneDTO("3001234567", false)));

        assertStatus(() -> userService.register(request), HttpStatus.BAD_REQUEST);
    }

    @Test
    void registerFailsWhenRoleIsNotSeeded() {
        when(roleRepository.findByName("CONTRATANTE")).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> userService.register(request(RoleName.CONTRATANTE)));
    }

    @Test
    void getOwnProfileReturnsMaskedData() {
        UserEntity user = user(AccountStatus.ACTIVE, "CONTRATANTE");
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
        UserEntity user = user(AccountStatus.INACTIVE);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        assertStatus(() -> userService.getOwnProfile(user.getId()), HttpStatus.FORBIDDEN);
    }

    @Test
    void updateOwnProfileChangesNameAndPhones() {
        UserEntity user = user(AccountStatus.ACTIVE, "TRABAJADOR");
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        saveReturnsArgument();
        UserUpdateDTO request = new UserUpdateDTO();
        request.setName(" Ana María ");
        request.setPhones(List.of(phoneDTO("3109876543", true), phoneDTO("3201112233", false)));

        UserResponseDTO result = userService.updateOwnProfile(user.getId(), request);

        assertThat(result.getName()).isEqualTo("Ana María");
        assertThat(result.getPhones()).extracting(PhoneDTO::getNumber).containsExactly("******6543", "******2233");
    }

    @Test
    void updateOwnProfileWithoutChangesKeepsData() {
        UserEntity user = user(AccountStatus.ACTIVE, "TRABAJADOR");
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        saveReturnsArgument();

        assertThat(userService.updateOwnProfile(user.getId(), new UserUpdateDTO()).getName()).isEqualTo("Ana Pérez");
    }

    @Test
    void getOwnPhonesReturnsFullNumbers() {
        UserEntity user = user(AccountStatus.ACTIVE, "TRABAJADOR");
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        List<PhoneDTO> result = userService.getOwnPhones(user.getId());

        assertThat(result).extracting(PhoneDTO::getNumber).containsExactly("3001234567");
        assertThat(result.get(0).getId()).isNotNull();
    }

    @Test
    void addPhoneAddsAdditionalPhone() {
        UserEntity user = user(AccountStatus.ACTIVE, "TRABAJADOR");
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        saveReturnsArgument();

        List<PhoneDTO> result = userService.addPhone(user.getId(), phoneDTO("3109876543", true));

        assertThat(result).extracting(PhoneDTO::isPrincipal).containsExactly(true, false);
        assertThat(result).extracting(PhoneDTO::getNumber).containsExactly("3001234567", "3109876543");
    }

    @Test
    void savedPhonesAreNotRepeated() {
        UserEntity user = user(AccountStatus.ACTIVE, "TRABAJADOR");
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        // Simula el merge de Hibernate devolviendo el teléfono nuevo dos veces
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> {
            UserEntity saved = invocation.getArgument(0);
            PhoneEntity added = saved.getPhones().get(1);
            added.setId(UUID.randomUUID());
            saved.getPhones().add(added);
            return saved;
        });

        List<PhoneDTO> result = userService.addPhone(user.getId(), phoneDTO("3109876543", false));

        assertThat(result).extracting(PhoneDTO::getNumber).containsExactly("3001234567", "3109876543");
    }

    @Test
    void addPhoneRejectsMoreThanThreePhones() {
        UserEntity user = user(AccountStatus.ACTIVE, "TRABAJADOR");
        user.setPhones(List.of(phone("3001234567", true), phone("3001111111", false), phone("3002222222", false)));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        assertStatus(() -> userService.addPhone(user.getId(), phoneDTO("3109876543", false)), HttpStatus.BAD_REQUEST);
        verify(userRepository, never()).save(any());
    }

    @Test
    void addPhoneRejectsRepeatedNumber() {
        UserEntity user = user(AccountStatus.ACTIVE, "TRABAJADOR");
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        assertStatus(() -> userService.addPhone(user.getId(), phoneDTO("3001234567", false)), HttpStatus.CONFLICT);
    }

    @Test
    void updatePhoneChangesNumberAndKeepsPrincipal() {
        UserEntity user = user(AccountStatus.ACTIVE, "TRABAJADOR");
        UUID phoneId = user.getPhones().get(0).getId();
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        saveReturnsArgument();

        List<PhoneDTO> result = userService.updatePhone(user.getId(), phoneId, phoneDTO("3109876543", false));

        assertThat(result.get(0).getNumber()).isEqualTo("3109876543");
        assertThat(result.get(0).isPrincipal()).isTrue();
    }

    @Test
    void updatePhoneFailsWhenPhoneDoesNotExist() {
        UserEntity user = user(AccountStatus.ACTIVE, "TRABAJADOR");
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        assertStatus(() -> userService.updatePhone(user.getId(), UUID.randomUUID(), phoneDTO("3109876543", false)),
                HttpStatus.NOT_FOUND);
    }

    @Test
    void removePhoneDeletesAdditionalPhone() {
        UserEntity user = user(AccountStatus.ACTIVE, "TRABAJADOR");
        user.setPhones(List.of(phone("3001234567", true), phone("3001111111", false)));
        UUID additionalId = user.getPhones().get(1).getId();
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        saveReturnsArgument();

        List<PhoneDTO> result = userService.removePhone(user.getId(), additionalId);

        assertThat(result).extracting(PhoneDTO::getNumber).containsExactly("3001234567");
    }

    @Test
    void removePhoneRejectsPrincipal() {
        UserEntity user = user(AccountStatus.ACTIVE, "TRABAJADOR");
        UUID principalId = user.getPhones().get(0).getId();
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        assertStatus(() -> userService.removePhone(user.getId(), principalId), HttpStatus.BAD_REQUEST);
        verify(userRepository, never()).save(any());
    }

    @Test
    void addRoleAddsMissingRole() {
        UserEntity user = user(AccountStatus.ACTIVE, "TRABAJADOR");
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(roleRepository.findByName("CONTRATANTE")).thenReturn(Optional.of(role("CONTRATANTE")));
        saveReturnsArgument();

        assertThat(userService.addRole(user.getId(), RoleName.CONTRATANTE).getRoles())
                .containsExactlyInAnyOrder("TRABAJADOR", "CONTRATANTE");
    }

    @Test
    void addRoleRejectsRoleAlreadyAssigned() {
        UserEntity user = user(AccountStatus.ACTIVE, "TRABAJADOR");
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        assertStatus(() -> userService.addRole(user.getId(), RoleName.TRABAJADOR), HttpStatus.CONFLICT);
    }

    @Test
    void addRoleRejectsAdmin() {
        assertStatus(() -> userService.addRole(UUID.randomUUID(), RoleName.ADMIN), HttpStatus.BAD_REQUEST);
    }

    @Test
    void addRoleRejectsAdminAccount() {
        UserEntity admin = user(AccountStatus.ACTIVE, "ADMIN");
        when(userRepository.findById(admin.getId())).thenReturn(Optional.of(admin));

        assertStatus(() -> userService.addRole(admin.getId(), RoleName.TRABAJADOR), HttpStatus.FORBIDDEN);
    }

    @Test
    void getPublicProfileHidesContactData() {
        UserEntity requester = user(AccountStatus.ACTIVE, "CONTRATANTE");
        UserEntity user = user(AccountStatus.ACTIVE, "TRABAJADOR");
        when(userRepository.findById(requester.getId())).thenReturn(Optional.of(requester));
        when(userRepository.findByIdAndAccountStatus(user.getId(), AccountStatus.ACTIVE)).thenReturn(Optional.of(user));

        UserResponseDTO result = userService.getPublicProfile(requester.getId(), user.getId());

        assertThat(result.getName()).isEqualTo("Ana Pérez");
        assertThat(result.getEmail()).isNull();
        assertThat(result.getPhones()).isNull();
    }

    @Test
    void getPublicProfileFailsForMissingOrInactiveUser() {
        UserEntity requester = user(AccountStatus.ACTIVE, "CONTRATANTE");
        UUID id = UUID.randomUUID();
        when(userRepository.findById(requester.getId())).thenReturn(Optional.of(requester));
        when(userRepository.findByIdAndAccountStatus(id, AccountStatus.ACTIVE)).thenReturn(Optional.empty());

        assertStatus(() -> userService.getPublicProfile(requester.getId(), id), HttpStatus.NOT_FOUND);
    }

    @Test
    void getPublicProfileRejectsInactiveRequester() {
        UserEntity requester = user(AccountStatus.INACTIVE, "CONTRATANTE");
        when(userRepository.findById(requester.getId())).thenReturn(Optional.of(requester));

        assertStatus(() -> userService.getPublicProfile(requester.getId(), UUID.randomUUID()), HttpStatus.FORBIDDEN);
    }

    @Test
    void adminSeesFullData() {
        UserEntity user = user(AccountStatus.INACTIVE, "TRABAJADOR");
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
        UserEntity active = user(AccountStatus.ACTIVE, "TRABAJADOR");
        UserEntity inactive = user(AccountStatus.INACTIVE, "TRABAJADOR");
        inactive.setId(active.getId());
        when(userRepository.findById(active.getId())).thenReturn(Optional.of(active), Optional.of(inactive));
        saveReturnsArgument();

        assertThat(userService.changeStatus(adminId, active.getId(), AccountStatus.INACTIVE, "Mal uso")
                .getAccountStatus()).isEqualTo(AccountStatus.INACTIVE);
        assertThat(userService.changeStatus(adminId, active.getId(), AccountStatus.ACTIVE, "Resuelto")
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
        UserEntity user = user(AccountStatus.ACTIVE);
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
