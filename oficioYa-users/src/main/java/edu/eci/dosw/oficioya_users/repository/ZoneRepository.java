package edu.eci.dosw.oficioya_users.repository;

import edu.eci.dosw.oficioya_users.entity.ZoneEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface ZoneRepository extends JpaRepository<ZoneEntity, Long> {
    List<ZoneEntity> findAllByOrderByIdAsc();

    List<ZoneEntity> findByIdInOrderByIdAsc(Collection<Long> ids);
}
