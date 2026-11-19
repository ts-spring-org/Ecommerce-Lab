package ms.identity.adapter.in.web;

import java.util.Arrays;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class SecurityConfiguration {
    @Bean
    SecurityFilterChain identitySecurityFilterChain(HttpSecurity http) throws Exception {
        CookieCsrfTokenRepository csrfRepository=CookieCsrfTokenRepository.withHttpOnlyFalse();
        csrfRepository.setHeaderName("X-XSRF-TOKEN");
        JwtGrantedAuthoritiesConverter authorities=new JwtGrantedAuthoritiesConverter();
        authorities.setAuthoritiesClaimName("roles");
        authorities.setAuthorityPrefix("");
        JwtAuthenticationConverter jwtConverter=new JwtAuthenticationConverter();
        jwtConverter.setJwtGrantedAuthoritiesConverter(authorities);

        return http.cors(Customizer.withDefaults())
                .csrf(csrf->csrf.csrfTokenRepository(csrfRepository)
                        .ignoringRequestMatchers("/api/v1/auth/register","/api/v1/auth/verify-email",
                                "/api/v1/auth/resend-verification","/api/v1/auth/mobile/**"))
                .sessionManagement(session->session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize->authorize
                        .requestMatchers("/api/v1/auth/csrf").permitAll()
                        .requestMatchers(HttpMethod.POST,"/api/v1/auth/register","/api/v1/auth/login",
                                "/api/v1/auth/refresh","/api/v1/auth/logout",
                                "/api/v1/auth/mobile/login","/api/v1/auth/mobile/refresh",
                                "/api/v1/auth/mobile/logout","/api/v1/auth/verify-email",
                                "/api/v1/auth/resend-verification").permitAll()
                        .requestMatchers("/actuator/health","/actuator/info").permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(resource->resource.jwt(jwt->jwt.jwtAuthenticationConverter(jwtConverter)))
                .build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource(
            @Value("${identity.security.web.allowed-origins}") String allowedOrigins) {
        CorsConfiguration config=new CorsConfiguration();
        config.setAllowedOrigins(Arrays.stream(allowedOrigins.split(",")).map(String::trim)
                .filter(origin->!origin.isEmpty()).toList());
        config.setAllowedMethods(Arrays.asList("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
        config.setAllowedHeaders(Arrays.asList("Authorization","Content-Type","X-XSRF-TOKEN","X-Refresh-Token"));
        config.setExposedHeaders(Arrays.asList("Authorization"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source=new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**",config);
        return source;
    }
}
