package ms.identity.domain.model;

import org.junit.jupiter.api.Test;

import ms.identity.domain.exception.InvalidUserStateException;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.util.UUID;

class UserTest {
	private static final UUID ID = UUID.randomUUID();
	private static final Instant NOW = Instant.parse("2026-11-01T10:00:00Z");
	private static final Instant BLOCKED_TIME = Instant.parse("2026-12-01T12:30:00Z");
	private static final Instant UNBLOCKED_TIME = Instant.parse("2026-12-10T10:00:00Z");
	private static final Instant ACTIVATION_TIME = Instant.parse("2026-11-01T12:30:00Z");
	private static final EmailAddress EMAIL = EmailAddress.of(" user@EXAMPLE.com ");
	private static final PasswordHash PASSWORD_HASH = new PasswordHash("encoded-hash");
	private static final String FIRST_NAME = "TestFirstname";
	private static final String LAST_NAME = "TestLastname";

	// region User Registration

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
	void shouldRejectNullId() {
		assertThrows(NullPointerException.class,
				() -> User.register(null, EMAIL, PASSWORD_HASH, FIRST_NAME, LAST_NAME, NOW));
	}

	@Test
	void shouldRejectNullEmail() {
		assertThrows(NullPointerException.class,
				() -> User.register(ID, null, PASSWORD_HASH, FIRST_NAME, LAST_NAME, NOW));
	}

	@Test
	void shouldRejectNullPasswordHash() {
		assertThrows(NullPointerException.class, () -> User.register(ID, EMAIL, null, FIRST_NAME, LAST_NAME, NOW));
	}

	@Test
	void shouldRejectNullCreationTime() {
		assertThrows(NullPointerException.class,
				() -> User.register(ID, EMAIL, PASSWORD_HASH, FIRST_NAME, LAST_NAME, null));
	}

	@Test
	void shouldRejectBlankFirstName() {
		assertThrows(IllegalArgumentException.class, () -> User.register(ID, EMAIL, PASSWORD_HASH, "", LAST_NAME, NOW));
	}

	@Test
	void shouldRejectBlankLastName() {

		assertThrows(IllegalArgumentException.class,
				() -> User.register(ID, EMAIL, PASSWORD_HASH, FIRST_NAME, "", NOW));
	}

	@Test
	void shouldRejectNullFirstName() {
		assertThrows(IllegalArgumentException.class,
				() -> User.register(ID, EMAIL, PASSWORD_HASH, null, LAST_NAME, NOW));
	}

	@Test
	void shouldRejectNullLastName() {
		assertThrows(IllegalArgumentException.class, () -> User.register(ID, new EmailAddress("user@example.com"),
				new PasswordHash("hashed"), "Anna", null, NOW));
	}

	@Test
	void shouldRejectLastNameLongerThan100Characters() {

		String lastName = "A".repeat(101);

		assertThrows(IllegalArgumentException.class,
				() -> User.register(ID, EMAIL, PASSWORD_HASH, FIRST_NAME, lastName, NOW));
	}

	@Test
	void shouldAcceptNameWithExactly100Characters() {

		String name = "A".repeat(100);

		final User user = User.register(ID, EMAIL, PASSWORD_HASH, name, name, NOW);

		assertEquals(name, user.getFirstName());
		assertEquals(name, user.getLastName());
	}

	@Test
	void shouldTrimNames() {
		User user = createUser();

		assertEquals("TestFirstname", user.getFirstName());
		assertEquals("TestLastname", user.getLastName());
	}

	// endregion

	// region User Activation

	@Test
	void shouldActivateUserStatus() {

		User user = createUser();

		user.activate(NOW);

		assertEquals(UserStatus.ACTIVE, user.getStatus());
	}

	@Test
	void shouldUpdateTimestampWhenUserIsActivated() {

		User user = createUser();

		user.activate(ACTIVATION_TIME);

		assertEquals(ACTIVATION_TIME, user.getUpdatedAt());
	}

	@Test
	void shouldRejectNullActivationTime() {

		User user = createUser();

		assertThrows(NullPointerException.class, () -> user.activate(null));
	}

