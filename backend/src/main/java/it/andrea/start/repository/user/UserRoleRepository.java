package it.andrea.start.repository.user;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import it.andrea.start.constants.RoleType;
import it.andrea.start.models.user.UserRole;

public interface UserRoleRepository extends JpaRepository<UserRole, Long>, JpaSpecificationExecutor<UserRole> {

    public Optional<UserRole> findByRole(RoleType roleType);
    
    public boolean existsByRole(RoleType roleType);

}
