package edu.eci.dosw.oficioya_users.mapper;

import edu.eci.dosw.oficioya_users.entity.PhoneEntity;
import edu.eci.dosw.oficioya_users.entity.RoleEntity;
import edu.eci.dosw.oficioya_users.entity.UserEntity;
import edu.eci.dosw.oficioya_users.enums.AccountStatus;
import edu.eci.dosw.oficioya_users.model.PhoneModel;
import edu.eci.dosw.oficioya_users.model.RoleModel;
import edu.eci.dosw.oficioya_users.model.UserModel;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UserEntityMapperTest {

    private final UserEntityMapper mapper = new UserEntityMapperImpl();

    @Test
    void toEntityLinksEachPhoneToItsUser() {
        PhoneModel phone = new PhoneModel();
        phone.setNumber("3001234567");
        phone.setPrincipal(true);
        RoleModel role = new RoleModel();
        role.setId(UUID.randomUUID());
        role.setName("TRABAJADOR");
        UserModel model = new UserModel();
        model.setName("Ana");
        model.setEmail("ana@mail.com");
        model.setAccountStatus(AccountStatus.ACTIVE);
        model.setPhones(List.of(phone));
        model.setRoles(Set.of(role));

        UserEntity entity = mapper.toEntity(model);

        assertThat(entity.getName()).isEqualTo("Ana");
        assertThat(entity.getPhones()).hasSize(1);
        assertThat(entity.getPhones().get(0).getNumber()).isEqualTo("3001234567");
        assertThat(entity.getPhones().get(0).getUser()).isSameAs(entity);
        assertThat(entity.getRoles()).extracting(RoleEntity::getName).containsExactly("TRABAJADOR");
    }

    @Test
    void toModelCopiesAllData() {
        PhoneEntity phone = new PhoneEntity();
        phone.setId(UUID.randomUUID());
        phone.setNumber("3109876543");
        phone.setPrincipal(true);
        RoleEntity role = new RoleEntity();
        role.setId(UUID.randomUUID());
        role.setName("CONTRATANTE");
        UserEntity entity = new UserEntity();
        entity.setId(UUID.randomUUID());
        entity.setName("Luis");
        entity.setEmail("luis@mail.com");
        entity.setAccountStatus(AccountStatus.INACTIVE);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setPhones(List.of(phone));
        entity.setRoles(new HashSet<>(Set.of(role)));

        UserModel model = mapper.toModel(entity);

        assertThat(model.getId()).isEqualTo(entity.getId());
        assertThat(model.getEmail()).isEqualTo("luis@mail.com");
        assertThat(model.getAccountStatus()).isEqualTo(AccountStatus.INACTIVE);
        assertThat(model.getCreatedAt()).isEqualTo(entity.getCreatedAt());
        assertThat(model.getPhones()).extracting(PhoneModel::getNumber).containsExactly("3109876543");
        assertThat(model.getRoles()).extracting(RoleModel::getName).containsExactly("CONTRATANTE");
    }

    @Test
    void nullInputsReturnNull() {
        assertThat(mapper.toModel((UserEntity) null)).isNull();
        assertThat(mapper.toEntity((UserModel) null)).isNull();
        assertThat(mapper.toModel((PhoneEntity) null)).isNull();
        assertThat(mapper.toEntity((PhoneModel) null)).isNull();
        assertThat(mapper.toModel((RoleEntity) null)).isNull();
        assertThat(mapper.toEntity((RoleModel) null)).isNull();
    }
}