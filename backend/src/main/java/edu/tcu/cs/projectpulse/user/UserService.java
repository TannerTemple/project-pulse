package edu.tcu.cs.projectpulse.user;

import edu.tcu.cs.projectpulse.common.exception.ObjectNotFoundException;
import edu.tcu.cs.projectpulse.email.EmailService;
import edu.tcu.cs.projectpulse.invitation.InvitationToken;
import edu.tcu.cs.projectpulse.invitation.InvitationTokenRepository;
import edu.tcu.cs.projectpulse.peerevaluation.PeerEvaluationRepository;
import edu.tcu.cs.projectpulse.section.Section;
import edu.tcu.cs.projectpulse.section.SectionService;
import edu.tcu.cs.projectpulse.user.dto.InviteRequest;
import edu.tcu.cs.projectpulse.user.dto.UpdateAccountRequest;
import edu.tcu.cs.projectpulse.user.dto.UserResponse;
import edu.tcu.cs.projectpulse.war.WARActivityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final InvitationTokenRepository tokenRepository;
    private final WARActivityRepository warRepository;
    private final PeerEvaluationRepository evalRepository;
    private final SectionService sectionService;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.invitation.expiry-hours:72}")
    private int invitationExpiryHours;

    @Value("${app.base-url:http://localhost:3000}")
    private String baseUrl;

    // ── Own Account ──────────────────────────────────────────────────────────

    /** Returns the current user's own profile (UC-26). */
    @Transactional(readOnly = true)
    public UserResponse getMe() {
        return UserResponse.from(currentUser());
    }

    /** Students and instructors can edit their own name and password (UC-26). */
    @Transactional
    public UserResponse updateMe(UpdateAccountRequest request) {
        AppUser user = currentUser();

        if (request.newPassword() != null && !request.newPassword().isBlank()) {
            if (!request.newPassword().equals(request.confirmPassword())) {
                throw new IllegalArgumentException("Passwords do not match.");
            }
            user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        }

        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        if (user.getRole() == UserRole.INSTRUCTOR && request.middleInitial() != null) {
            user.setMiddleInitial(request.middleInitial());
        }

        return UserResponse.from(userRepository.save(user));
    }

    // ── Find / View ───────────────────────────────────────────────────────────

    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    @Transactional(readOnly = true)
    public List<UserResponse> findStudents(String firstName, String lastName,
                                           String email, Long sectionId, Long teamId) {
        return userRepository.findByRole(UserRole.STUDENT).stream()
                .filter(u -> firstName == null || u.getFirstName().toLowerCase().contains(firstName.toLowerCase()))
                .filter(u -> lastName == null  || u.getLastName().toLowerCase().contains(lastName.toLowerCase()))
                .filter(u -> email == null     || u.getEmail().toLowerCase().contains(email.toLowerCase()))
                .filter(u -> sectionId == null || (u.getSection() != null && u.getSection().getId().equals(sectionId)))
                .filter(u -> teamId == null    || (u.getTeam() != null && u.getTeam().getId().equals(teamId)))
                .sorted((a, b) -> {
                    String sA = a.getSection() != null ? a.getSection().getName() : "";
                    String sB = b.getSection() != null ? b.getSection().getName() : "";
                    int sectionCmp = sB.compareTo(sA);
                    return sectionCmp != 0 ? sectionCmp : a.getLastName().compareTo(b.getLastName());
                })
                .map(UserResponse::from)
                .toList();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public List<UserResponse> findInstructors(String firstName, String lastName,
                                              String teamName, Boolean active) {
        return userRepository.findByRole(UserRole.INSTRUCTOR).stream()
                .filter(u -> firstName == null || u.getFirstName().toLowerCase().contains(firstName.toLowerCase()))
                .filter(u -> lastName == null  || u.getLastName().toLowerCase().contains(lastName.toLowerCase()))
                .filter(u -> active == null    || u.isActive() == active)
                .sorted((a, b) -> a.getLastName().compareTo(b.getLastName()))
                .map(UserResponse::from)
                .toList();
    }

    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    @Transactional(readOnly = true)
    public UserResponse findById(Long id) {
        return UserResponse.from(getUserOrThrow(id));
    }

    // ── Invitations ───────────────────────────────────────────────────────────

    /**
     * Parses the semicolon-separated email list, creates invitation tokens,
     * and fires emails (UC-11).
     */
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public int inviteStudents(Long sectionId, InviteRequest request) {
        Section section = sectionService.getSectionOrThrow(sectionId);
        List<String> emails = parseEmails(request.emails());
        AppUser admin = currentAdmin();

        for (String email : emails) {
            String token = createToken(email, UserRole.STUDENT, section);
            emailService.sendStudentInvitation(
                    email,
                    admin.getFirstName() + " " + admin.getLastName(),
                    admin.getEmail(),
                    token,
                    request.customMessage());
        }
        return emails.size();
    }

    /**
     * Parses the semicolon-separated email list, creates invitation tokens,
     * and fires emails (UC-18).
     */
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public int inviteInstructors(InviteRequest request) {
        List<String> emails = parseEmails(request.emails());
        AppUser admin = currentAdmin();

        for (String email : emails) {
            String token = createToken(email, UserRole.INSTRUCTOR, null);
            emailService.sendInstructorInvitation(
                    email,
                    admin.getFirstName() + " " + admin.getLastName(),
                    admin.getEmail(),
                    token,
                    request.customMessage());
        }
        return emails.size();
    }

    // ── Deactivate / Reactivate ───────────────────────────────────────────────

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public UserResponse deactivate(Long id) {
        AppUser instructor = getUserOrThrow(id);
        if (instructor.getRole() != UserRole.INSTRUCTOR) {
            throw new IllegalArgumentException("Only instructors can be deactivated.");
        }
        instructor.setActive(false);
        return UserResponse.from(userRepository.save(instructor));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public UserResponse reactivate(Long id) {
        AppUser instructor = getUserOrThrow(id);
        if (instructor.getRole() != UserRole.INSTRUCTOR) {
            throw new IllegalArgumentException("Only instructors can be reactivated.");
        }
        instructor.setActive(true);
        userRepository.save(instructor);
        emailService.sendAccountReactivated(instructor.getEmail(), instructor.getFirstName());
        return UserResponse.from(instructor);
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    /** Permanently deletes a student and their associated data (UC-17). */
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void deleteStudent(Long id) {
        AppUser student = getUserOrThrow(id);
        if (student.getRole() != UserRole.STUDENT) {
            throw new IllegalArgumentException("This operation is only valid for students.");
        }
        warRepository.deleteByStudentId(id);
        evalRepository.deleteByEvaluatorIdOrEvaluateeId(id, id);
        userRepository.delete(student);
    }

    // ── private ───────────────────────────────────────────────────────────────

    private AppUser getUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("User", id));
    }

    private String createToken(String email, UserRole role, Section section) {
        InvitationToken token = new InvitationToken();
        token.setToken(UUID.randomUUID().toString());
        token.setEmail(email);
        token.setRole(role);
        token.setSection(section);
        token.setUsed(false);
        token.setCreatedAt(LocalDateTime.now());
        token.setExpiresAt(LocalDateTime.now().plusHours(invitationExpiryHours));
        tokenRepository.save(token);
        log.info("[DEV] Registration link for {}: {}/register?token={}", email, baseUrl, token.getToken());
        return token.getToken();
    }

    private List<String> parseEmails(String raw) {
        List<String> emails = Arrays.stream(raw.split(";"))
                .map(String::trim)
                .filter(e -> !e.isBlank())
                .toList();
        if (emails.isEmpty()) {
            throw new IllegalArgumentException("No valid email addresses provided.");
        }
        // Validate basic format
        for (String email : emails) {
            if (!email.contains("@")) {
                throw new IllegalArgumentException("Invalid email address: " + email);
            }
        }
        return emails;
    }

    private AppUser currentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ObjectNotFoundException("Current user not found."));
    }

    private AppUser currentAdmin() {
        return currentUser();
    }
}
