package edu.eci.dosw.oficioya_users;

import edu.eci.dosw.oficioya_users.entity.Role;
import edu.eci.dosw.oficioya_users.repository.RoleRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class OficioyaUsersApplicationTests {

    @Autowired
    private RoleRepository roleRepository;

    @Test
    void contextLoadsAndSeedsRoles() {
        assertThat(roleRepository.findAll()).extracting(Role::getName)
                .containsExactlyInAnyOrder("TRABAJADOR", "CONTRATANTE", "ADMIN");
    }
}
