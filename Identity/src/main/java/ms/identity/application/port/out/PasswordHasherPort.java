package ms.identity.application.port.out;

public interface PasswordHasherPort {
	String hash(final String rawPassword);

	boolean matches(final String rawPassword, final String encodedPassword);
}
