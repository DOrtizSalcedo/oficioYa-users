package edu.eci.dosw.oficioya_users.mapper;

import edu.eci.dosw.oficioya_users.dto.PhoneDTO;
import edu.eci.dosw.oficioya_users.entity.Phone;

public interface PhoneMapper {

    Phone toEntity(PhoneDTO dto);

    PhoneDTO toDTO(Phone phone);
}
