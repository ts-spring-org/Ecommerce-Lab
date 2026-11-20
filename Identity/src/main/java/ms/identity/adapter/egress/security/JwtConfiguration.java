package ms.identity.adapter.egress.security;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.List;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

@Configuration
public class JwtConfiguration {
	@Bean
	RSAPublicKey identityJwtPublicKey(@Value("${identity.security.jwt.public-key}") final String encoded) {
		return RsaKeyLoader.publicKey(encoded);
	}

	@Bean
	RSAPrivateKey identityJwtPrivateKey(@Value("${identity.security.jwt.private-key}") final String encoded) {
		return RsaKeyLoader.privateKey(encoded);
	}

	@Bean
	JwtEncoder jwtEncoder(final RSAPublicKey publicKey, final RSAPrivateKey privateKey) {
		RSAKey signingKey = new RSAKey.Builder(publicKey).privateKey(privateKey).keyID("identity-rsa-1").build();
		var jwkSource = new ImmutableJWKSet<SecurityContext>(new JWKSet(signingKey));
		return new NimbusJwtEncoder(jwkSource);
	}

	@Bean
	JwtDecoder jwtDecoder(final RSAPublicKey publicKey, @Value("${identity.security.jwt.issuer}") final String issuer,
			@Value("${identity.security.jwt.audience}") final String audience) {
		NimbusJwtDecoder decoder = NimbusJwtDecoder.withPublicKey(publicKey).build();
		OAuth2TokenValidator<Jwt> issuerValidator = JwtValidators.createDefaultWithIssuer(issuer);
		OAuth2TokenValidator<Jwt> audienceValidator = jwt -> jwt.getAudience().contains(audience)
				? org.springframework.security.oauth2.core.OAuth2TokenValidatorResult.success()
				: org.springframework.security.oauth2.core.OAuth2TokenValidatorResult
						.failure(new org.springframework.security.oauth2.core.OAuth2Error("invalid_token",
								"Required audience is missing", null));
		decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(List.of(issuerValidator, audienceValidator)));
		return decoder;
	}
}
