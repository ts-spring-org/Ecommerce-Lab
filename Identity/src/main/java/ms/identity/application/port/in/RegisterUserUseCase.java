package ms.identity.application.port.in;

import ms.identity.application.command.RegisterUserCommand;
import ms.identity.application.results.RegisterUserResult;

public interface RegisterUserUseCase {
	RegisterUserResult register(final RegisterUserCommand user);
}
