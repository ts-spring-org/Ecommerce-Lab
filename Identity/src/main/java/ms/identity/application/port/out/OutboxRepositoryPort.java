package ms.identity.application.port.out;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
public interface OutboxRepositoryPort {
    record OutboxEvent(UUID id, String eventType, String payload, int attempts) {}
    void enqueue(UUID id, String aggregateType, UUID aggregateId, String eventType, String payload, Instant occurredAt);
    List<OutboxEvent> claimPending(Instant now, int limit);
    void markPublished(UUID id, Instant at);
    void scheduleRetry(UUID id, int attempts, Instant nextAttempt, String error);
}
