package edu.eci.dosw.oficioya_users.mapper.impl;

import edu.eci.dosw.oficioya_users.dto.PhoneDTO;
import edu.eci.dosw.oficioya_users.entity.Phone;
import edu.eci.dosw.oficioya_users.mapper.PhoneMapper;
import org.springframework.stereotype.Component;

@Component
public class PhoneMapperImpl implements PhoneMapper {

    @Override
    public Phone toEntity(PhoneDTO dto) {
        if (dto == null) {
            return null;
        }
        Phone phone = new Phone();
        phone.setNumber(dto.getNumber());
        phone.setPrincipal(dto.isPrincipal());
        return phone;
    }

    @Override
    public PhoneDTO toDTO(Phone phone) {
        if (phone == null) {
            return null;
        }
        PhoneDTO dto = new PhoneDTO();
        dto.setNumber(phone.getNumber());
        dto.setPrincipal(phone.isPrincipal());
        return dto;
    }
}
