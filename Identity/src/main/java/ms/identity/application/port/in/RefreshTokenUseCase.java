package ms.identity.application.port.in;
import ms.identity.application.results.TokenPair;
public interface RefreshTokenUseCase { TokenPair refresh(String rawRefreshToken, String expectedClientType); }
