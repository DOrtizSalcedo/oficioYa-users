package edu.eci.dosw.oficioya_users.repository;

import edu.eci.dosw.oficioya_users.entity.PhoneEntity;
import edu.eci.dosw.oficioya_users.entity.RoleEntity;
import edu.eci.dosw.oficioya_users.entity.UserEntity;
import edu.eci.dosw.oficioya_users.enums.AccountStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private TestEntityManager entityManager;

    private UserEntity newUser(String email, AccountStatus status) {
        RoleEntity roleRepo = roleRepository.findByName("PRUEBA").orElseGet(() -> {
            RoleEntity role = new RoleEntity();
            role.setName("PRUEBA");
            return roleRepository.save(role);
        });
        PhoneEntity principal = new PhoneEntity();
        principal.setNumber("3001234567");
        principal.setPrincipal(true);
        PhoneEntity secondary = new PhoneEntity();
        secondary.setNumber("3109876543");

        UserEntity user = new UserEntity();
        user.setName("Ana Pérez");
        user.setEmail(email);
        user.setAccountStatus(status);
        user.setPhones(List.of(principal, secondary));
        user.setRoles(new HashSet<>(Set.of(roleRepo)));
        UserEntity saved = userRepository.saveAndFlush(user);
        entityManager.clear();
        return saved;
    }

    @Test
    void savesUserWithPhonesRolesAndDates() {
        UserEntity saved = newUser("ana@mail.com", null);

        UserEntity found = userRepository.findById(saved.getId()).orElseThrow();
        assertThat(found.getAccountStatus()).isEqualTo(AccountStatus.ACTIVE);
        assertThat(found.getCreatedAt()).isNotNull();
        assertThat(found.getPhones()).hasSize(2);
        assertThat(found.getRoles()).extracting(RoleEntity::getName).containsExactly("PRUEBA");
        assertThat(userRepository.existsByEmail("ana@mail.com")).isTrue();
        assertThat(userRepository.existsByEmail("otro@mail.com")).isFalse();
    }

    @Test
    void findByIdAndAccountStatusIgnoresInactiveUsers() {
        UserEntity active = newUser("activo@mail.com", AccountStatus.ACTIVE);
        UserEntity inactive = newUser("inactivo@mail.com", AccountStatus.INACTIVE);

        assertThat(userRepository.findByIdAndAccountStatus(active.getId(), AccountStatus.ACTIVE)).isPresent();
        assertThat(userRepository.findByIdAndAccountStatus(inactive.getId(), AccountStatus.ACTIVE)).isEmpty();
    }

    @Test
    void findByIdInAndAccountStatusReturnsOnlyActiveUsers() {
        UserEntity active = newUser("activo@mail.com", AccountStatus.ACTIVE);
        UserEntity inactive = newUser("inactivo@mail.com", AccountStatus.INACTIVE);

        List<UserEntity> found = userRepository.findByIdInAndAccountStatus(
                List.of(active.getId(), inactive.getId(), UUID.randomUUID()), AccountStatus.ACTIVE);

        assertThat(found).extracting(UserEntity::getId).containsExactly(active.getId());
    }

    @Test
    void replacingPhonesRemovesOldOnes() {
        UserEntity saved = newUser("ana@mail.com", AccountStatus.ACTIVE);
        UserEntity found = userRepository.findById(saved.getId()).orElseThrow();
        PhoneEntity phone = new PhoneEntity();
        phone.setNumber("3205554444");
        phone.setPrincipal(true);

        found.setPhones(List.of(phone));
        userRepository.saveAndFlush(found);

        Long phones = entityManager.getEntityManager()
                .createQuery("select count(p) from PhoneEntity p", Long.class).getSingleResult();
        assertThat(phones).isEqualTo(1L);
        assertThat(found.getUpdatedAt()).isNotNull();
    }

    @Test
    void emailMustBeUnique() {
        newUser("ana@mail.com", AccountStatus.ACTIVE);

        assertThrows(DataIntegrityViolationException.class, () -> newUser("ana@mail.com", AccountStatus.ACTIVE));
    }

    @Test
    void findByEmailReturnsAccountRegardlessOfStatus() {
        UserEntity inactive = newUser("inactivo@mail.com", AccountStatus.INACTIVE);

        assertThat(userRepository.findByEmail("inactivo@mail.com")).get()
                .extracting(UserEntity::getId).isEqualTo(inactive.getId());
        assertThat(userRepository.findByEmail("otro@mail.com")).isEmpty();
    }
}