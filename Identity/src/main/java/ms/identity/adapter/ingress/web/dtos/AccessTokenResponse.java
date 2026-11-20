package ms.identity.adapter.ingress.web.dtos;
public record AccessTokenResponse(String accessToken, String tokenType, long expiresInSeconds) {}
