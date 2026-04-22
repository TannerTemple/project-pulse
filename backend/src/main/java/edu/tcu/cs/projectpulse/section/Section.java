package edu.tcu.cs.projectpulse.section;

import edu.tcu.cs.projectpulse.activeweek.ActiveWeek;
import edu.tcu.cs.projectpulse.rubric.Rubric;
import edu.tcu.cs.projectpulse.team.Team;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sections")
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"teams", "activeWeeks"})
public class Section {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Format: YYYY-YYYY, e.g. "2023-2024". Must be unique.
    @NotBlank
    @Column(nullable = false, unique = true)
    private String name;

    @NotNull
    @Column(nullable = false)
    private LocalDate startDate;

    @NotNull
    @Column(nullable = false)
    private LocalDate endDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rubric_id")
    private Rubric rubric;

    @OneToMany(mappedBy = "section", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<Team> teams = new ArrayList<>();

    @OneToMany(mappedBy = "section", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("weekStart ASC")
    private List<ActiveWeek> activeWeeks = new ArrayList<>();
}
