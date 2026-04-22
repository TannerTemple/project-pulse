package edu.tcu.cs.projectpulse.war;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WARActivityRepository extends JpaRepository<WARActivity, Long> {

    List<WARActivity> findByStudentIdAndWeekId(Long studentId, Long weekId);

    List<WARActivity> findByStudentId(Long studentId);

    List<WARActivity> findByWeekId(Long weekId);

    void deleteByStudentId(Long studentId);
}
