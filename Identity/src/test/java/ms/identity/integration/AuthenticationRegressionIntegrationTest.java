package ms.identity.integration;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.HttpStatus.*;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;

import ms.identity.application.results.TokenPair;
import ms.identity.adapter.out.security.JdbcAuthenticationRateLimiter;

@AutoConfigureTestRestTemplate
class AuthenticationRegressionIntegrationTest extends AbstractIntegrationTest {
    @Autowired private TestRestTemplate rest;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private JdbcAuthenticationRateLimiter rateLimiter;

    @Test
    void pendingAccountCannotLoginAndVerificationActivatesItExactlyOnce() throws Exception {
        String email=uniqueEmail();
        String password="StrongPassword123";
        ResponseEntity<Map> registration=register(email,password);
        assertEquals(CREATED,registration.getStatusCode());
        UUID userId=UUID.fromString(registration.getBody().get("id").toString());

        ResponseEntity<String> before=mobileLoginError(email,password);
        assertEquals(UNAUTHORIZED,before.getStatusCode());

        String verificationUrl=jdbc.queryForObject(
                "SELECT payload->>'verificationUrl' FROM outbox_events WHERE aggregate_id=? ORDER BY occurred_at DESC LIMIT 1",
                String.class,userId);
        String token=verificationUrl.substring(verificationUrl.lastIndexOf("token=")+6);
        String verificationHash=jdbc.queryForObject("SELECT token_hash FROM email_verification_tokens WHERE user_id=?",String.class,userId);
        assertNotEquals(token,verificationHash);
        assertEquals(sha256(token),verificationHash.trim());
        ResponseEntity<Void> verified=rest.postForEntity("/api/v1/auth/verify-email",
                Map.of("token",token),Void.class);
        assertEquals(NO_CONTENT,verified.getStatusCode());
        assertEquals("ACTIVE",jdbc.queryForObject("SELECT status FROM users WHERE id=?",String.class,userId));

        ResponseEntity<String> replay=rest.postForEntity("/api/v1/auth/verify-email",
                Map.of("token",token),String.class);
        assertEquals(UNAUTHORIZED,replay.getStatusCode());
    }

    @Test
    void mobileLoginRefreshRotationAndReuseDetectionRevokeEntireFamily() throws Exception {
        String email=uniqueEmail(), password="StrongPassword123";
        UUID userId=registerAndActivate(email,password);

        ResponseEntity<TokenPair> login=rest.postForEntity("/api/v1/auth/mobile/login",
                Map.of("email",email,"password",password),TokenPair.class);
        assertEquals(OK,login.getStatusCode());
        assertNotNull(login.getBody());
        TokenPair first=login.getBody();

        String persistedHash=jdbc.queryForObject("SELECT token_hash FROM refresh_tokens WHERE user_id=?",
                String.class,userId);
        assertNotEquals(first.refreshToken(),persistedHash);
        assertEquals(sha256(first.refreshToken()),persistedHash.trim());

        HttpHeaders headers=new HttpHeaders();
        headers.set("X-Refresh-Token",first.refreshToken());
        ResponseEntity<TokenPair> refreshed=rest.exchange("/api/v1/auth/mobile/refresh",HttpMethod.POST,
                new HttpEntity<>(headers),TokenPair.class);
        assertEquals(OK,refreshed.getStatusCode());
        TokenPair second=refreshed.getBody();
        assertNotNull(second);
        assertNotEquals(first.refreshToken(),second.refreshToken());
        assertNotEquals(first.accessToken(),second.accessToken());

        HttpHeaders replayHeaders=new HttpHeaders();
        replayHeaders.set("X-Refresh-Token",first.refreshToken());
        ResponseEntity<String> replay=rest.exchange("/api/v1/auth/mobile/refresh",HttpMethod.POST,
                new HttpEntity<>(replayHeaders),String.class);
        assertEquals(UNAUTHORIZED,replay.getStatusCode());

        HttpHeaders secondHeaders=new HttpHeaders();
        secondHeaders.set("X-Refresh-Token",second.refreshToken());
        ResponseEntity<String> familyRevoked=rest.exchange("/api/v1/auth/mobile/refresh",HttpMethod.POST,
                new HttpEntity<>(secondHeaders),String.class);
        assertEquals(UNAUTHORIZED,familyRevoked.getStatusCode());

        HttpHeaders bearer=new HttpHeaders();
        bearer.setBearerAuth(first.accessToken());
        ResponseEntity<Map> me=rest.exchange("/api/v1/auth/me",HttpMethod.GET,new HttpEntity<>(bearer),Map.class);
        assertEquals(OK,me.getStatusCode());
        assertEquals(userId.toString(),me.getBody().get("userId"));
    }

