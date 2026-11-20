package ms.identity.application.exception;

public class InvalidVerificationTokenException extends RuntimeException {

	private static final long serialVersionUID = 3619588741248105054L;

	public InvalidVerificationTokenException() {
		super("Verification token is invalid or expired");
	}
}
