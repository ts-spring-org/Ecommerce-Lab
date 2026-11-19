package ms.identity.adapter.in.web.dtos;
public record AccessTokenResponse(String accessToken, String tokenType, long expiresInSeconds) {}