    @Test
    void mobileLogoutRevokesRefreshTokenFamily() {
        String email=uniqueEmail(),password="StrongPassword123";
        registerAndActivate(email,password);
        TokenPair pair=rest.postForObject("/api/v1/auth/mobile/login",
                Map.of("email",email,"password",password),TokenPair.class);
        HttpHeaders logoutHeaders=new HttpHeaders();
        logoutHeaders.set("X-Refresh-Token",pair.refreshToken());
        ResponseEntity<Void> logout=rest.exchange("/api/v1/auth/mobile/logout",HttpMethod.POST,
                new HttpEntity<>(logoutHeaders),Void.class);
        assertEquals(NO_CONTENT,logout.getStatusCode());

        HttpHeaders refreshHeaders=new HttpHeaders();
        refreshHeaders.set("X-Refresh-Token",pair.refreshToken());
        ResponseEntity<String> refresh=rest.exchange("/api/v1/auth/mobile/refresh",HttpMethod.POST,
                new HttpEntity<>(refreshHeaders),String.class);
        assertEquals(UNAUTHORIZED,refresh.getStatusCode());
    }

    @Test
    void invalidPasswordDoesNotCreateRefreshSession() {
        String email=uniqueEmail(),password="StrongPassword123";
        UUID userId=registerAndActivate(email,password);
        ResponseEntity<String> response=mobileLoginError(email,"WrongPassword123");
        assertEquals(UNAUTHORIZED,response.getStatusCode());
        assertEquals(0,jdbc.queryForObject("SELECT COUNT(*) FROM refresh_tokens WHERE user_id=?",Integer.class,userId));
    }


    @Test
    void webRefreshCookieIsHttpOnlyAndRotatedWithCsrfProtection() {
        String email=uniqueEmail(),password="StrongPassword123";
        registerAndActivate(email,password);

        ResponseEntity<Map> csrf=rest.getForEntity("/api/v1/auth/csrf",Map.class);
        assertEquals(OK,csrf.getStatusCode());
        String csrfToken=csrf.getBody().get("token").toString();
        String csrfCookie=cookiePair(csrf.getHeaders().getFirst(HttpHeaders.SET_COOKIE),"XSRF-TOKEN");

        HttpHeaders loginHeaders=new HttpHeaders();
        loginHeaders.setContentType(MediaType.APPLICATION_JSON);
        loginHeaders.add(HttpHeaders.COOKIE,csrfCookie);
        loginHeaders.set("X-XSRF-TOKEN",csrfToken);
        ResponseEntity<Map> login=rest.exchange("/api/v1/auth/login",HttpMethod.POST,
                new HttpEntity<>(Map.of("email",email,"password",password),loginHeaders),Map.class);
        assertEquals(OK,login.getStatusCode());
        assertFalse(login.getBody().containsKey("refreshToken"));
        String firstRefreshCookie=login.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        assertTrue(firstRefreshCookie.contains("HttpOnly"));
        assertTrue(firstRefreshCookie.contains("SameSite=Lax"));
        String firstRefresh=cookiePair(firstRefreshCookie,"refresh_token");

        HttpHeaders refreshHeaders=new HttpHeaders();
        refreshHeaders.add(HttpHeaders.COOKIE,csrfCookie+"; "+firstRefresh);
        refreshHeaders.set("X-XSRF-TOKEN",csrfToken);
        ResponseEntity<Map> refreshed=rest.exchange("/api/v1/auth/refresh",HttpMethod.POST,
                new HttpEntity<>(refreshHeaders),Map.class);
        assertEquals(OK,refreshed.getStatusCode());
        String secondRefreshCookie=refreshed.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        String secondRefresh=cookiePair(secondRefreshCookie,"refresh_token");
        assertNotEquals(firstRefresh,secondRefresh);

        HttpHeaders logoutHeaders=new HttpHeaders();
        logoutHeaders.add(HttpHeaders.COOKIE,csrfCookie+"; "+secondRefresh);
        logoutHeaders.set("X-XSRF-TOKEN",csrfToken);
        ResponseEntity<Void> logout=rest.exchange("/api/v1/auth/logout",HttpMethod.POST,
                new HttpEntity<>(logoutHeaders),Void.class);
        assertEquals(NO_CONTENT,logout.getStatusCode());
        assertTrue(logout.getHeaders().getFirst(HttpHeaders.SET_COOKIE).contains("Max-Age=0"));
    }

