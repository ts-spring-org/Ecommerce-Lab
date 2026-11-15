package ms.identity.application.port.out;

import ms.identity.domain.model.EmailAddress;
import ms.identity.domain.model.User;

public interface UserRepositoryPort {

	boolean existsByEmail(final EmailAddress email);

	void save(final User user);

}
