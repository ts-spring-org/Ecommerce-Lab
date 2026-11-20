package ms.identity.adapter.out.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

import org.springframework.stereotype.Component;

import ms.identity.application.port.out.RefreshTokenGeneratorPort;

@Component
public class SecureRefreshTokenGenerator implements RefreshTokenGeneratorPort {
	private final SecureRandom secureRandom = new SecureRandom();

	@Override
	public String generate() {
		byte[] bytes = new byte[32];
		secureRandom.nextBytes(bytes);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
	}

	@Override
	public String hash(final String rawToken) {
		try {
			byte[] digest = MessageDigest.getInstance("SHA-256").digest(rawToken.getBytes(StandardCharsets.UTF_8));
			return HexFormat.of().formatHex(digest);
		} catch (Exception exception) {
			throw new IllegalStateException("SHA-256 is unavailable", exception);
		}
	}
}