    private String cookiePair(String setCookie,String name) {
        assertNotNull(setCookie);
        return java.util.Arrays.stream(setCookie.split(";"))
                .map(String::trim).filter(value->value.startsWith(name+"="))
                .findFirst().orElseThrow();
    }



    @Test
    void protectedEndpointRejectsMissingAndMalformedAccessTokens() {
        ResponseEntity<String> missing=rest.getForEntity("/api/v1/auth/me",String.class);
        assertEquals(UNAUTHORIZED,missing.getStatusCode());

        HttpHeaders headers=new HttpHeaders();
        headers.setBearerAuth("not-a-valid-jwt");
        ResponseEntity<String> malformed=rest.exchange("/api/v1/auth/me",HttpMethod.GET,
                new HttpEntity<>(headers),String.class);
        assertEquals(UNAUTHORIZED,malformed.getStatusCode());
    }

    @Test
    void persistentRateLimiterBlocksAfterConfiguredAttemptsAndResetsWindow() {
        String key="regression:"+UUID.randomUUID();
        Instant start=Instant.parse("2026-11-20T10:00:00Z");
        assertTrue(rateLimiter.allow(key,start,2,Duration.ofMinutes(15)));
        assertTrue(rateLimiter.allow(key,start.plusSeconds(1),2,Duration.ofMinutes(15)));
        assertFalse(rateLimiter.allow(key,start.plusSeconds(2),2,Duration.ofMinutes(15)));
        assertFalse(rateLimiter.allow(key,start.plusSeconds(3),2,Duration.ofMinutes(15)));
        assertTrue(rateLimiter.allow(key,start.plusSeconds(901),2,Duration.ofMinutes(15)));
    }

    private UUID registerAndActivate(String email,String password) {
        ResponseEntity<Map> response=register(email,password);
        assertEquals(CREATED,response.getStatusCode());
        UUID id=UUID.fromString(response.getBody().get("id").toString());
        jdbc.update("UPDATE users SET status='ACTIVE' WHERE id=?",id);
        return id;
    }

    private ResponseEntity<Map> register(String email,String password) {
        return rest.postForEntity("/api/v1/auth/register",
                Map.of("email",email,"password",password,"firstName","Regression","lastName","Test"),Map.class);
    }

    private ResponseEntity<String> mobileLoginError(String email,String password) {
        return rest.postForEntity("/api/v1/auth/mobile/login",Map.of("email",email,"password",password),String.class);
    }

    private String uniqueEmail() { return "auth-"+UUID.randomUUID()+"@example.com"; }

    private String sha256(String value) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                .digest(value.getBytes(StandardCharsets.UTF_8)));
    }
}
