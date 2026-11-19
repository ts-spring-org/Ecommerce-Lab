package ms.identity.integration;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.HttpStatus.*;

import java.nio.charset.StandardCharsets;
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

@AutoConfigureTestRestTemplate
class AuthenticationRegressionIntegrationTest extends AbstractIntegrationTest {
    @Autowired private TestRestTemplate rest;
    @Autowired private JdbcTemplate jdbc;

    @Test
    void pendingAccountCannotLoginAndVerificationActivatesItExactlyOnce() {
        String email=uniqueEmail();
        String password="StrongPassword123";
        ResponseEntity<Map> registration=register(email,password);
        assertEquals(CREATED,registration.getStatusCode());
        UUID userId=UUID.fromString(registration.getBody().get("id").toString());

        ResponseEntity<String> before=mobileLoginError(email,password);
        assertEquals(FORBIDDEN,before.getStatusCode());

        String verificationUrl=jdbc.queryForObject(
                "SELECT payload->>'verificationUrl' FROM outbox_events WHERE aggregate_id=? ORDER BY occurred_at DESC LIMIT 1",
                String.class,userId);
        String token=verificationUrl.substring(verificationUrl.lastIndexOf("token=")+6);
        ResponseEntity<Void> verified=rest.postForEntity("/api/v1/auth/verify-email",
                Map.of("token",token),Void.class);
        assertEquals(NO_CONTENT,verified.getStatusCode());
        assertEquals("ACTIVE",jdbc.queryForObject("SELECT status FROM users WHERE id=?",String.class,userId));

        ResponseEntity<String> replay=rest.postForEntity("/api/v1/auth/verify-email",
                Map.of("token",token),String.class);
        assertEquals(UNAUTHORIZED,replay.getStatusCode());
    }

    @Test
    void mobileLoginRefreshRotationAndReuseDetectionRevokeEntireFamily() {
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
