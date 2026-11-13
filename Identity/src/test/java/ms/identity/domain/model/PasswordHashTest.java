package ms.identity.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordHashTest {

	@Test
	void shouldCreatePasswordHash() {

		PasswordHash passwordHash = new PasswordHash("encoded-value");

		assertEquals("encoded-value", passwordHash.encodedValue());
	}

	@Test
	void shouldRejectNullPasswordHash() {

		assertThrows(NullPointerException.class, () -> new PasswordHash(null));
	}

	@Test
	void shouldRejectBlankPasswordHash() {

		assertThrows(IllegalArgumentException.class, () -> new PasswordHash(" "));
	}

	@Test
	void shouldNotExposeHashInToString() {

		PasswordHash passwordHash = new PasswordHash("secret-encoded-value");

		assertEquals("PasswordHash[REDACTED]", passwordHash.toString());
	}
}