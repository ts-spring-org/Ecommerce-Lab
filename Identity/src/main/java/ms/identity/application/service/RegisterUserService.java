package ms.identity.application.service;

import java.time.Instant;
import java.util.UUID;

import ms.identity.application.command.RegisterUserCommand;
import ms.identity.application.exception.EmailAlreadyRegisteredException;
import ms.identity.application.port.in.RegisterUserUseCase;
import ms.identity.application.port.out.PasswordHasherPort;
import ms.identity.application.port.out.UserRepositoryPort;
import ms.identity.application.results.RegisterUserResult;
import ms.identity.domain.model.EmailAddress;
import ms.identity.domain.model.PasswordHash;
import ms.identity.domain.model.User;

public class RegisterUserService implements RegisterUserUseCase {
	private final PasswordHasherPort passwordHashedPort;
	private final UserRepositoryPort userRepositoryPort;

	public RegisterUserService(final PasswordHasherPort passwordHashedPort,
			final UserRepositoryPort userRepositoryPort) {
		this.passwordHashedPort = passwordHashedPort;
		this.userRepositoryPort = userRepositoryPort;
	}

	@Override
	public RegisterUserResult register(final RegisterUserCommand command) {

		final EmailAddress email = EmailAddress.of(command.email());
		final boolean emailExists = userRepositoryPort.existsByEmail(email);

		if (emailExists) {
			throw new EmailAlreadyRegisteredException();
		}

		final String encodedPassword = passwordHashedPort.hash(command.rawPassword());

		final PasswordHash pwdHash = new PasswordHash(encodedPassword);

		final Instant now = Instant.now();
		User user = User.register(UUID.randomUUID(), email, pwdHash, command.firstName(), command.lastName(), now);

		userRepositoryPort.save(user);

		return new RegisterUserResult(user.getId(), user.getEmail().toString(), user.getStatus(), now);
	}

}
