package ms.identity.adapter.out.security;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import ms.identity.application.port.out.AccessTokenProviderPort;

@Component
public class JwtAccessTokenProvider implements AccessTokenProviderPort {
    private final JwtEncoder encoder;
    private final String issuer;
    private final String audience;
    private final long accessTokenTtlSeconds;

    public JwtAccessTokenProvider(
            JwtEncoder encoder,
            @Value("${identity.security.jwt.issuer}") String issuer,
            @Value("${identity.security.jwt.audience}") String audience,
            @Value("${identity.security.jwt.access-token-ttl}") java.time.Duration accessTokenTtl) {
        this.encoder = encoder;
        this.issuer = issuer;
        this.audience = audience;
        this.accessTokenTtlSeconds = accessTokenTtl.toSeconds();
        if (accessTokenTtlSeconds <= 0) {
            throw new IllegalArgumentException("Access token TTL must be positive");
        }
    }

    @Override
    public String issue(UUID userId, List<String> authorities) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .subject(userId.toString())
                .audience(List.of(audience))
                .issuedAt(now)
                .expiresAt(now.plusSeconds(accessTokenTtlSeconds))
                .id(UUID.randomUUID().toString())
                .claim("roles", List.copyOf(authorities))
                .build();
        return encoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }
}
