package ms.identity.adapter.egress.persistence;

import org.springframework.stereotype.Component;

import ms.identity.domain.model.EmailAddress;
import ms.identity.domain.model.PasswordHash;
import ms.identity.domain.model.User;

@Component
public class UserPersistenceMapper {
	public UserJpaEntity mapToUserJpaEntity(final User user) {
		return new UserJpaEntity(user.getId(), user.getEmail().toString(), user.getPasswordHash().encodedValue(),
				user.getFirstName(), user.getLastName(), user.getStatus(), user.getCreatedAt(), user.getUpdatedAt());
	}

	public User mapToUserDomain(final UserJpaEntity entity) {
		return User.reconstitute(entity.getId(), EmailAddress.of(entity.getEmail()),
				new PasswordHash(entity.getPasswordHash()), entity.getFirstName(), entity.getLastName(),
				entity.getStatus(), entity.getCreatedAt(), entity.getUpdatedAt());
	}
}
