package ms.identity.adapter.egress.persistence;

import java.sql.Timestamp;
import java.time.Instant;

public final class JdbcConverters {
	 private JdbcConverters() {
	    }

	    public static Timestamp toTimestamp(final Instant instant) {
	        return instant == null ? null : Timestamp.from(instant);
	    }
}
