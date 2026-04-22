package edu.tcu.cs.projectpulse.rubric;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CriterionRepository extends JpaRepository<Criterion, Long> {

    List<Criterion> findByRubricIdOrderByOrderIndexAsc(Long rubricId);
}
