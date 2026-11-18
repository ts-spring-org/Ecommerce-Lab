package ms.identity.adapter.out.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import ms.identity.application.port.out.PasswordHasherPort;

@Component
public class BCryptPasswordHasher implements PasswordHasherPort {
	private final BCryptPasswordEncoder encoder;

	public BCryptPasswordHasher() {
		this.encoder = new BCryptPasswordEncoder(12);
	}

	@Override
	public String hash(String rawPassword) {
		return encoder.encode(rawPassword);
	}

}
