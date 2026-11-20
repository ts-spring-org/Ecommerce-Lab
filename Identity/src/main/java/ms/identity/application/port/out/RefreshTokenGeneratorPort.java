package ms.identity.application.port.out;

public interface RefreshTokenGeneratorPort {
	String generate();

	String hash(final String rawToken);
}
