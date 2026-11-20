package ms.identity.application.exception;

public class AuthenticationNotAllowedException extends RuntimeException {
	
	private static final long serialVersionUID = 2662891401085819938L;

	public AuthenticationNotAllowedException() {
		super("Account is not active");
	}
}
