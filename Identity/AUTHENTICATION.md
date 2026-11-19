# Identity authentication configuration

The Identity service signs access JWTs with an RSA private key and exposes a public-key JWT resource server.
Do not commit signing keys or put them in application.yml.

Generate a 3072-bit RSA key pair locally (Linux/macOS with OpenSSL):

```bash
openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:3072 -out identity-private.pem
openssl pkcs8 -topk8 -inform PEM -outform DER -in identity-private.pem -nocrypt | base64 -w0
openssl pkey -in identity-private.pem -pubout -outform DER | base64 -w0
```

Set the first Base64 value as `IDENTITY_JWT_PRIVATE_KEY` (PKCS#8 DER) and the second as
`IDENTITY_JWT_PUBLIC_KEY` (X.509 SubjectPublicKeyInfo DER). Keep the private value only in the
Identity service secret store. Downstream services need only the public key.

The access-token default lifetime is 15 minutes. The issuer and audience must be configured
consistently by every service that validates these tokens.

Web clients should first call `GET /api/v1/auth/csrf`, retain the `XSRF-TOKEN` cookie, and send
its value in the `X-XSRF-TOKEN` header on state-changing cookie-authenticated requests. Configure
the exact production web origins in `IDENTITY_WEB_ALLOWED_ORIGINS`; do not use wildcard origins
with credentials. Mobile refresh endpoints are excluded from CSRF because mobile clients send
the refresh secret explicitly in `X-Refresh-Token`.

All non-local deployments must use HTTPS. Header transport does not replace TLS.
