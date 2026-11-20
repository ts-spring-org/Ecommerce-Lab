package ms.identity.application.port.in;

public interface ResendVerificationEmailUseCase {
	void resend(final String email);
}
