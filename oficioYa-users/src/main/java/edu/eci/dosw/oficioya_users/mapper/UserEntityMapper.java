package edu.eci.dosw.oficioya_users.mapper;

import edu.eci.dosw.oficioya_users.entity.PhoneEntity;
import edu.eci.dosw.oficioya_users.entity.RoleEntity;
import edu.eci.dosw.oficioya_users.entity.UserEntity;
import edu.eci.dosw.oficioya_users.model.PhoneModel;
import edu.eci.dosw.oficioya_users.model.RoleModel;
import edu.eci.dosw.oficioya_users.model.UserModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserEntityMapper {

    UserModel toModel(UserEntity entity);

    UserEntity toEntity(UserModel model);

    PhoneModel toModel(PhoneEntity entity);

    @Mapping(target = "user", ignore = true)
    PhoneEntity toEntity(PhoneModel model);

    RoleModel toModel(RoleEntity entity);

    RoleEntity toEntity(RoleModel model);
}