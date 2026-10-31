package edu.eci.dosw.oficioya_users.repository;

import edu.eci.dosw.oficioya_users.TestData;
import edu.eci.dosw.oficioya_users.entity.Permission;
import edu.eci.dosw.oficioya_users.entity.Role;
import edu.eci.dosw.oficioya_users.entity.User;
import edu.eci.dosw.oficioya_users.enums.AccountStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PermissionRepository permissionRepository;
    @Autowired
    private TestEntityManager entityManager;

    private Role worker;

    @BeforeEach
    void setUp() {
        Permission permission = new Permission();
        permission.setName("PROFILE_EDIT");
        permissionRepository.save(permission);

        Role role = new Role();
        role.setName("TRABAJADOR");
        role.setPermissions(new HashSet<>(Set.of(permission)));
        worker = roleRepository.save(role);
    }

    private User newUser(String email, AccountStatus status) {
        User user = new User();
        user.setName("Ana Pérez");
        user.setEmail(email);
        user.setAccountStatus(status);
        user.setPhones(List.of(TestData.phone("3001234567", true), TestData.phone("3109876543", false)));
        user.setRoles(new HashSet<>(Set.of(worker)));
        return user;
    }

    private User persist(User user) {
        User saved = userRepository.saveAndFlush(user);
        entityManager.clear();
        return saved;
    }

    @Test
    void savesUserWithPhonesRolesAndAuditDates() {
        User saved = persist(newUser("ana@mail.com", AccountStatus.ACTIVE));

        User found = userRepository.findById(saved.getId()).orElseThrow();
        assertThat(found.getCreatedAt()).isNotNull();
        assertThat(found.getUpdatedAt()).isNotNull();
        assertThat(found.getPhones()).hasSize(2);
        assertThat(found.getRoles()).extracting(Role::getName).containsExactly("TRABAJADOR");
        assertThat(found.getRoles().iterator().next().getPermissions())
                .extracting(Permission::getName).containsExactly("PROFILE_EDIT");
    }

    @Test
    void defaultStatusIsActiveWhenNotSet() {
        User user = newUser("sin-estado@mail.com", null);

        assertThat(persist(user).getAccountStatus()).isEqualTo(AccountStatus.ACTIVE);
    }

    @Test
    void findsByEmailAndChecksExistence() {
        persist(newUser("ana@mail.com", AccountStatus.ACTIVE));

        assertThat(userRepository.existsByEmail("ana@mail.com")).isTrue();
        assertThat(userRepository.existsByEmail("otro@mail.com")).isFalse();
        assertThat(userRepository.findByEmail("ana@mail.com")).isPresent();
    }

    @Test
    void findByIdAndAccountStatusIgnoresInactiveUsers() {
        User active = persist(newUser("activo@mail.com", AccountStatus.ACTIVE));
        User inactive = persist(newUser("inactivo@mail.com", AccountStatus.INACTIVE));

        assertThat(userRepository.findByIdAndAccountStatus(active.getId(), AccountStatus.ACTIVE)).isPresent();
        assertThat(userRepository.findByIdAndAccountStatus(inactive.getId(), AccountStatus.ACTIVE)).isEmpty();
        assertThat(userRepository.findByAccountStatus(AccountStatus.INACTIVE))
                .extracting(User::getEmail).containsExactly("inactivo@mail.com");
    }

    @Test
    void replacingPhonesRemovesOrphans() {
        User saved = persist(newUser("ana@mail.com", AccountStatus.ACTIVE));

        User found = userRepository.findById(saved.getId()).orElseThrow();
        found.setPhones(List.of(TestData.phone("3205554444", true)));
        persist(found);

        assertThat(userRepository.findById(saved.getId()).orElseThrow().getPhones())
                .extracting("number").containsExactly("3205554444");
        Long phoneCount = entityManager.getEntityManager()
                .createQuery("select count(p) from Phone p", Long.class).getSingleResult();
        assertThat(phoneCount).isEqualTo(1L);
    }

    @Test
    void emailMustBeUnique() {
        persist(newUser("ana@mail.com", AccountStatus.ACTIVE));

        assertThatThrownBy(() -> userRepository.saveAndFlush(newUser("ana@mail.com", AccountStatus.ACTIVE)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void findsRoleAndPermissionByName() {
        assertThat(roleRepository.findByName("TRABAJADOR")).isPresent();
        assertThat(roleRepository.findByName("NO_EXISTE")).isEmpty();
        assertThat(permissionRepository.findByName("PROFILE_EDIT")).isPresent();
    }
}
