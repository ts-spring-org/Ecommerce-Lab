package ms.identity.adapter.out.persistence;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ms.identity.application.port.out.EmailVerificationRepositoryPort;

@Repository
public class JdbcEmailVerificationRepository implements EmailVerificationRepositoryPort {
	private final JdbcTemplate jdbc;

	public JdbcEmailVerificationRepository(JdbcTemplate jdbc) {
		this.jdbc = jdbc;
	}

	@Override
	public void invalidateOutstanding(final UUID userId, final Instant at) {
		jdbc.update("UPDATE email_verification_tokens SET consumed_at=? WHERE user_id=? AND consumed_at IS NULL", at,
				userId);
	}

	@Override
	public void insert(final UUID id, final UUID userId, final String hash, final Instant created,
			final Instant expires) {
		jdbc.update(
				"INSERT INTO email_verification_tokens (id,user_id,token_hash,created_at,expires_at) VALUES (?,?,?,?,?)",
				id, userId, hash, created, expires);
	}

	@Override
	public Optional<VerificationToken> findByHashForUpdate(final String hash) {
		return jdbc.query(
				"SELECT id,user_id,expires_at,consumed_at FROM email_verification_tokens WHERE token_hash=? FOR UPDATE",
				(rs, row) -> new VerificationToken(rs.getObject("id", UUID.class), rs.getObject("user_id", UUID.class),
						rs.getTimestamp("expires_at").toInstant(),
						rs.getTimestamp("consumed_at") == null ? null : rs.getTimestamp("consumed_at").toInstant()),
				hash).stream().findFirst();
	}

	@Override
	public void consume(final UUID id, final Instant at) {
		int rows = jdbc.update("UPDATE email_verification_tokens SET consumed_at=? WHERE id=? AND consumed_at IS NULL",
				at, id);
		if (rows != 1)
			throw new IllegalStateException("Verification token was already consumed");
	}
}
