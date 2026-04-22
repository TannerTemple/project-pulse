package edu.tcu.cs.projectpulse.activeweek;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ActiveWeekRepository extends JpaRepository<ActiveWeek, Long> {

    List<ActiveWeek> findBySectionIdOrderByWeekStartAsc(Long sectionId);

    List<ActiveWeek> findBySectionIdAndActiveTrue(Long sectionId);

    Optional<ActiveWeek> findBySectionIdAndWeekStart(Long sectionId, LocalDate weekStart);
}
