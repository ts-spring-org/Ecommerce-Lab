package ms.identity.domain.model;

import java.util.Objects;

// Make sure that the encoded value received isn't null or blank
public record PasswordHash(String encodedValue) {
	public PasswordHash {
		Objects.requireNonNull(encodedValue, "Password hash cannot be null");

		if (encodedValue.isBlank()) {
			throw new IllegalArgumentException("Password hash cannot be blank.");
		}
	}

	@Override
	public String toString() {
		return "PasswordHash[REDACTED]";
	}
}
