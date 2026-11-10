package edu.eci.dosw.oficioya_users.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import edu.eci.dosw.oficioya_users.enums.AccountStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponseDTO {
    private UUID id;
    private String name;
    private String email;
    private List<PhoneDTO> phones;
    private Set<String> roles;
    private AccountStatus accountStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
