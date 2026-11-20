package ms.identity.adapter.egress.security;

import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

final class RsaKeyLoader {
	private RsaKeyLoader() {
	}

	static RSAPrivateKey privateKey(final String encoded) {
		try {
			byte[] der = Base64.getDecoder().decode(encoded);
			return (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(der));
		} catch (Exception exception) {
			throw new IllegalStateException(
					"IDENTITY_JWT_PRIVATE_KEY must contain a Base64-encoded PKCS#8 RSA private key", exception);
		}
	}

	static RSAPublicKey publicKey(final String encoded) {
		try {
			byte[] der = Base64.getDecoder().decode(encoded);
			return (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(der));
		} catch (Exception exception) {
			throw new IllegalStateException(
					"IDENTITY_JWT_PUBLIC_KEY must contain a Base64-encoded X.509 RSA public key", exception);
		}
	}
}
