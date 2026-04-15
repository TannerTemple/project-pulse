package edu.tcu.cs.projectpulse.user;

import edu.tcu.cs.projectpulse.section.Section;
import edu.tcu.cs.projectpulse.team.Team;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"team", "section", "supervisedTeams"})
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String firstName;

    @NotBlank
    @Column(nullable = false)
    private String lastName;

    // Optional — used by instructors (UC-30)
    private String middleInitial;

    @NotBlank
    @Email
    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    // Active flag — used for instructor deactivation/reactivation (UC-23, UC-24).
    // Always true for ADMIN and STUDENT.
    @Column(nullable = false)
    private boolean active = true;

    // True after the user has completed account setup via the invitation link (UC-25, UC-30).
    @Column(nullable = false)
    private boolean registrationComplete = false;

    // --- Student-specific associations ---

    // The section this student was invited to and belongs to (set at invitation time).
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id")
    private Section section;

    // The team this student is assigned to. Null until the admin assigns them (UC-12).
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    // --- Instructor-specific associations ---

    // Teams this instructor supervises. Populated via the Team.instructors ManyToMany (UC-19).
    @ManyToMany(mappedBy = "instructors")
    private List<Team> supervisedTeams = new ArrayList<>();
}
