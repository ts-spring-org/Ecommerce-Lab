package ms.identity.adapter.egress.persistence;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import ms.identity.domain.model.UserStatus;

@Entity
@Table(name = "users", uniqueConstraints = { @UniqueConstraint(name = "uk_users_email", columnNames = "email") })
public class UserJpaEntity {
	@Id
	private UUID id;
	@NotBlank
	@Email
	@Size(max = 254)
	@Column(nullable = false, length = 254)
	private String email;
	@NotBlank
	@Size(min = 8, max = 255)
	@Column(name = "password_hash", nullable = false, length = 255)
	private String passwordHash;
	@NotBlank
	@Size(min = 1, max = 100)
	@Column(name = "first_name", nullable = false, length = 100)
	private String firstName;
	@NotBlank
	@Size(min = 1, max = 100)
	@Column(name = "last_name", nullable = false, length = 100)
	private String lastName;
	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 30)
	private UserStatus status;
	@NotNull
	@Column(name = "created_at", nullable = false)
	private Instant createdAt;
	@NotNull
	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	public UserJpaEntity() {
	}

	public UserJpaEntity(final UUID id, final String email, final String passwordHash, final String firstName, final String lastName,
			final UserStatus status, final Instant createdAt, final Instant updatedAt) {
		super();
		this.id = id;
		this.email = email;
		this.passwordHash = passwordHash;
		this.firstName = firstName;
		this.lastName = lastName;
		this.status = status;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	public UUID getId() {
		return id;
	}

	public String getEmail() {
		return email;
	}

	public String getPasswordHash() {
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

}
