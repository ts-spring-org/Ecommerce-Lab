package ms.identity.application.port.in;
public interface LogoutUseCase { void logout(String rawRefreshToken, String expectedClientType); }
