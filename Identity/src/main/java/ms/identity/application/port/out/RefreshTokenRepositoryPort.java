package ms.identity.application.port.out;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepositoryPort {
	record StoredRefreshToken(UUID id, UUID userId, UUID familyId, String tokenHash, String clientType,
			Instant expiresAt, Instant consumedAt, Instant revokedAt) {
		public boolean activeAt(final Instant now) {
			return consumedAt == null && revokedAt == null && expiresAt.isAfter(now);
		}
	}

	void insert(final UUID id, final UUID userId, final UUID familyId, final String tokenHash, final String clientType,
			final Instant createdAt, final Instant expiresAt);

	Optional<StoredRefreshToken> findByHashForUpdate(final String tokenHash);

	void consume(final UUID id, final Instant consumedAt, final UUID replacementId);

	void revokeFamily(final UUID familyId, final Instant revokedAt);

	void revokeAllForUser(final UUID userId, final Instant revokedAt);
}
