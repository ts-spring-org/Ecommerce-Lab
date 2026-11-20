package ms.identity.adapter.out.persistence;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ms.identity.application.port.out.OutboxRepositoryPort;

@Repository
public class JdbcOutboxRepository implements OutboxRepositoryPort {
	private final JdbcTemplate jdbc;

	public JdbcOutboxRepository(final JdbcTemplate jdbc) {
		this.jdbc = jdbc;
	}

	@Override
	public void enqueue(final UUID id, final String aggregateType, final UUID aggregateId, final String eventType,
			final String payload, final Instant occurredAt) {
		jdbc.update(
				"INSERT INTO outbox_events (id,aggregate_type,aggregate_id,event_type,payload,occurred_at,next_attempt_at) VALUES (?,?,?,?,?::jsonb,?,?)",
				id, aggregateType, aggregateId, eventType, payload, JdbcConverters.toTimestamp(occurredAt), JdbcConverters.toTimestamp(occurredAt));
	}

	@Override
	public List<OutboxEvent> claimPending(final Instant now, final int limit) {
		return jdbc.query(
				"SELECT id,event_type,payload::text AS payload,attempts FROM outbox_events WHERE published_at IS NULL AND next_attempt_at<=? ORDER BY occurred_at LIMIT ? FOR UPDATE SKIP LOCKED",
				(rs, row) -> new OutboxEvent(rs.getObject("id", UUID.class), rs.getString("event_type"),
						rs.getString("payload"), rs.getInt("attempts")),
				JdbcConverters.toTimestamp(now), limit);
	}

	@Override
	public void markPublished(final UUID id, final Instant at) {
		jdbc.update("UPDATE outbox_events SET published_at=?, payload='{}'::jsonb WHERE id=?", JdbcConverters.toTimestamp(at), id);
	}

	@Override
	public void scheduleRetry(final UUID id, final int attempts, final Instant nextAttempt, final String error) {
		jdbc.update("UPDATE outbox_events SET attempts=?, next_attempt_at=?, last_error=? WHERE id=?", attempts,
				JdbcConverters.toTimestamp(nextAttempt), error == null ? null : error.substring(0, Math.min(1000, error.length())), id);
	}
}
