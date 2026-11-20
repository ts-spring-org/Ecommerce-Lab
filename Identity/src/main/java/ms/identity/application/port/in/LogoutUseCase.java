package ms.identity.application.port.in;

public interface LogoutUseCase {
	void logout(final String rawRefreshToken, final String expectedClientType);
}
