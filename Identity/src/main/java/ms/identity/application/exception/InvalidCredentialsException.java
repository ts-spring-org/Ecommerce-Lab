package ms.identity.application.exception;

public class InvalidCredentialsException extends RuntimeException {

	private static final long serialVersionUID = 2199989835742753242L;

	public InvalidCredentialsException() {
		super("Invalid email or password");
	}
}
