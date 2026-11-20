package ms.identity.application.service;

import java.time.Clock;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ms.identity.application.exception.InvalidVerificationTokenException;
import ms.identity.application.port.in.VerifyEmailUseCase;
import ms.identity.application.port.out.EmailVerificationRepositoryPort;
import ms.identity.application.port.out.RefreshTokenGeneratorPort;
import ms.identity.application.port.out.UserRepositoryPort;
import ms.identity.domain.model.UserStatus;

@Service
public class VerifyEmailService implements VerifyEmailUseCase {
	private final EmailVerificationRepositoryPort tokens;
	private final RefreshTokenGeneratorPort generator;
	private final UserRepositoryPort users;
	private final Clock clock;

	public VerifyEmailService(final EmailVerificationRepositoryPort tokens, final RefreshTokenGeneratorPort generator,
			final UserRepositoryPort users, final Clock clock) {
		this.tokens = tokens;
		this.generator = generator;
		this.users = users;
		this.clock = clock;
	}

	@Override
	@Transactional
	public void verify(final String rawToken) {
		if (rawToken == null || rawToken.isBlank())
			throw new InvalidVerificationTokenException();
		Instant now = Instant.now(clock);
		var token = tokens.findByHashForUpdate(generator.hash(rawToken))
				.orElseThrow(InvalidVerificationTokenException::new);
		if (token.consumedAt() != null || !token.expiresAt().isAfter(now))
			throw new InvalidVerificationTokenException();
		var user = users.findById(token.userId()).orElseThrow(InvalidVerificationTokenException::new);
		if (user.getStatus() != UserStatus.PENDING_VERIFICATION)
			throw new InvalidVerificationTokenException();
		tokens.consume(token.id(), now);
		user.activate(now);
		users.save(user);
	}
}
