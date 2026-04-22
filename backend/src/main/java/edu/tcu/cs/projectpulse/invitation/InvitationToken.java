package edu.tcu.cs.projectpulse.invitation;

import edu.tcu.cs.projectpulse.section.Section;
import edu.tcu.cs.projectpulse.user.UserRole;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(name = "invitation_tokens")
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = "section")
public class InvitationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // UUID-based token — unique per invitation (UC-11, UC-18)
    @NotBlank
    @Column(nullable = false, unique = true)
    private String token;

    @NotBlank
    @Email
    @Column(nullable = false)
    private String email;

    // STUDENT or INSTRUCTOR — determines registration flow
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    // Set for student invitations (UC-11). Null for instructor invitations (UC-18).
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id")
    private Section section;

    // True once the user completes registration (UC-25, UC-30)
    @Column(nullable = false)
    private boolean used = false;

    @NotNull
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @NotNull
    @Column(nullable = false)
    private LocalDateTime expiresAt;
}
