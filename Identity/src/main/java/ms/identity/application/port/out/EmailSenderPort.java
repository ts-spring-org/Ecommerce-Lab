package ms.identity.application.port.out;

public interface EmailSenderPort {
	void sendVerificationEmail(final String recipient, final String firstName, final String verificationUrl);
}
