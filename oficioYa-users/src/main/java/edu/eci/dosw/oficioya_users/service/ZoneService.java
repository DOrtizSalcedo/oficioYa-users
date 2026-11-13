package edu.eci.dosw.oficioya_users.service;

import edu.eci.dosw.oficioya_users.dto.ZoneDTO;
import edu.eci.dosw.oficioya_users.entity.ZoneEntity;
import edu.eci.dosw.oficioya_users.exception.BadRequestException;
import edu.eci.dosw.oficioya_users.repository.ZoneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ZoneService {

    static final int MAX_IDS = 50;

    private final ZoneRepository zoneRepository;

    /** Ctálogo, ordenado por id*/
    public List<ZoneDTO> findAll() {
        return zoneRepository.findAllByOrderByIdAsc().stream().map(ZoneService::toDTO).toList();
    }

    /**
     * Solo las zonas pedidas que existen, sin repetidas y ordenadas por id
     */
    public List<ZoneDTO> findByIds(List<Long> ids) {
        Set<Long> unique = new LinkedHashSet<>(ids);
        if (unique.size() > MAX_IDS) {
            throw new BadRequestException("Se pueden consultar máximo " + MAX_IDS + " zonas por petición");
        }
        return zoneRepository.findByIdInOrderByIdAsc(unique).stream().map(ZoneService::toDTO).toList();
    }

    private static ZoneDTO toDTO(ZoneEntity zone) {
        return new ZoneDTO(zone.getId(), zone.getName(), zone.getLocality());
    }
}
