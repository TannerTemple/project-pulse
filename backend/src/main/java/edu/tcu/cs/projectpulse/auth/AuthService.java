package edu.tcu.cs.projectpulse.auth;

import edu.tcu.cs.projectpulse.auth.dto.LoginRequest;
import edu.tcu.cs.projectpulse.auth.dto.LoginResponse;
import edu.tcu.cs.projectpulse.auth.dto.RegisterRequest;
import edu.tcu.cs.projectpulse.invitation.InvitationToken;
import edu.tcu.cs.projectpulse.invitation.InvitationTokenRepository;
import edu.tcu.cs.projectpulse.user.AppUser;
import edu.tcu.cs.projectpulse.user.UserRepository;
import edu.tcu.cs.projectpulse.user.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authManager;
    private final AppUserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final InvitationTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;

    /** Authenticate email/password and return a signed JWT. */
    public LoginResponse login(LoginRequest request) {
        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        AppUserDetails userDetails =
                (AppUserDetails) userDetailsService.loadUserByUsername(request.email());

        String jwt = jwtService.generateToken(userDetails);

        return new LoginResponse(
                jwt,
                userDetails.getId(),
                userDetails.getFirstName(),
                userDetails.getLastName(),
                userDetails.getRole()
        );
    }

    /**
     * Complete registration via an invitation token (UC-25, UC-30).
     * Creates the AppUser, marks the token as used, and returns a JWT so the
     * user is immediately logged in after registering.
     */
    @Transactional
    public LoginResponse register(RegisterRequest request) {
        if (!request.password().equals(request.confirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match.");
        }

        InvitationToken invite = tokenRepository.findByToken(request.token())
                .orElseThrow(() -> new IllegalArgumentException("Invalid invitation token."));

        if (invite.isUsed()) {
            throw new IllegalArgumentException("This invitation link has already been used.");
        }
        if (invite.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("This invitation link has expired.");
        }
        if (userRepository.existsByEmail(invite.getEmail())) {
            throw new IllegalArgumentException("An account with this email already exists.");
        }

        AppUser user = new AppUser();
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setEmail(invite.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(invite.getRole());
        user.setActive(true);
        user.setRegistrationComplete(true);

        if (invite.getRole() == UserRole.INSTRUCTOR && request.middleInitial() != null) {
            user.setMiddleInitial(request.middleInitial());
        }

        // Bind students to the section they were invited to
        if (invite.getRole() == UserRole.STUDENT && invite.getSection() != null) {
            user.setSection(invite.getSection());
        }

        userRepository.save(user);

        invite.setUsed(true);
        tokenRepository.save(invite);

        AppUserDetails userDetails = new AppUserDetails(user);
        String jwt = jwtService.generateToken(userDetails);

        return new LoginResponse(
                jwt,
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole()
        );
    }
}
