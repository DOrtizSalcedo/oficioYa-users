package edu.eci.dosw.oficioya_users.config;

import edu.eci.dosw.oficioya_users.TestData;
import edu.eci.dosw.oficioya_users.entity.Role;
import edu.eci.dosw.oficioya_users.repository.RoleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoleSeederTest {

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private RoleSeeder roleSeeder;

    @Test
    void createsMissingRoles() {
        when(roleRepository.findByName(anyString())).thenReturn(Optional.empty());

        roleSeeder.run(null);

        verify(roleRepository, times(3)).save(any(Role.class));
    }

    @Test
    void doesNotDuplicateExistingRoles() {
        when(roleRepository.findByName(anyString())).thenReturn(Optional.of(TestData.role("ADMIN")));

        roleSeeder.run(null);

        verify(roleRepository, never()).save(any(Role.class));
    }
}
