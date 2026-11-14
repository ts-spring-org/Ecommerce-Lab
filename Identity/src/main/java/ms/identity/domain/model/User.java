package ms.identity.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import ms.identity.domain.exception.InvalidUserStateException;

public final class User {

	private final UUID id;
	private final EmailAddress email;
	private final PasswordHash passwordHash;
	private final String firstName;
	private final String lastName;
	private UserStatus status;
	private final Instant createdAt;
	private Instant updatedAt;

	private User(final UUID id, final EmailAddress email, final PasswordHash passwordHash, final String firstName,
			final String lastName, final Instant now) {
		this.id = Objects.requireNonNull(id);
		this.email = Objects.requireNonNull(email);
		this.passwordHash = Objects.requireNonNull(passwordHash);

		this.firstName = validateName(firstName);
		this.lastName = validateName(lastName);

		this.status = UserStatus.PENDING_VERIFICATION;
		this.createdAt = Objects.requireNonNull(now);
		this.updatedAt = now;
	}

	public static User register(final UUID id, final EmailAddress email, final PasswordHash passwordHash,
			final String firstName, final String lastName, final Instant now) {
		return new User(id, email, passwordHash, firstName, lastName, now);
	}

	private static String validateName(final String name) {
		if (name == null || name.isBlank()) {
			throw new IllegalArgumentException("Name should not be null or blank");
		}

		final String normalizedName = name.trim();
		if (normalizedName.length() > 100) {
			throw new IllegalArgumentException("Invalid name.");
		}

		return normalizedName;
	}

	@Override
	public boolean equals(Object object) {

		if (this == object) {
			return true;
		}

		if (!(object instanceof User other)) {
			return false;
		}

		return id.equals(other.id);
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	public UUID getId() {
		return id;
	}

	public EmailAddress getEmail() {
		return email;
	}

	public PasswordHash getPasswordHash() {
		return passwordHash;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public UserStatus getStatus() {
		return status;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	public void activate(final Instant now) {
		validateTransitionTime(now, "Activation time is required.");
		if (this.status != UserStatus.PENDING_VERIFICATION) {
			throw new InvalidUserStateException("Only pending users can be activated");
		}

		this.status = UserStatus.ACTIVE;
		this.updatedAt = now;
	}

	public void block(final Instant now) {
		validateTransitionTime(now, "Lock time is required.");
		if (this.status != UserStatus.ACTIVE) {
			throw new InvalidUserStateException("Only active users can be locked.");
		}

		this.status = UserStatus.BLOCKED;
		this.updatedAt = now;
	}

	public void unblock(final Instant now) {
		validateTransitionTime(now, "Unlock time is required.");
		if (this.status != UserStatus.BLOCKED) {
			throw new InvalidUserStateException("Only blocked users can be unlocked.");
		}
		this.status = UserStatus.ACTIVE;
		this.updatedAt = now;
	}

	private void validateTransitionTime(final Instant now, final String message) {

		Objects.requireNonNull(now, message);

		if (now.isBefore(updatedAt)) {
			throw new IllegalArgumentException("Transition time cannot be before last update time.");
		}
	}
}
