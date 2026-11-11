package edu.eci.dosw.oficioya_users.service;

import edu.eci.dosw.oficioya_users.dto.ZoneDTO;
import edu.eci.dosw.oficioya_users.entity.ZoneEntity;
import edu.eci.dosw.oficioya_users.exception.BadRequestException;
import edu.eci.dosw.oficioya_users.repository.ZoneRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;
import java.util.stream.LongStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ZoneServiceTest {

    @Mock
    private ZoneRepository zoneRepository;
    @InjectMocks
    private ZoneService zoneService;

    private static ZoneEntity zone(long id, String name, String locality) {
        ZoneEntity zone = new ZoneEntity();
        zone.setId(id);
        zone.setName(name);
        zone.setLocality(locality);
        return zone;
    }

    @Test
    void findAllReturnsWholeCatalog() {
        when(zoneRepository.findAllByOrderByIdAsc())
                .thenReturn(List.of(zone(1, "Cedritos", "Usaquén"), zone(2, "Santa Bárbara", "Usaquén")));

        assertThat(zoneService.findAll()).containsExactly(
                new ZoneDTO(1L, "Cedritos", "Usaquén"),
                new ZoneDTO(2L, "Santa Bárbara", "Usaquén"));
    }

    @Test
    void findByIdsRemovesDuplicatesAndReturnsOnlyExisting() {
        when(zoneRepository.findByIdInOrderByIdAsc(Set.of(13L, 2L, 999L)))
                .thenReturn(List.of(zone(2, "Santa Bárbara", "Usaquén"), zone(13, "Teusaquillo", "Teusaquillo")));

        List<ZoneDTO> zones = zoneService.findByIds(List.of(13L, 2L, 2L, 999L));

        assertThat(zones).extracting(ZoneDTO::id).containsExactly(2L, 13L);
        verify(zoneRepository).findByIdInOrderByIdAsc(Set.of(13L, 2L, 999L));
    }

    @Test
    void findByIdsAcceptsExactlyTheLimit() {
        List<Long> ids = LongStream.rangeClosed(1, ZoneService.MAX_IDS).boxed().toList();
        when(zoneRepository.findByIdInOrderByIdAsc(Set.copyOf(ids))).thenReturn(List.of());

        assertThat(zoneService.findByIds(ids)).isEmpty();
    }

    @Test
    void findByIdsRejectsMoreThanTheLimit() {
        List<Long> ids = LongStream.rangeClosed(1, ZoneService.MAX_IDS + 1L).boxed().toList();

        BadRequestException exception = assertThrows(BadRequestException.class, () -> zoneService.findByIds(ids));
        assertThat(exception.getMessage()).contains("50");
        verifyNoInteractions(zoneRepository);
    }
}
