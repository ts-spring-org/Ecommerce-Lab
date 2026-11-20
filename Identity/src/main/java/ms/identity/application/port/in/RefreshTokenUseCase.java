package ms.identity.application.port.in;

import ms.identity.application.results.TokenPair;

public interface RefreshTokenUseCase {
	TokenPair refresh(final String rawRefreshToken, final String expectedClientType);
}
