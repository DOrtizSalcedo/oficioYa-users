package edu.eci.dosw.oficioya_users.repository;

import edu.eci.dosw.oficioya_users.entity.User;
import edu.eci.dosw.oficioya_users.enums.AccountStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByAccountStatus(AccountStatus status);

    Optional<User> findByIdAndAccountStatus(UUID id, AccountStatus status);
}
