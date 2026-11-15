package ms.identity.application.port.out;

public interface PasswordHasherPort {
	String hash(final String rawPassword);

}
