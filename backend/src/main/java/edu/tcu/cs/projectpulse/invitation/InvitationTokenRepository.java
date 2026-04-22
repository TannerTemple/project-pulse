package edu.tcu.cs.projectpulse.invitation;

import edu.tcu.cs.projectpulse.user.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InvitationTokenRepository extends JpaRepository<InvitationToken, Long> {

    Optional<InvitationToken> findByToken(String token);

    boolean existsByEmailAndRoleAndUsedFalse(String email, UserRole role);

    Optional<InvitationToken> findByEmailAndRole(String email, UserRole role);
}
