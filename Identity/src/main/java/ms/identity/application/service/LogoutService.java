package ms.identity.application.service;

import java.time.Clock;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ms.identity.application.port.in.LogoutUseCase;
import ms.identity.application.port.out.RefreshTokenGeneratorPort;
import ms.identity.application.port.out.RefreshTokenRepositoryPort;

@Service
public class LogoutService implements LogoutUseCase {
    private final RefreshTokenRepositoryPort tokens;
    private final RefreshTokenGeneratorPort generator;
    private final Clock clock;
    public LogoutService(RefreshTokenRepositoryPort tokens, RefreshTokenGeneratorPort generator, Clock clock) {
        this.tokens=tokens; this.generator=generator; this.clock=clock;
    }

    @Override @Transactional
    public void logout(String rawRefreshToken, String expectedClientType) {
        if (rawRefreshToken==null || rawRefreshToken.isBlank()) return;
        String client=LoginService.normalizeClient(expectedClientType);
        Instant now=Instant.now(clock);
        tokens.findByHashForUpdate(generator.hash(rawRefreshToken)).ifPresent(token -> {
            if (token.clientType().equals(client)) tokens.revokeFamily(token.familyId(),now);
        });
    }
}
