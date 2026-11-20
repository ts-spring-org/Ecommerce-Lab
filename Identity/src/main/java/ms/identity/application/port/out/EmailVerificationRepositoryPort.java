package ms.identity.application.port.out;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface EmailVerificationRepositoryPort {
	record VerificationToken(UUID id, UUID userId, Instant expiresAt, Instant consumedAt) {
	}

	void invalidateOutstanding(final UUID userId, final Instant at);

	void insert(final UUID id, final UUID userId, final String tokenHash, final Instant createdAt,
			final Instant expiresAt);

	Optional<VerificationToken> findByHashForUpdate(final String tokenHash);

	void consume(final UUID id, final Instant at);
}
