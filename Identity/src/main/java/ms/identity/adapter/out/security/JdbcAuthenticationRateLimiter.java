package ms.identity.adapter.out.security;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class JdbcAuthenticationRateLimiter {
    private final JdbcTemplate jdbc;
    public JdbcAuthenticationRateLimiter(JdbcTemplate jdbc) { this.jdbc=jdbc; }

    public boolean allow(String bucketKey,Instant now,int maximum,Duration window) {
        long seconds=window.toSeconds();
        Timestamp blockedUntil=jdbc.queryForObject("""
                INSERT INTO auth_rate_limits (bucket_key,window_started_at,request_count,blocked_until,updated_at)
                VALUES (?, ?, 1, NULL, ?)
                ON CONFLICT (bucket_key) DO UPDATE SET
                    window_started_at = CASE
                        WHEN auth_rate_limits.window_started_at + (? * INTERVAL '1 second') <= ?
                        THEN ? ELSE auth_rate_limits.window_started_at END,
                    request_count = CASE
                        WHEN auth_rate_limits.blocked_until > ? THEN auth_rate_limits.request_count
                        WHEN auth_rate_limits.window_started_at + (? * INTERVAL '1 second') <= ? THEN 1
                        ELSE auth_rate_limits.request_count + 1 END,
                    blocked_until = CASE
                        WHEN auth_rate_limits.blocked_until > ? THEN auth_rate_limits.blocked_until
                        WHEN auth_rate_limits.window_started_at + (? * INTERVAL '1 second') <= ? THEN NULL
                        WHEN auth_rate_limits.request_count + 1 > ? THEN ? + (? * INTERVAL '1 second')
                        ELSE NULL END,
                    updated_at = ?
                RETURNING blocked_until
                """, (rs,row)->rs.getTimestamp("blocked_until"),
                bucketKey,Timestamp.from(now),Timestamp.from(now),
                seconds,Timestamp.from(now),Timestamp.from(now),
                Timestamp.from(now),seconds,Timestamp.from(now),
                Timestamp.from(now),seconds,Timestamp.from(now),maximum,
                Timestamp.from(now),300L,Timestamp.from(now));
        return blockedUntil==null || !blockedUntil.toInstant().isAfter(now);
    }

    public int deleteExpired(Instant cutoff) {
        return jdbc.update("DELETE FROM auth_rate_limits WHERE updated_at < ?",Timestamp.from(cutoff));
    }
}
