package ms.identity.application.port.out;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepositoryPort {
    record StoredRefreshToken(UUID id, UUID userId, UUID familyId, String tokenHash, String clientType,
                              Instant expiresAt, Instant consumedAt, Instant revokedAt) {
        public boolean activeAt(Instant now) {
            return consumedAt == null && revokedAt == null && expiresAt.isAfter(now);
        }
    }

    void insert(UUID id, UUID userId, UUID familyId, String tokenHash, String clientType,
                Instant createdAt, Instant expiresAt);
    Optional<StoredRefreshToken> findByHashForUpdate(String tokenHash);
    void consume(UUID id, Instant consumedAt, UUID replacementId);
    void revokeFamily(UUID familyId, Instant revokedAt);
    void revokeAllForUser(UUID userId, Instant revokedAt);
}
