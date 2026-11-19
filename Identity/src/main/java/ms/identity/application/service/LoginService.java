package ms.identity.application.service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ms.identity.application.command.LoginCommand;
import ms.identity.application.exception.InvalidCredentialsException;
import ms.identity.application.port.in.LoginUseCase;
import ms.identity.application.port.out.AccessTokenProviderPort;
import ms.identity.application.port.out.PasswordHasherPort;
import ms.identity.application.port.out.RefreshTokenGeneratorPort;
import ms.identity.application.port.out.RefreshTokenRepositoryPort;
import ms.identity.application.port.out.UserRepositoryPort;
import ms.identity.application.results.TokenPair;
import ms.identity.domain.model.EmailAddress;
import ms.identity.domain.model.User;
import ms.identity.domain.model.UserStatus;

@Service
public class LoginService implements LoginUseCase {
    private final UserRepositoryPort users;
    private final PasswordHasherPort passwords;
    private final AccessTokenProviderPort accessTokens;
    private final RefreshTokenGeneratorPort refreshTokens;
    private final RefreshTokenRepositoryPort refreshRepository;
    private final Clock clock;
    private final Duration refreshTtl;
    private final Duration accessTtl;

    public LoginService(UserRepositoryPort users, PasswordHasherPort passwords,
            AccessTokenProviderPort accessTokens, RefreshTokenGeneratorPort refreshTokens,
            RefreshTokenRepositoryPort refreshRepository, Clock clock,
            @Value("${identity.security.refresh-token.ttl}") Duration refreshTtl,
            @Value("${identity.security.jwt.access-token-ttl}") Duration accessTtl) {
        this.users=users; this.passwords=passwords; this.accessTokens=accessTokens;
        this.refreshTokens=refreshTokens; this.refreshRepository=refreshRepository;
        this.clock=clock; this.refreshTtl=refreshTtl; this.accessTtl=accessTtl;
    }

    @Override @Transactional
    public TokenPair login(LoginCommand command) {
        String client = normalizeClient(command.clientType());
        User user = users.findByEmail(EmailAddress.of(command.email()))
                .orElseThrow(InvalidCredentialsException::new);
        if (!passwords.matches(command.password(), user.getPasswordHash().encodedValue()))
            throw new InvalidCredentialsException();
        if (user.getStatus() != UserStatus.ACTIVE) throw new InvalidCredentialsException();

        Instant now=Instant.now(clock);
        UUID family=UUID.randomUUID(), id=UUID.randomUUID();
        String raw=refreshTokens.generate();
        refreshRepository.insert(id,user.getId(),family,refreshTokens.hash(raw),client,now,now.plus(refreshTtl));
        return new TokenPair(accessTokens.issue(user.getId(), List.of("ROLE_USER")),raw,accessTtl.toSeconds());
    }

    static String normalizeClient(String client) {
        if (client == null) throw new IllegalArgumentException("Client type is required");
        String normalized=client.trim().toUpperCase(java.util.Locale.ROOT);
        if (!normalized.equals("WEB") && !normalized.equals("MOBILE"))
            throw new IllegalArgumentException("Client type must be WEB or MOBILE");
        return normalized;
    }
}
