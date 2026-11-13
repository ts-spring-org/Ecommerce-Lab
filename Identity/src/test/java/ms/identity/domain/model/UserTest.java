package ms.identity.domain.model;

import org.junit.jupiter.api.Test;

import ms.identity.domain.exception.InvalidUserStateException;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.util.UUID;

class UserTest {
	private static final UUID ID = UUID.randomUUID();
	private static final Instant NOW = Instant.parse("2026-11-01T10:00:00Z");
	private static final Instant LOCKED_TIME = Instant.parse("2026-12-01T12:30:00Z");
	private static final Instant UNLOCKED_TIME = Instant.parse("2026-12-10T10:00:00Z");
	private static final Instant ACTIVATION_TIME = Instant.parse("2026-11-01T12:30:00Z");
	private static final EmailAddress EMAIL = EmailAddress.of(" user@EXAMPLE.com ");
	private static final PasswordHash PASSWORD_HASH = new PasswordHash("encoded-hash");
	private static final String FIRST_NAME = "TestFirstname";
	private static final String LAST_NAME = "TestLastname";

	@Test
	void shouldRegisterUserWithPendingVerificationStatus() throws Exception {
		User user = createUser();

		assertNotNull(user);
		assertEquals(UserStatus.PENDING_VERIFICATION, user.getStatus());
		assertEquals(EMAIL, user.getEmail());
		assertEquals(NOW, user.getCreatedAt());
		assertEquals(NOW, user.getUpdatedAt());
	}

	@Test
	void shouldRejectBlankLastName() {

		assertThrows(IllegalArgumentException.class,
				() -> User.register(ID, EMAIL, PASSWORD_HASH, FIRST_NAME, "", NOW));
	}

	@Test
	void shouldRejectNullLastName() {
		assertThrows(IllegalArgumentException.class, () -> User.register(ID, new EmailAddress("user@example.com"),
				new PasswordHash("hashed"), "Anna", null, NOW));
	}

	@Test
	void shouldRejectBlankFirstName() {
		assertThrows(IllegalArgumentException.class, () -> User.register(ID, EMAIL, PASSWORD_HASH, "", LAST_NAME, NOW));
	}

	@Test
	void shouldRejectNullFirstName() {
		assertThrows(IllegalArgumentException.class,
				() -> User.register(ID, EMAIL, PASSWORD_HASH, null, LAST_NAME, NOW));
	}

	@Test
	void shouldTrimNames() {
		User user = createUser();

		assertEquals("TestFirstname", user.getFirstName());
		assertEquals("TestLastname", user.getLastName());
	}

	@Test
	void shouldActivateUserStatus() {

		User user = createUser();

		user.activate(NOW);

		assertEquals(UserStatus.ACTIVE, user.getStatus());
	}

	@Test
	void shouldLockUserAccount() {

		User user = createUser();
		user.activate(ACTIVATION_TIME);
		user.block(LOCKED_TIME);

		assertEquals(UserStatus.BLOCKED, user.getStatus());
		assertEquals(LOCKED_TIME, user.getUpdatedAt());
	}

	@Test
	void shouldUnLockUserAccount() {

		User user = createUser();
		user.activate(ACTIVATION_TIME);
		user.block(LOCKED_TIME);
		user.unblock(UNLOCKED_TIME);

		assertEquals(UserStatus.ACTIVE, user.getStatus());
		assertEquals(UNLOCKED_TIME, user.getUpdatedAt());
	}

	@Test
	void shouldRejectNullId() {
		assertThrows(NullPointerException.class,
				() -> User.register(null, EMAIL, PASSWORD_HASH, FIRST_NAME, LAST_NAME, NOW));
	}

	@Test
	void usersWithSameIdShouldBeEqual() {
		User first = createUser();
		User second = User.register(ID, EMAIL, PASSWORD_HASH, "test", "test", NOW);

		assertEquals(first, second);
		assertEquals(first.hashCode(), second.hashCode());
	}

	@Test
	void shouldRejectActivationWhenUserIsAlreadyActive() {

		User user = createUser();
		user.activate(ACTIVATION_TIME);

		assertThrows(InvalidUserStateException.class, () -> user.activate(LOCKED_TIME));
	}

	@Test
	void shouldRejectLockWhenUserIsPendingVerification() {

		User user = createUser();

		assertThrows(InvalidUserStateException.class, () -> user.block(LOCKED_TIME));
	}

	@Test
	void shouldRejectUnlockWhenUserIsNotBlocked() {

		User user = createUser();

		assertThrows(InvalidUserStateException.class, () -> user.unblock(UNLOCKED_TIME));
	}

	@Test
	void shouldRejectNullActivationTime() {

		User user = createUser();

		assertThrows(NullPointerException.class, () -> user.activate(null));
	}

	@Test
	void shouldRejectNameLongerThan100Characters() {

		String longName = "A".repeat(101);

		assertThrows(IllegalArgumentException.class,
				() -> User.register(ID, EMAIL, PASSWORD_HASH, longName, LAST_NAME, NOW));
	}

	@Test
	void shouldNotUpdateTimestampWhenActivationFails() {

		User user = createUser();

		assertThrows(InvalidUserStateException.class, () -> user.block(LOCKED_TIME));

		assertEquals(UserStatus.PENDING_VERIFICATION, user.getStatus());
		assertEquals(NOW, user.getUpdatedAt());
	}

	@Test
	void shouldRejectTransitionTimeBeforeLastUpdate() {

		User user = createUser();

		user.activate(ACTIVATION_TIME);

		Instant olderTime = Instant.parse("2026-10-01T10:00:00Z");

		assertThrows(IllegalArgumentException.class, () -> user.block(olderTime));

		assertEquals(UserStatus.ACTIVE, user.getStatus());

		assertEquals(ACTIVATION_TIME, user.getUpdatedAt());
	}

	private User createUser() {
		return User.register(ID, EMAIL, PASSWORD_HASH, FIRST_NAME, LAST_NAME, NOW);
	}
}