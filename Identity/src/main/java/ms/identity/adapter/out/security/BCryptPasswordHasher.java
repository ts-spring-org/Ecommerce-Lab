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
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(BCRYPT_STRENGTH);

    @Override
    public String hash(String rawPassword) {
        Objects.requireNonNull(rawPassword, "Password cannot be null");
        validateLength(rawPassword);
        return encoder.encode(rawPassword);
    }

    @Override
    public boolean matches(String rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null) return false;
        validateLength(rawPassword);
        return encoder.matches(rawPassword, encodedPassword);
    }

    private void validateLength(String rawPassword) {
        if (rawPassword.getBytes(StandardCharsets.UTF_8).length > MAX_PASSWORD_BYTES)
            throw new IllegalArgumentException("Password exceeds maximum allowed length of 72 UTF-8 bytes");
    }
}
