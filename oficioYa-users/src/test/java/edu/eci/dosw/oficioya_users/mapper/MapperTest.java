package edu.eci.dosw.oficioya_users.mapper;

import edu.eci.dosw.oficioya_users.TestData;
import edu.eci.dosw.oficioya_users.dto.AdminUserDTO;
import edu.eci.dosw.oficioya_users.dto.OwnProfileDTO;
import edu.eci.dosw.oficioya_users.dto.PublicProfileDTO;
import edu.eci.dosw.oficioya_users.dto.RoleDTO;
import edu.eci.dosw.oficioya_users.dto.UserUpdateDTO;
import edu.eci.dosw.oficioya_users.entity.Role;
import edu.eci.dosw.oficioya_users.entity.User;
import edu.eci.dosw.oficioya_users.enums.AccountStatus;
import edu.eci.dosw.oficioya_users.enums.RoleName;
import edu.eci.dosw.oficioya_users.mapper.impl.PhoneMapperImpl;
import edu.eci.dosw.oficioya_users.mapper.impl.RoleMapperImpl;
import edu.eci.dosw.oficioya_users.mapper.impl.UserMapperImpl;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class MapperTest {

    private final PhoneMapper phoneMapper = new PhoneMapperImpl();
    private final RoleMapper roleMapper = new RoleMapperImpl();
    private final UserMapper userMapper = new UserMapperImpl(phoneMapper, roleMapper);

    @Test
    void nullInputsReturnNull() {
        assertThat(phoneMapper.toEntity(null)).isNull();
        assertThat(phoneMapper.toDTO(null)).isNull();
        assertThat(roleMapper.toDTO(null)).isNull();
        assertThat(userMapper.toEntity(null)).isNull();
        assertThat(userMapper.toPublicProfile(null)).isNull();
        assertThat(userMapper.toOwnProfile(null)).isNull();
        assertThat(userMapper.toAdminView(null)).isNull();
    }

    @Test
    void roleMapperIncludesPermissions() {
        Role role = TestData.role("ADMIN");
        role.getPermissions().add(TestData.permission("USERS_DEACTIVATE"));

        RoleDTO dto = roleMapper.toDTO(role);

        assertThat(dto.getName()).isEqualTo("ADMIN");
        assertThat(dto.getPermissions()).containsExactly("USERS_DEACTIVATE");
    }

    @Test
    void toEntityTrimsNameAndMapsPhones() {
        User user = userMapper.toEntity(TestData.userRequest(RoleName.TRABAJADOR));

        assertThat(user.getName()).isEqualTo("Ana Pérez");
        assertThat(user.getPhones()).hasSize(1);
        assertThat(user.getPhones().get(0).getUser()).isSameAs(user);
    }

    @Test
    void publicProfileHasNoContactData() {
        User user = TestData.user(AccountStatus.ACTIVE, "TRABAJADOR");

        PublicProfileDTO dto = userMapper.toPublicProfile(user);

        assertThat(dto.getId()).isEqualTo(user.getId());
        assertThat(dto.getName()).isEqualTo("Ana Pérez");
        assertThat(dto.getRoles()).containsExactly("TRABAJADOR");
    }

    @Test
    void ownProfileMasksEmailAndPhones() {
        OwnProfileDTO dto = userMapper.toOwnProfile(TestData.user(AccountStatus.ACTIVE, "CONTRATANTE"));

        assertThat(dto.getEmail()).isEqualTo("*****@mail.com");
        assertThat(dto.getPhones().get(0).getNumber()).isEqualTo("******4567");
        assertThat(dto.getPhones().get(0).isPrincipal()).isTrue();
        assertThat(dto.getAccountStatus()).isEqualTo(AccountStatus.ACTIVE);
    }

    @Test
    void adminViewShowsEverything() {
        User user = TestData.user(AccountStatus.INACTIVE, "ADMIN");

        AdminUserDTO dto = userMapper.toAdminView(user);

        assertThat(dto.getEmail()).isEqualTo("ana@mail.com");
        assertThat(dto.getPhones().get(0).getNumber()).isEqualTo("3001234567");
        assertThat(dto.getRoles()).extracting(RoleDTO::getName).containsExactly("ADMIN");
        assertThat(dto.getAccountStatus()).isEqualTo(AccountStatus.INACTIVE);
        assertThat(dto.getCreatedAt()).isNotNull();
        assertThat(dto.getUpdatedAt()).isNotNull();
    }

    @Test
    void updateEntityChangesOnlyNonNullFields() {
        User user = TestData.user(AccountStatus.ACTIVE, "TRABAJADOR");
        UserUpdateDTO onlyName = new UserUpdateDTO();
        onlyName.setName("  Nuevo Nombre ");

        userMapper.updateEntity(onlyName, user);

        assertThat(user.getName()).isEqualTo("Nuevo Nombre");
        assertThat(user.getPhones()).extracting("number").containsExactly("3001234567");

        UserUpdateDTO onlyPhones = new UserUpdateDTO();
        onlyPhones.setPhones(List.of(TestData.phoneDTO("3109876543", true), TestData.phoneDTO("3205554444", false)));

        userMapper.updateEntity(onlyPhones, user);

        assertThat(user.getName()).isEqualTo("Nuevo Nombre");
        assertThat(user.getPhones()).extracting("number").containsExactly("3109876543", "3205554444");
    }

    @Test
    void updateEntityIgnoresNulls() {
        User user = TestData.user(AccountStatus.ACTIVE);
        userMapper.updateEntity(null, user);
        userMapper.updateEntity(new UserUpdateDTO(), null);
        assertThat(user.getName()).isEqualTo("Ana Pérez");
        assertThat(user.getRoles()).isEqualTo(Set.of());
    }
}
