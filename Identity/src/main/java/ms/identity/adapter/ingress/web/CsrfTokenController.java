package ms.identity.adapter.ingress.web;

import java.util.Map;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class CsrfTokenController {
    @GetMapping("/csrf")
    public ResponseEntity<Map<String,String>> csrf(CsrfToken csrfToken) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .body(Map.of("headerName",csrfToken.getHeaderName(),
                        "parameterName",csrfToken.getParameterName(),"token",csrfToken.getToken()));
    }
}
