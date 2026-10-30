package edu.eci.dosw.oficioya_users.config;

import edu.eci.dosw.oficioya_users.entity.Role;
import edu.eci.dosw.oficioya_users.enums.RoleName;
import edu.eci.dosw.oficioya_users.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Crea los roles del sistema al arrancar si aún no existen. */
@Slf4j
@Component
@RequiredArgsConstructor
public class RoleSeeder implements ApplicationRunner {

    private final RoleRepository roleRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        for (RoleName name : RoleName.values()) {
            if (roleRepository.findByName(name.name()).isEmpty()) {
                Role role = new Role();
                role.setName(name.name());
                roleRepository.save(role);
                log.info("Rol creado: {}", name);
            }
        }
    }
}
