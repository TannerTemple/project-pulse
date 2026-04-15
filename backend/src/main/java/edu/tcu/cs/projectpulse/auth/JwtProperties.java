package edu.tcu.cs.projectpulse.auth;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "jwt")
@Getter
@Setter
public class JwtProperties {

    /** Base64-encoded HMAC-SHA256 signing secret. */
    private String secret;

    /** Token lifetime in milliseconds. Default: 24 hours. */
    private long expirationMs = 86_400_000L;
}
