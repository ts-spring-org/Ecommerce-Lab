package ms.identity.application.port.in;

import ms.identity.application.command.LoginCommand;
import ms.identity.application.results.TokenPair;


public interface LoginUseCase {
	TokenPair login(final LoginCommand command);
}
