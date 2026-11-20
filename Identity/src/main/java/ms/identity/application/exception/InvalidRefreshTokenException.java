package ms.identity.application.exception;

public class InvalidRefreshTokenException extends RuntimeException {
	
	private static final long serialVersionUID = -8019653418950547283L;

	public InvalidRefreshTokenException() {
		super("Refresh token is invalid or expired");
	}
}
