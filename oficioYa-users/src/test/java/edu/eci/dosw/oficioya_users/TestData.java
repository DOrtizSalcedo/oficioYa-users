package edu.eci.dosw.oficioya_users;

import edu.eci.dosw.oficioya_users.dto.PhoneDTO;
import edu.eci.dosw.oficioya_users.dto.UserRequestDTO;
import edu.eci.dosw.oficioya_users.entity.Permission;
import edu.eci.dosw.oficioya_users.entity.Phone;
import edu.eci.dosw.oficioya_users.entity.Role;
import edu.eci.dosw.oficioya_users.entity.User;
import edu.eci.dosw.oficioya_users.enums.AccountStatus;
import edu.eci.dosw.oficioya_users.enums.RoleName;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Datos de prueba reutilizables. */
public final class TestData {

    private TestData() {
    }

    public static Role role(String name) {
        Role role = new Role();
        role.setId(UUID.randomUUID());
        role.setName(name);
        return role;
    }

    public static Permission permission(String name) {
        Permission permission = new Permission();
        permission.setId(UUID.randomUUID());
        permission.setName(name);
        return permission;
    }

    public static PhoneDTO phoneDTO(String number, boolean principal) {
        PhoneDTO dto = new PhoneDTO();
        dto.setNumber(number);
        dto.setPrincipal(principal);
        return dto;
    }

    public static Phone phone(String number, boolean principal) {
        Phone phone = new Phone();
        phone.setNumber(number);
        phone.setPrincipal(principal);
        return phone;
    }

    public static User user(AccountStatus status, String... roleNames) {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setName("Ana Pérez");
        user.setEmail("ana@mail.com");
        user.setAccountStatus(status);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setPhones(new ArrayList<>(List.of(phone("3001234567", true))));
        Set<Role> roles = new HashSet<>();
        for (String name : roleNames) {
            roles.add(role(name));
        }
        user.setRoles(roles);
        return user;
    }

    public static UserRequestDTO userRequest(RoleName... roles) {
        UserRequestDTO request = new UserRequestDTO();
        request.setName("  Ana Pérez  ");
        request.setEmail("Ana@Mail.com");
        request.setPhones(new ArrayList<>(List.of(phoneDTO("3001234567", true))));
        request.setRoles(Set.of(roles));
        return request;
    }
}