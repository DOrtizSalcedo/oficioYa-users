package edu.eci.dosw.oficioya_users.mapper;

import edu.eci.dosw.oficioya_users.dto.PhoneDTO;
import edu.eci.dosw.oficioya_users.dto.UserResponseDTO;
import edu.eci.dosw.oficioya_users.entity.Phone;
import edu.eci.dosw.oficioya_users.entity.Role;
import edu.eci.dosw.oficioya_users.entity.User;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/** Convierte entre entidades y DTOs. Aplica el ocultamiento de correo y teléfono (RNF-01). */
@Component
public class UserMapper {

    public List<Phone> toPhones(List<PhoneDTO> dtos) {
        return dtos.stream().map(dto -> {
            Phone phone = new Phone();
            phone.setNumber(dto.getNumber());
            phone.setPrincipal(dto.isPrincipal());
            return phone;
        }).toList();
    }

    /** RF-05: solo datos básicos, sin correo ni teléfonos. */
    public UserResponseDTO toPublic(User user) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setRoles(user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()));
        return dto;
    }

    /** RF-03: perfil propio con correo y teléfonos ocultos de forma parcial. */
    public UserResponseDTO toOwn(User user) {
        UserResponseDTO dto = toPublic(user);
        dto.setEmail(maskEmail(user.getEmail()));
        dto.setPhones(user.getPhones().stream().map(p -> phoneDTO(maskPhone(p.getNumber()), p.isPrincipal())).toList());
        dto.setAccountStatus(user.getAccountStatus());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }

    /** RF-08: el administrador ve todos los datos. */
    public UserResponseDTO toAdmin(User user) {
        UserResponseDTO dto = toPublic(user);
        dto.setEmail(user.getEmail());
        dto.setPhones(user.getPhones().stream().map(p -> phoneDTO(p.getNumber(), p.isPrincipal())).toList());
        dto.setAccountStatus(user.getAccountStatus());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        return dto;
    }

    private PhoneDTO phoneDTO(String number, boolean principal) {
        PhoneDTO dto = new PhoneDTO();
        dto.setNumber(number);
        dto.setPrincipal(principal);
        return dto;
    }

    /** juan@gmail.com -> *****@gmail.com */
    static String maskEmail(String email) {
        return "*****" + email.substring(email.indexOf('@'));
    }

    /** 3001234567 -> ******4567 */
    static String maskPhone(String number) {
        int visible = Math.min(4, number.length());
        return "*".repeat(number.length() - visible) + number.substring(number.length() - visible);
    }
}
