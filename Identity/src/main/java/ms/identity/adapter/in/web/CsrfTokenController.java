package ms.identity.adapter.in.web;

import java.util.Map;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class CsrfTokenController {
    @GetMapping("/csrf")
    public Map<String,String> csrf(CsrfToken csrfToken) {
        return Map.of("headerName",csrfToken.getHeaderName(),
                "parameterName",csrfToken.getParameterName(),"token",csrfToken.getToken());
    }
}
