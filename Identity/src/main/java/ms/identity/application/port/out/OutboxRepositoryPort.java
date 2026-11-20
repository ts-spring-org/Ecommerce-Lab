package ms.identity.application.port.out;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface OutboxRepositoryPort {
	record OutboxEvent(UUID id, String eventType, String payload, int attempts) {
	}

	void enqueue(final UUID id, final String aggregateType, final UUID aggregateId, final String eventType, final String payload, final Instant occurredAt);

	List<OutboxEvent> claimPending(final Instant now, final int limit);

	void markPublished(final UUID id, final Instant at);

	void scheduleRetry(final UUID id, final int attempts, final Instant nextAttempt, final String error);
}
