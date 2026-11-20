# Identity authentication

## Token model

- Access tokens are RSA-signed JWTs (RS256), default lifetime 15 minutes.
- Refresh tokens are 256-bit random opaque secrets. PostgreSQL stores only SHA-256 hashes.
- Refresh tokens rotate on every refresh. A consumed-token replay revokes its complete token family.
- Refresh tokens are bound to a client type (WEB or MOBILE).
- Logout revokes refresh-token families. Already-issued access JWTs remain valid until their short expiry (15 minutes by default); immediate JWT revocation would require a shared denylist or token-version lookup.
- Only ACTIVE users can log in or refresh tokens. Email verification activates PENDING_VERIFICATION accounts.
- HTTPS is mandatory outside local development. HTTP headers do not protect tokens without TLS.

## HTTP API

| Client | Endpoint | Refresh token transport |
| --- | --- | --- |
| Web | POST /api/v1/auth/login | HttpOnly refresh_token cookie |
| Web | POST /api/v1/auth/refresh | Cookie; returns rotated cookie |
| Web | POST /api/v1/auth/logout | Cookie; revokes token family |
| Mobile | POST /api/v1/auth/mobile/login | JSON token pair |
| Mobile | POST /api/v1/auth/mobile/refresh | X-Refresh-Token header; returns rotated token pair |
| Mobile | POST /api/v1/auth/mobile/logout | X-Refresh-Token header |
| Both | POST /api/v1/auth/register | Creates PENDING_VERIFICATION account |
| Both | POST /api/v1/auth/verify-email | JSON body containing token |
| Both | POST /api/v1/auth/resend-verification | Generic accepted response |
| Both | GET /api/v1/auth/me | Requires Authorization: Bearer access-token |
| Web | GET /api/v1/auth/csrf | CSRF bootstrap |

Web refresh cookies are HttpOnly and Secure by default, scoped to /api/v1/auth. The browser must first obtain a CSRF token from GET /api/v1/auth/csrf and send its value in X-XSRF-TOKEN for cookie-authenticated POST requests. Set IDENTITY_COOKIE_SAME_SITE=None only when the frontend is truly cross-site; this requires Secure cookies and explicit trusted CORS origins.

Mobile clients should keep the access token in memory and the refresh token in platform secure storage (Android Keystore / iOS Keychain). Send the refresh token only in X-Refresh-Token over HTTPS.

## RSA keys

Generate a production RSA key pair (3072 bits) with OpenSSL:

```bash
openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:3072 -out identity-private.pem
openssl pkcs8 -topk8 -inform PEM -outform DER -in identity-private.pem -nocrypt | base64 -w0
openssl pkey -in identity-private.pem -pubout -outform DER | base64 -w0
```

Set the first Base64 value as IDENTITY_JWT_PRIVATE_KEY (PKCS#8 DER) and the second as IDENTITY_JWT_PUBLIC_KEY (X.509 SubjectPublicKeyInfo DER). Never commit production private keys. Downstream services need the public key and must validate issuer and audience. Keep issuer and audience consistent across services.

## Email verification and outbox

Registration persists the user, a hashed one-time verification token (24-hour expiry), and an outbox event in the same transaction. The outbox worker sends HTML email asynchronously and retries failures with bounded exponential backoff. The frontend verification link carries the token in the URL fragment; the frontend must POST it to /api/v1/auth/verify-email. The token is not consumed by an email-client GET request. Once delivery succeeds, the outbox payload is cleared so it no longer retains the raw token.

Configure MAIL_HOST, MAIL_PORT, MAIL_USERNAME, MAIL_PASSWORD, MAIL_SMTP_AUTH, MAIL_SMTP_STARTTLS, MAIL_FROM, and IDENTITY_FRONTEND_BASE_URL in the deployment environment.

## Rate limiting

The Identity service applies PostgreSQL-backed per-IP limits: registration 5/hour, login 10/15 minutes, refresh 30/15 minutes, email verification 10/15 minutes, and resend 3/15 minutes. A blocked bucket is retried after a 5-minute cooldown and remains throttled until its window resets. Configure IDENTITY_RATE_LIMIT_ENABLED and ensure the reverse proxy strips untrusted forwarding headers and supplies a trustworthy client address. Expired buckets are cleaned daily.

## Database migrations

Liquibase is the only schema-management mechanism. Migration 001 remains immutable. New authentication tables and rate-limit buckets are introduced through additive migrations 002 and 003. Hibernate remains in validate mode.

## Tests

Run the Identity module tests with:

```bash
mvn -pl Identity -am clean verify
```

Run the full repository regression suite with:

```bash
mvn clean verify
```

The integration tests use Testcontainers PostgreSQL. Test-only RSA keys are stored in the test profile and must never be reused in any deployed environment.
