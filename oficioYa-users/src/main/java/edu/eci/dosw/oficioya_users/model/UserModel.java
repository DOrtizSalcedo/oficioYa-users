package edu.eci.dosw.oficioya_users.model;

import edu.eci.dosw.oficioya_users.enums.AccountStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
public class UserModel {
    private UUID id;
    private String name;
    private String email;
    private AccountStatus accountStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<PhoneModel> phones = new ArrayList<>();
    private Set<RoleModel> roles = new HashSet<>();
}