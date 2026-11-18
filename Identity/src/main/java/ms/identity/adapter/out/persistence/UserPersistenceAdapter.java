package ms.identity.adapter.out.persistence;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import ms.identity.application.exception.EmailAlreadyRegisteredException;
import ms.identity.application.port.out.UserRepositoryPort;
import ms.identity.domain.model.EmailAddress;
import ms.identity.domain.model.User;

@Repository
public class UserPersistenceAdapter implements UserRepositoryPort {
	private static final String EMAIL_UNIQUE_CONSTRAINT = "uk_users_email";

	private final SpringDataUserRepository repository;
	private final UserPersistenceMapper mapper;

	public UserPersistenceAdapter(final SpringDataUserRepository repository, final UserPersistenceMapper mapper) {
		this.mapper = mapper;
		this.repository = repository;
	}

	@Override
	@Transactional(readOnly = true)
	public boolean existsByEmail(final EmailAddress email) {
		return repository.existsByEmail(email.value());
	}

	@Override
	@Transactional
	public void save(final User user) {
		try {
			repository.saveAndFlush(mapper.mapToUserJpaEntity(user));
		}catch(final DataIntegrityViolationException exception) {
			if(isEmailUniqueConstraintViolation(exception)) {
				throw new EmailAlreadyRegisteredException();
			}
			
			throw exception;
		}

	}

	private boolean isEmailUniqueConstraintViolation(final Throwable exception) {
        Throwable cause = exception;

        while (cause != null) {
            if (cause instanceof ConstraintViolationException) {
            	final ConstraintViolationException violation = (ConstraintViolationException) cause;
                return EMAIL_UNIQUE_CONSTRAINT.equals(violation.getConstraintName());
            }

            cause = cause.getCause();
        }

        return false;
    }
}
