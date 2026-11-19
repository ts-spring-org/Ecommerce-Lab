package ms.identity.application.port.out;

import java.util.Optional;
import java.util.UUID;
import ms.identity.domain.model.EmailAddress;
import ms.identity.domain.model.User;

public interface UserRepositoryPort {
    boolean existsByEmail(EmailAddress email);
    Optional<User> findByEmail(EmailAddress email);
    Optional<User> findById(UUID id);
    void save(User user);
}
