package edu.tcu.cs.projectpulse.team;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {

    Optional<Team> findByName(String name);

    boolean existsByName(String name);

    List<Team> findBySectionId(Long sectionId);

    List<Team> findByNameContainingIgnoreCase(String name);

    List<Team> findByInstructorsId(Long instructorId);
}
