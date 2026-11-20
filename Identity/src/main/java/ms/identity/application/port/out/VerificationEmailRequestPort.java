package ms.identity.application.port.out;

import ms.identity.domain.model.User;

public interface VerificationEmailRequestPort {
	void requestVerification(final User user);
}
