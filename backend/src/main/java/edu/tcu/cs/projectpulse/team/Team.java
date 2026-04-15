package edu.tcu.cs.projectpulse.team;

import edu.tcu.cs.projectpulse.section.Section;
import edu.tcu.cs.projectpulse.user.AppUser;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "teams")
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"section", "students", "instructors"})
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Must be unique across all sections (UC-9, UC-10)
    @NotBlank
    @Column(nullable = false, unique = true)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String websiteUrl;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id", nullable = false)
    private Section section;

    // Students assigned to this team. A student belongs to exactly one team.
    @OneToMany(mappedBy = "team")
    private List<AppUser> students = new ArrayList<>();

    // Instructors supervising this team. An instructor can supervise multiple teams (BR-1).
    @ManyToMany
    @JoinTable(
        name = "team_instructors",
        joinColumns = @JoinColumn(name = "team_id"),
        inverseJoinColumns = @JoinColumn(name = "instructor_id")
    )
    private List<AppUser> instructors = new ArrayList<>();
}
