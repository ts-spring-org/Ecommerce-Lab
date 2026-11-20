package ms.identity.adapter.egress.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataUserRepository extends JpaRepository<UserJpaEntity, UUID> {
	boolean existsByEmail(final String email);

	Optional<UserJpaEntity> findByEmail(final String email);
}
