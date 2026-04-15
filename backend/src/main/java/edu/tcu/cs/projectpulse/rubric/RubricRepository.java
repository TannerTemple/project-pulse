package edu.tcu.cs.projectpulse.rubric;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RubricRepository extends JpaRepository<Rubric, Long> {

    Optional<Rubric> findByName(String name);

    boolean existsByName(String name);
}
