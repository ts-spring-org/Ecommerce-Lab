package ms.identity.application.results;

public record TokenPair(String accessToken, String refreshToken, long expiresInSeconds, String tokenType) {
	public TokenPair(final String accessToken, final String refreshToken, final long expiresInSeconds) {
		this(accessToken, refreshToken, expiresInSeconds, "Bearer");
	}
}
