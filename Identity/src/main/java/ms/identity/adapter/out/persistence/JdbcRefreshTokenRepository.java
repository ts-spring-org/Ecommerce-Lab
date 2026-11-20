package ms.identity.adapter.out.persistence;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ms.identity.application.port.out.RefreshTokenRepositoryPort;

@Repository
public class JdbcRefreshTokenRepository implements RefreshTokenRepositoryPort {
	private final JdbcTemplate jdbc;

	public JdbcRefreshTokenRepository(final JdbcTemplate jdbc) {
		this.jdbc = jdbc;
	}

	private static final RowMapper<StoredRefreshToken> MAPPER = (rs, row) -> new StoredRefreshToken(
			rs.getObject("id", UUID.class), rs.getObject("user_id", UUID.class), rs.getObject("family_id", UUID.class),
			rs.getString("token_hash"), rs.getString("client_type"), rs.getTimestamp("expires_at").toInstant(),
			instant(rs, "consumed_at"), instant(rs, "revoked_at"));

	private static Instant instant(final ResultSet rs, final String column) throws SQLException {
		var value = rs.getTimestamp(column);
		return value == null ? null : value.toInstant();
	}

	@Override
	public void insert(final UUID id, final UUID userId, final UUID familyId, final String tokenHash,
			final String clientType, final Instant createdAt, final Instant expiresAt) {
		jdbc.update(
				"INSERT INTO refresh_tokens (id,user_id,family_id,token_hash,client_type,created_at,expires_at) VALUES (?,?,?,?,?,?,?)",
				id, userId, familyId, tokenHash, clientType, JdbcConverters.toTimestamp(createdAt),
				JdbcConverters.toTimestamp(expiresAt));
	}

	@Override
	public Optional<StoredRefreshToken> findByHashForUpdate(final String hash) {
		return jdbc.query(
				"SELECT id,user_id,family_id,token_hash,client_type,expires_at,consumed_at,revoked_at FROM refresh_tokens WHERE token_hash=? FOR UPDATE",
				MAPPER, hash).stream().findFirst();
	}

	@Override
	public void consume(final UUID id, final Instant at, final UUID replacementId) {
		int rows = jdbc.update(
				"UPDATE refresh_tokens SET consumed_at=?,replaced_by_token_id=? WHERE id=? AND consumed_at IS NULL AND revoked_at IS NULL",
				JdbcConverters.toTimestamp(at), replacementId, id);
		if (rows != 1)
			throw new IllegalStateException("Refresh token is no longer active");
	}

	@Override
	public void revokeFamily(final UUID familyId, final Instant at) {
		jdbc.update("UPDATE refresh_tokens SET revoked_at=? WHERE family_id=? AND revoked_at IS NULL",
				JdbcConverters.toTimestamp(at), familyId);
	}

	@Override
	public void revokeAllForUser(final UUID userId, final Instant at) {
		jdbc.update("UPDATE refresh_tokens SET revoked_at=? WHERE user_id=? AND revoked_at IS NULL",
				JdbcConverters.toTimestamp(at), userId);
	}
}
