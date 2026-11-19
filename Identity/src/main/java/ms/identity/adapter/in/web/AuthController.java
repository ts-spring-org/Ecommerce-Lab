package ms.identity.adapter.in.web;

import java.time.Duration;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ms.identity.adapter.in.web.dtos.AccessTokenResponse;
import ms.identity.adapter.in.web.dtos.LoginRequest;
import ms.identity.application.command.LoginCommand;
import ms.identity.application.port.in.LoginUseCase;
import ms.identity.application.port.in.LogoutUseCase;
import ms.identity.application.port.in.RefreshTokenUseCase;\nimport ms.identity.application.port.in.VerifyEmailUseCase;\nimport ms.identity.application.port.in.ResendVerificationEmailUseCase;\nimport ms.identity.adapter.in.web.dtos.VerifyEmailRequest;\nimport ms.identity.adapter.in.web.dtos.ResendVerificationRequest;
import ms.identity.application.results.TokenPair;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final LoginUseCase login;
    private final RefreshTokenUseCase refresh;
    private final LogoutUseCase logout;
    private final Duration refreshTtl;\n    private final VerifyEmailUseCase verifyEmail;\n    private final ResendVerificationEmailUseCase resendVerification;
    private final boolean cookieSecure;
    private final String sameSite;

    public AuthController(LoginUseCase login, RefreshTokenUseCase refresh, LogoutUseCase logout,\n            VerifyEmailUseCase verifyEmail, ResendVerificationEmailUseCase resendVerification,
            @Value("${identity.security.refresh-token.ttl}") Duration refreshTtl,
            @Value("${identity.security.web.cookie-secure:true}") boolean cookieSecure,
            @Value("${identity.security.web.cookie-same-site:Lax}") String sameSite) {
        this.login=login; this.refresh=refresh; this.logout=logout;\n        this.verifyEmail=verifyEmail; this.resendVerification=resendVerification;
        this.refreshTtl=refreshTtl; this.cookieSecure=cookieSecure; this.sameSite=sameSite;
    }

    @PostMapping("/login")
    public ResponseEntity<AccessTokenResponse> webLogin(@Valid @RequestBody LoginRequest request) {
        TokenPair pair=login.login(new LoginCommand(request.email(),request.password(),"WEB"));
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, refreshCookie(pair.refreshToken(),refreshTtl).toString())
                .body(new AccessTokenResponse(pair.accessToken(),pair.tokenType(),pair.expiresInSeconds()));
    }

    @PostMapping("/mobile/login")
    public ResponseEntity<TokenPair> mobileLogin(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(login.login(new LoginCommand(request.email(),request.password(),"MOBILE")));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AccessTokenResponse> webRefresh(@CookieValue(name="refresh_token",required=false) String token) {
        TokenPair pair=refresh.refresh(token,"WEB");
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE,refreshCookie(pair.refreshToken(),refreshTtl).toString())
                .body(new AccessTokenResponse(pair.accessToken(),pair.tokenType(),pair.expiresInSeconds()));
    }

    @PostMapping("/mobile/refresh")
    public ResponseEntity<TokenPair> mobileRefresh(@RequestHeader(name="X-Refresh-Token",required=false) String token) {
        return ResponseEntity.ok(refresh.refresh(token,"MOBILE"));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> webLogout(@CookieValue(name="refresh_token",required=false) String token) {
        logout.logout(token,"WEB");
        return ResponseEntity.noContent().header(HttpHeaders.SET_COOKIE,refreshCookie("",Duration.ZERO).toString()).build();
    }

    @PostMapping("/mobile/logout")
    public ResponseEntity<Void> mobileLogout(@RequestHeader(name="X-Refresh-Token",required=false) String token) {
        logout.logout(token,"MOBILE");
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/verify-email")\n    public ResponseEntity<Void> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {\n        verifyEmail.verify(request.token());\n        return ResponseEntity.noContent().build();\n    }\n\n    @PostMapping("/resend-verification")\n    public ResponseEntity<Void> resendVerification(@Valid @RequestBody ResendVerificationRequest request) {\n        resendVerification.resend(request.email());\n        return ResponseEntity.accepted().build();\n    }\n\n    private ResponseCookie refreshCookie(String token, Duration maxAge) {
        return ResponseCookie.from("refresh_token",token)
                .httpOnly(true).secure(cookieSecure).sameSite(sameSite)
                .path("/api/v1/auth").maxAge(maxAge).build();
    }
}
