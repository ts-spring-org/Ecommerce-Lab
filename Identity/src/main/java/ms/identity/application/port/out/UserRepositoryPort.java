package ms.identity.application.port.out;

import java.util.Optional;
import java.util.UUID;
import ms.identity.domain.model.EmailAddress;
import ms.identity.domain.model.User;

public interface UserRepositoryPort {
	boolean existsByEmail(final EmailAddress email);

	Optional<User> findByEmail(final EmailAddress email);

	Optional<User> findById(final UUID id);

	void save(final User user);
}