	@Test
	void shouldRejectActivationWhenUserIsAlreadyActive() {

		User user = createUser();
		user.activate(ACTIVATION_TIME);

		assertThrows(InvalidUserStateException.class, () -> user.activate(BLOCKED_TIME));
	}

	// endregion

	// region User Blocking / Unblocking
	@Test
	void shouldLockUserAccount() {

		User user = createUser();
		user.activate(ACTIVATION_TIME);
		user.block(BLOCKED_TIME);

		assertEquals(UserStatus.BLOCKED, user.getStatus());
		assertEquals(BLOCKED_TIME, user.getUpdatedAt());
	}

	@Test
	void shouldRejectLockWhenUserIsPendingVerification() {

		User user = createUser();

		assertThrows(InvalidUserStateException.class, () -> user.block(BLOCKED_TIME));
	}

	@Test
	void shouldRejectNullBlockTime() {
		User user = createUser();
		user.activate(ACTIVATION_TIME);
		assertThrows(NullPointerException.class, () -> user.block(null));
	}

	@Test
	void shouldUnLockUserAccount() {

		User user = createUser();
		user.activate(ACTIVATION_TIME);
		user.block(BLOCKED_TIME);
		user.unblock(UNBLOCKED_TIME);

		assertEquals(UserStatus.ACTIVE, user.getStatus());
		assertEquals(UNBLOCKED_TIME, user.getUpdatedAt());
	}
	
	@Test
	void shouldRejectUnlockWhenUserIsNotBlocked() {

		User user = createUser();

		assertThrows(InvalidUserStateException.class, () -> user.unblock(UNBLOCKED_TIME));
	}

	@Test
	void shouldRejectNullUnblockTime() {
		User user = createUser();
		user.activate(ACTIVATION_TIME);
		user.block(BLOCKED_TIME);

		assertThrows(NullPointerException.class, () -> user.unblock(null));
	}
	
	//endregion

	// region Timestamp Rules

	@Test
	void shouldRejectTransitionTimeBeforeLastUpdate() {

		User user = createUser();

		user.activate(ACTIVATION_TIME);

		Instant olderTime = Instant.parse("2026-10-01T10:00:00Z");

		assertThrows(IllegalArgumentException.class, () -> user.block(olderTime));

		assertEquals(UserStatus.ACTIVE, user.getStatus());

		assertEquals(ACTIVATION_TIME, user.getUpdatedAt());
	}

	@Test
	void shouldNotUpdateStateWhenBlockFails() {

		User user = createUser();

		assertThrows(InvalidUserStateException.class, () -> user.block(BLOCKED_TIME));

		assertEquals(UserStatus.PENDING_VERIFICATION, user.getStatus());
		assertEquals(NOW, user.getUpdatedAt());
	}
	
	// endregion

	// region Equality & HashCode
	@Test
	void usersWithSameIdShouldBeEqual() {
		User first = createUser();
		User second = User.register(ID, EMAIL, PASSWORD_HASH, "test", "test", NOW);

		assertEquals(first, second);
		assertEquals(first.hashCode(), second.hashCode());
	}

	@Test
	void usersWithDifferentIdsShouldNotBeEqual() {

		User first = createUser();

		User second = User.register(UUID.randomUUID(), EMAIL, PASSWORD_HASH, FIRST_NAME, LAST_NAME, NOW);

		assertNotEquals(first, second);
	}

	@Test
	void userShouldNotBeEqualToNull() {

		User user = createUser();

		assertNotEquals(user, null);
	}	

	@Test
	void shouldRejectFirstNameLongerThan100Characters() {

		String firstName = "A".repeat(101);

		assertThrows(IllegalArgumentException.class,
				() -> User.register(ID, EMAIL, PASSWORD_HASH, firstName, LAST_NAME, NOW));
	}
	
	@Test
	void shouldAcceptTransitionAtExactlyLastUpdateTime() {

	    User user = createUser();

	    user.activate(ACTIVATION_TIME);

	    user.block(ACTIVATION_TIME);

	    assertEquals(UserStatus.BLOCKED, user.getStatus());

	    assertEquals(ACTIVATION_TIME, user.getUpdatedAt());
	}
	
	private User createUser() {
		return User.register(ID, EMAIL, PASSWORD_HASH, FIRST_NAME, LAST_NAME, NOW);
	}
}