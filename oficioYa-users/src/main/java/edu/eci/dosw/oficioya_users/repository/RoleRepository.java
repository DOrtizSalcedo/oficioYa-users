package edu.eci.dosw.oficioya_users.repository;

import edu.eci.dosw.oficioya_users.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {

    Optional<Role> findByName(String name);

    // findAllById(ids) ya viene incluido en JpaRepository
}
