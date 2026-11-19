package ms.identity.application.port.out;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
public interface EmailVerificationRepositoryPort {
    record VerificationToken(UUID id, UUID userId, Instant expiresAt, Instant consumedAt) {}
    void invalidateOutstanding(UUID userId, Instant at);
    void insert(UUID id, UUID userId, String tokenHash, Instant createdAt, Instant expiresAt);
    Optional<VerificationToken> findByHashForUpdate(String tokenHash);
    void consume(UUID id, Instant at);
}
