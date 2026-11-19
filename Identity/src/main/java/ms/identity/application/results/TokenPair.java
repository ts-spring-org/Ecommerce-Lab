package ms.identity.application.results;
public record TokenPair(String accessToken, String refreshToken, long expiresInSeconds, String tokenType) {
    public TokenPair(String accessToken, String refreshToken, long expiresInSeconds) {
        this(accessToken, refreshToken, expiresInSeconds, "Bearer");
    }
}
