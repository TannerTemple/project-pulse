package edu.tcu.cs.projectpulse.activeweek;

import edu.tcu.cs.projectpulse.section.Section;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

@Entity
@Table(
    name = "active_weeks",
    uniqueConstraints = @UniqueConstraint(columnNames = {"section_id", "week_start"})
)
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = "section")
public class ActiveWeek {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Always a Monday — used as the week identifier (BR-2)
    @NotNull
    @Column(nullable = false)
    private LocalDate weekStart;

    // false = inactive (holiday, etc.). Students must submit WARs/evals only on active weeks.
    @Column(nullable = false)
    private boolean active = true;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id", nullable = false)
    private Section section;
}
