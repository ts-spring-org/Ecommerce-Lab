package ms.identity.adapter.ingress.web;

import java.time.Duration;
import java.time.Instant;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.web.filter.OncePerRequestFilter;
import ms.identity.adapter.egress.security.JdbcAuthenticationRateLimiter;

public class AuthenticationRateLimitFilter extends OncePerRequestFilter {
    private final JdbcAuthenticationRateLimiter limiter;
    public AuthenticationRateLimitFilter(JdbcAuthenticationRateLimiter limiter) { this.limiter=limiter; }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (!"POST".equalsIgnoreCase(request.getMethod())) return true;
        String path=request.getRequestURI();
        return !(path.equals("/api/v1/auth/login") || path.equals("/api/v1/auth/mobile/login")
                || path.equals("/api/v1/auth/register") || path.equals("/api/v1/auth/refresh")
                || path.equals("/api/v1/auth/mobile/refresh") || path.equals("/api/v1/auth/verify-email")
                || path.equals("/api/v1/auth/resend-verification"));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,HttpServletResponse response,FilterChain chain)
            throws ServletException,IOException {
        String path=request.getRequestURI();
        int maximum=path.contains("register")?5:path.contains("resend-verification")?3:
                path.contains("verify-email")?10:path.contains("refresh")?30:10;
        Duration window=path.contains("register")?Duration.ofHours(1):Duration.ofMinutes(15);
        String key=request.getRemoteAddr()+":"+path;
        Instant now=Instant.now();
        if(!limiter.allow(key,now,maximum,window)) {
            response.setStatus(429);
            response.setHeader("Retry-After","300");
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"too_many_requests\",\"message\":\"Too many authentication attempts\"}");
            return;
        }
        chain.doFilter(request,response);
    }
}
