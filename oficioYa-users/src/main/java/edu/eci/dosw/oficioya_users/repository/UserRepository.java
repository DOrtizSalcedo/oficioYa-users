package edu.eci.dosw.oficioya_users.repository;

import edu.eci.dosw.oficioya_users.entity.UserEntity;
import edu.eci.dosw.oficioya_users.enums.AccountStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {
    boolean existsByEmail(String email);

    Optional<UserEntity> findByIdAndAccountStatus(UUID id, AccountStatus status);
}
