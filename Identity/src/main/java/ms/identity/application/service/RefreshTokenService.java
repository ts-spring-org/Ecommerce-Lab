package ms.identity.application.service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ms.identity.application.exception.InvalidRefreshTokenException;
import ms.identity.application.port.in.RefreshTokenUseCase;
import ms.identity.application.port.out.AccessTokenProviderPort;
import ms.identity.application.port.out.RefreshTokenGeneratorPort;
import ms.identity.application.port.out.RefreshTokenRepositoryPort;
import ms.identity.application.port.out.UserRepositoryPort;
import ms.identity.application.results.TokenPair;
import ms.identity.domain.model.UserStatus;

@Service
public class RefreshTokenService implements RefreshTokenUseCase {
	private final RefreshTokenRepositoryPort tokens;
	private final RefreshTokenGeneratorPort generator;
	private final AccessTokenProviderPort accessTokens;
	private final UserRepositoryPort users;
	private final Clock clock;
	private final Duration refreshTtl;
	private final Duration accessTtl;

	public RefreshTokenService(final RefreshTokenRepositoryPort tokens, final RefreshTokenGeneratorPort generator,
			final AccessTokenProviderPort accessTokens, final UserRepositoryPort users, final Clock clock,
			@Value("${identity.security.refresh-token.ttl}") final Duration refreshTtl,
			@Value("${identity.security.jwt.access-token-ttl}") final Duration accessTtl) {
		this.tokens = tokens;
		this.generator = generator;
		this.accessTokens = accessTokens;
		this.users = users;
		this.clock = clock;
		this.refreshTtl = refreshTtl;
		this.accessTtl = accessTtl;
	}

	@Override
	@Transactional(noRollbackFor = InvalidRefreshTokenException.class)
	public TokenPair refresh(final String rawRefreshToken, final String expectedClientType) {
		if (rawRefreshToken == null || rawRefreshToken.isBlank())
			throw new InvalidRefreshTokenException();
		String client = LoginService.normalizeClient(expectedClientType);
		Instant now = Instant.now(clock);
		var stored = tokens.findByHashForUpdate(generator.hash(rawRefreshToken))
				.orElseThrow(InvalidRefreshTokenException::new);

		if (stored.consumedAt() != null || stored.revokedAt() != null) {
			tokens.revokeFamily(stored.familyId(), now);
			throw new InvalidRefreshTokenException();
		}
		if (!stored.expiresAt().isAfter(now) || !stored.clientType().equals(client))
			throw new InvalidRefreshTokenException();

		var user = users.findById(stored.userId()).orElseThrow(InvalidRefreshTokenException::new);
		if (user.getStatus() != UserStatus.ACTIVE) {
			tokens.revokeFamily(stored.familyId(), now);
			throw new InvalidRefreshTokenException();
		}

		UUID replacementId = UUID.randomUUID();
		String replacement = generator.generate();
		// Insert the replacement first because replaced_by_token_id has an immediate
		// self-FK.
		// Both writes are in this transaction, so a failed consume rolls the insert
		// back.
		tokens.insert(replacementId, user.getId(), stored.familyId(), generator.hash(replacement), client, now,
				now.plus(refreshTtl));
		tokens.consume(stored.id(), now, replacementId);
		return new TokenPair(accessTokens.issue(user.getId(), List.of("ROLE_USER")), replacement,
				accessTtl.toSeconds());
	}
}
