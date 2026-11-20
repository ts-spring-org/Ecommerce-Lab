# Identity Service regression test plan

## Automated suites

Run only Identity and its reactor dependencies:

```bash
mvn -pl Identity -am clean verify
```

Run the full ecommerce repository regression suite:

```bash
mvn -B clean verify
```

Integration tests use PostgreSQL 17 through Testcontainers and run Liquibase migrations from an empty database. They do not require a locally installed PostgreSQL server. Docker must be available to Maven tests.

## Regression coverage

| Area | Regression assertions |
| --- | --- |
| Registration | Valid registration persists a pending user and hashed password; duplicate email remains HTTP 409; malformed requests and passwords over 72 UTF-8 bytes are rejected |
| Email verification | Registration writes a hashed 24-hour token and outbox event; verification activates the user; replay is rejected |
| Login | Pending accounts cannot authenticate; invalid credentials are generic HTTP 401; failed login creates no refresh session |
| Access JWT | Valid Bearer JWT is accepted; missing and malformed JWTs are rejected; subject and role claims are mapped |
| Mobile refresh | Refresh token is hashed in PostgreSQL; rotation replaces the token; replay revokes the entire family; revoked token cannot be refreshed |
| Logout | Mobile logout revokes the refresh-token family |
| Web transport | Refresh token is HttpOnly cookie, not JSON; browser CSRF header is required; refresh rotates cookie; logout clears it |
| Rate limiting | PostgreSQL bucket blocks over-limit requests and resets after the configured window |
| Database | Liquibase applies additive migrations 002 and 003; migration 001 remains unchanged |

## Test-only settings

`application-test.yml` supplies a test-only RSA key pair, disables outbound email polling, and disables the HTTP rate-limit filter so independent integration tests do not share an IP quota. The rate-limit persistence implementation is tested directly against the Testcontainers database.

Never use the test RSA private key in a deployed environment. Configure production signing keys, SMTP credentials, frontend origin, HTTPS, and CORS allow-list separately.
