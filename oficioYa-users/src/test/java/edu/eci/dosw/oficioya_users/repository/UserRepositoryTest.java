package edu.eci.dosw.oficioya_users.repository;

import edu.eci.dosw.oficioya_users.entity.Permission;
import edu.eci.dosw.oficioya_users.entity.Phone;
import edu.eci.dosw.oficioya_users.entity.Role;
import edu.eci.dosw.oficioya_users.entity.User;
import edu.eci.dosw.oficioya_users.enums.AccountStatus;
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
    private TestEntityManager entityManager;

    private User newUser(String email, AccountStatus status) {
        Role role = roleRepository.findByName("PRUEBA").orElseGet(() -> {
            Permission permission = new Permission();
            permission.setName("EDITAR_PERFIL");
            entityManager.persist(permission);
            Role r = new Role();
            r.setName("PRUEBA");
            r.getPermissions().add(permission);
            return roleRepository.save(r);
        });
        Phone principal = new Phone();
        principal.setNumber("3001234567");
        principal.setPrincipal(true);
        Phone secondary = new Phone();
        secondary.setNumber("3109876543");

        User user = new User();
        user.setName("Ana Pérez");
        user.setEmail(email);
        user.setAccountStatus(status);
        user.setPhones(List.of(principal, secondary));
        user.setRoles(new HashSet<>(Set.of(role)));
        User saved = userRepository.saveAndFlush(user);
        entityManager.clear();
        return saved;
    }

    @Test
    void savesUserWithPhonesRolesPermissionsAndDates() {
        User saved = newUser("ana@mail.com", null);

        User found = userRepository.findById(saved.getId()).orElseThrow();
        assertThat(found.getAccountStatus()).isEqualTo(AccountStatus.ACTIVE);
        assertThat(found.getCreatedAt()).isNotNull();
        assertThat(found.getPhones()).hasSize(2);
        assertThat(found.getRoles().iterator().next().getPermissions()).hasSize(1);
        assertThat(userRepository.existsByEmail("ana@mail.com")).isTrue();
        assertThat(userRepository.existsByEmail("otro@mail.com")).isFalse();
    }

    @Test
    void findByIdAndAccountStatusIgnoresInactiveUsers() {
        User active = newUser("activo@mail.com", AccountStatus.ACTIVE);
        User inactive = newUser("inactivo@mail.com", AccountStatus.INACTIVE);

        assertThat(userRepository.findByIdAndAccountStatus(active.getId(), AccountStatus.ACTIVE)).isPresent();
        assertThat(userRepository.findByIdAndAccountStatus(inactive.getId(), AccountStatus.ACTIVE)).isEmpty();
    }

    @Test
    void replacingPhonesRemovesOldOnes() {
        User saved = newUser("ana@mail.com", AccountStatus.ACTIVE);
        User found = userRepository.findById(saved.getId()).orElseThrow();
        Phone phone = new Phone();
        phone.setNumber("3205554444");
        phone.setPrincipal(true);

        found.setPhones(List.of(phone));
        userRepository.saveAndFlush(found);

        Long phones = entityManager.getEntityManager()
                .createQuery("select count(p) from Phone p", Long.class).getSingleResult();
        assertThat(phones).isEqualTo(1L);
        assertThat(found.getUpdatedAt()).isNotNull();
    }

    @Test
    void emailMustBeUnique() {
        newUser("ana@mail.com", AccountStatus.ACTIVE);

        assertThatThrownBy(() -> newUser("ana@mail.com", AccountStatus.ACTIVE))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
