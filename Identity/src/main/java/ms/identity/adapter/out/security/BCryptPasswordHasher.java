package ms.identity.adapter.out.security;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import ms.identity.application.port.out.PasswordHasherPort;

@Component
public class BCryptPasswordHasher implements PasswordHasherPort {
	private static final int BCRYPT_STRENGTH = 12;
	private static final int MAX_PASSWORD_BYTES = 72;

	private final BCryptPasswordEncoder encoder;

	public BCryptPasswordHasher() {
		this.encoder = new BCryptPasswordEncoder(BCRYPT_STRENGTH);
	}

	@Override
	public String hash(String rawPassword) {
		Objects.requireNonNull("Password cannot be null");

		final int passwordBytes = rawPassword.getBytes(StandardCharsets.UTF_8).length; // Ensure password is in UTF-8

		if (passwordBytes > MAX_PASSWORD_BYTES) {
			throw new IllegalArgumentException(
					"Password exceeds maximum allowed length of " + MAX_PASSWORD_BYTES + " bytes");
		}

		return encoder.encode(rawPassword);
	}

}
