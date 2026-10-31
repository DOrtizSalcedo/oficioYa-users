package edu.eci.dosw.oficioya_users.service;

import edu.eci.dosw.oficioya_users.TestData;
import edu.eci.dosw.oficioya_users.entity.Role;
import edu.eci.dosw.oficioya_users.exception.ResourceNotFoundException;
import edu.eci.dosw.oficioya_users.mapper.impl.RoleMapperImpl;
import edu.eci.dosw.oficioya_users.repository.RoleRepository;
import edu.eci.dosw.oficioya_users.service.impl.RoleServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoleServiceImplTest {

    @Mock
    private RoleRepository roleRepository;

    private RoleService roleService;

    @BeforeEach
    void setUp() {
        roleService = new RoleServiceImpl(roleRepository, new RoleMapperImpl());
    }

    @Test
    void findAllMapsRoles() {
        when(roleRepository.findAll()).thenReturn(List.of(TestData.role("ADMIN"), TestData.role("TRABAJADOR")));

        assertThat(roleService.findAll()).hasSize(2);
    }

    @Test
    void findByIdReturnsRole() {
        Role role = TestData.role("CONTRATANTE");
        when(roleRepository.findById(role.getId())).thenReturn(Optional.of(role));

        assertThat(roleService.findById(role.getId()).getName()).isEqualTo("CONTRATANTE");
    }

    @Test
    void findByIdFailsWhenMissing() {
        UUID id = UUID.randomUUID();
        when(roleRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roleService.findById(id)).isInstanceOf(ResourceNotFoundException.class);
    }
}
