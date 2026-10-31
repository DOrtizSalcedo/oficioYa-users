package edu.eci.dosw.oficioya_users;

import edu.eci.dosw.oficioya_users.entity.Role;
import edu.eci.dosw.oficioya_users.enums.RoleName;
import edu.eci.dosw.oficioya_users.repository.RoleRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

/** Levanta el contexto completo con H2 y verifica que RoleSeeder cree los roles. */
@SpringBootTest
class OficioyaUsersApplicationTests {

    @Autowired
    private RoleRepository roleRepository;

    @Test
    void contextLoadsAndSeedsRoles() {
        assertThat(roleRepository.findAll()).extracting(Role::getName)
                .containsExactlyInAnyOrder(Arrays.stream(RoleName.values()).map(Enum::name).toArray(String[]::new));
    }
}
