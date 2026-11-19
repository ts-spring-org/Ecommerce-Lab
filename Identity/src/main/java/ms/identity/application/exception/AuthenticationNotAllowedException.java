package ms.identity.application.exception;
public class AuthenticationNotAllowedException extends RuntimeException {
    public AuthenticationNotAllowedException() { super("Account is not active"); }
}
