package ms.identity.domain.exception;

public class InvalidUserStateException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public InvalidUserStateException(final String message) {
		super(message);
	}
}
