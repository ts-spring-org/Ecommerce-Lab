package ms.identity.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class EmailAddressTest {

	final String ACTUAL_EMAIL = "user@example.com";

	@Test
	void shouldCreateUsingFactoryMethod() {
		final EmailAddress email = EmailAddress.of(ACTUAL_EMAIL);
		assertEquals(ACTUAL_EMAIL, email.value());

	}

	@Test
	void shouldNormalizeEmail() {
		final EmailAddress email = EmailAddress.of("UsEr@eXamplE.cOm");
		assertEquals(ACTUAL_EMAIL, email.value());
	}

	@Test
	void shouldFailsEmailIsNull() {
		assertThrows(IllegalArgumentException.class, () -> EmailAddress.of(null));
	}

	@Test
	void shouldFailWhenEmailIsBlank() {
		assertThrows(IllegalArgumentException.class, () -> EmailAddress.of(" "));
	}

	@Test
	void shouldFailWhenEmailHasNoAtSymbol() {
		assertThrows(IllegalArgumentException.class, () -> EmailAddress.of("testexample.com"));
	}

	@Test
	void shouldTrimAndNormalizeEmail() {
		EmailAddress email = EmailAddress.of("  USER@EXAMPLE.COM  ");
		assertEquals("user@example.com", email.value());
	}

	@Test
	void shouldBeEqualAfterNormalization() {
		EmailAddress first = EmailAddress.of("USER@EXAMPLE.COM");
		EmailAddress second = EmailAddress.of("user@example.com");

		assertEquals(first, second);
	}
	
	@Test
	void shouldRejectInvalidEmail() {
		assertThrows(IllegalArgumentException.class, () -> EmailAddress.of("1@test,cp"));
	}
}
