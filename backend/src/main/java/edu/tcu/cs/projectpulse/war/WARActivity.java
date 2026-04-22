package edu.tcu.cs.projectpulse.war;

import edu.tcu.cs.projectpulse.activeweek.ActiveWeek;
import edu.tcu.cs.projectpulse.user.AppUser;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "war_activities")
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"student", "week"})
public class WARActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ActivityCategory category;

    // Short title of the activity (the "Planned activity" column in the WAR)
    @NotBlank
    @Column(nullable = false)
    private String activity;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Min(0)
    @Column(nullable = false)
    private double plannedHours;

    // Nullable — the student fills this in as work progresses
    @Column
    private Double actualHours;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ActivityStatus status;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private AppUser student;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "week_id", nullable = false)
    private ActiveWeek week;
}
