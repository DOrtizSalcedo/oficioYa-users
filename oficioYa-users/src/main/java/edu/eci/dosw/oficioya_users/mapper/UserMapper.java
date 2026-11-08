package edu.eci.dosw.oficioya_users.mapper;

import edu.eci.dosw.oficioya_users.dto.PhoneDTO;
import edu.eci.dosw.oficioya_users.dto.UserResponseDTO;
import edu.eci.dosw.oficioya_users.model.PhoneModel;
import edu.eci.dosw.oficioya_users.model.RoleModel;
import edu.eci.dosw.oficioya_users.model.UserModel;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class UserMapper {

    public List<PhoneModel> toPhones(List<PhoneDTO> dtos) {
        return dtos.stream().map(dto -> {
            PhoneModel phone = new PhoneModel();
            phone.setNumber(dto.getNumber());
            phone.setPrincipal(dto.isPrincipal());
            return phone;
        }).toList();
    }

    /** RF-05 */
    public UserResponseDTO toPublic(UserModel user) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setRoles(user.getRoles().stream().map(RoleModel::getName).collect(Collectors.toSet()));
        return dto;
    }

    /** RF-03 */
    public UserResponseDTO toOwn(UserModel user) {
        UserResponseDTO dto = toPublic(user);
        dto.setEmail(maskEmail(user.getEmail()));
        dto.setPhones(user.getPhones().stream().map(p -> phoneDTO(p.getId(), maskPhone(p.getNumber()), p.isPrincipal())).toList());
        dto.setAccountStatus(user.getAccountStatus());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }

    /** RF-08 */
    public UserResponseDTO toAdmin(UserModel user) {
        UserResponseDTO dto = toPublic(user);
        dto.setEmail(user.getEmail());
        dto.setPhones(toPhoneDTOs(user.getPhones()));
        dto.setAccountStatus(user.getAccountStatus());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        return dto;
    }

    /** Teléfonos sin enmascarar: para el admin y para que el dueño los edite */
    public List<PhoneDTO> toPhoneDTOs(List<PhoneModel> phones) {
        return phones.stream().map(p -> phoneDTO(p.getId(), p.getNumber(), p.isPrincipal())).toList();
    }

    private PhoneDTO phoneDTO(UUID id, String number, boolean principal) {
        PhoneDTO dto = new PhoneDTO();
        dto.setId(id);
        dto.setNumber(number);
        dto.setPrincipal(principal);
        return dto;
    }

    static String maskEmail(String email) {
        return "*****" + email.substring(email.indexOf('@'));
    }

    static String maskPhone(String number) {
        int visible = Math.min(4, number.length());
        return "*".repeat(number.length() - visible) + number.substring(number.length() - visible);
    }
}