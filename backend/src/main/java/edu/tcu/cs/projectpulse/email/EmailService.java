package edu.tcu.cs.projectpulse.email;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromAddress;

    @Value("${app.base-url}")
    private String baseUrl;

    /**
     * Sends an invitation email to a student (UC-11).
     *
     * @param toEmail       recipient email
     * @param adminName     name of the admin who sent the invite
     * @param adminEmail    admin's email shown in the message
     * @param token         UUID invitation token
     * @param customMessage optional override body; null = use default
     */
    @Async
    public void sendStudentInvitation(String toEmail, String adminName, String adminEmail,
                                      String token, String customMessage) {
        String registrationLink = baseUrl + "/register?token=" + token;
        String body = customMessage != null ? customMessage
                : buildDefaultInvitationBody(adminName, adminEmail, registrationLink);

        send(toEmail,
                "Welcome to The Peer Evaluation Tool - Complete Your Registration",
                body);
    }

    /**
     * Sends an invitation email to an instructor (UC-18).
     */
    @Async
    public void sendInstructorInvitation(String toEmail, String adminName, String adminEmail,
                                         String token, String customMessage) {
        // Instructors use the same registration endpoint; the token role distinguishes them.
        sendStudentInvitation(toEmail, adminName, adminEmail, token, customMessage);
    }

    /**
     * Notifies a student or instructor of their team assignment (UC-12, UC-19).
     */
    @Async
    public void sendTeamAssignment(String toEmail, String firstName, String teamName) {
        String body = "Hello " + firstName + ",\n\n"
                + "You have been assigned to team \"" + teamName + "\" in Project Pulse.\n\n"
                + "Log in at " + baseUrl + " to view your team and start submitting reports.\n\n"
                + "Best regards,\nPeer Evaluation Tool Team";
        send(toEmail, "Project Pulse – Team Assignment", body);
    }

    /**
     * Notifies a user they were removed from a team (UC-13, UC-20).
     */
    @Async
    public void sendTeamRemoval(String toEmail, String firstName, String teamName) {
        String body = "Hello " + firstName + ",\n\n"
                + "You have been removed from team \"" + teamName + "\" in Project Pulse.\n\n"
                + "Please contact your instructor if you have questions.\n\n"
                + "Best regards,\nPeer Evaluation Tool Team";
        send(toEmail, "Project Pulse – Team Removal", body);
    }

    /**
     * Notifies an instructor that their account has been reactivated (UC-24).
     */
    @Async
    public void sendAccountReactivated(String toEmail, String firstName) {
        String body = "Hello " + firstName + ",\n\n"
                + "Your Project Pulse account has been reactivated. You can log in at " + baseUrl + ".\n\n"
                + "Best regards,\nPeer Evaluation Tool Team";
        send(toEmail, "Project Pulse – Account Reactivated", body);
    }

    // ── private helpers ───────────────────────────────────────────────────────

    private void send(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }

    private String buildDefaultInvitationBody(String adminName, String adminEmail, String link) {
        return "Hello,\n\n"
                + adminName + " has invited you to join The Peer Evaluation Tool. "
                + "To complete your registration, please use the link below:\n\n"
                + link + "\n\n"
                + "If you have any questions or need assistance, feel free to contact "
                + adminEmail + " or our team directly.\n\n"
                + "Please note: This email is not monitored, so do not reply directly to this message.\n\n"
                + "Best regards,\nPeer Evaluation Tool Team";
    }
}
