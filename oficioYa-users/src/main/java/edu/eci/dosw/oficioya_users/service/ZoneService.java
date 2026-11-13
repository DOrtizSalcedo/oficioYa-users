package edu.eci.dosw.oficioya_users.service;

import edu.eci.dosw.oficioya_users.dto.ZoneDTO;
import edu.eci.dosw.oficioya_users.entity.ZoneEntity;
import edu.eci.dosw.oficioya_users.exception.BadRequestException;
import edu.eci.dosw.oficioya_users.repository.ZoneRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class ZoneService {

    static final int MAX_IDS = 50;

    private final ZoneRepository zoneRepository;

    /** Catálogo, ordenado por id */
    public List<ZoneDTO> findAll() {
        List<ZoneDTO> zones = zoneRepository.findAllByOrderByIdAsc().stream().map(ZoneService::toDTO).toList();
        log.info("Catálogo de zonas consultado: {} zonas", zones.size());
        return zones;
    }

    /**
     * Solo las zonas pedidas que existen, sin repetidas y ordenadas por id
     */
    public List<ZoneDTO> findByIds(List<Long> ids) {
        Set<Long> unique = new LinkedHashSet<>(ids);
        if (unique.size() > MAX_IDS) {
            log.warn("Consulta de zonas rechazada: {} ids (máximo {})", unique.size(), MAX_IDS);
            throw new BadRequestException("Se pueden consultar máximo " + MAX_IDS + " zonas por petición");
        }
        List<ZoneDTO> zones = zoneRepository.findByIdInOrderByIdAsc(unique).stream().map(ZoneService::toDTO).toList();
        log.info("Zonas consultadas por ids: {} pedidas, {} encontradas", unique.size(), zones.size());
        return zones;
    }

    private static ZoneDTO toDTO(ZoneEntity zone) {
        return new ZoneDTO(zone.getId(), zone.getName(), zone.getLocality());
    }
}
