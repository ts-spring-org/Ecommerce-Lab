package ms.identity.adapter.in.web;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ms.identity.application.exception.AuthenticationNotAllowedException;
import ms.identity.application.exception.EmailAlreadyRegisteredException;
import ms.identity.application.exception.InvalidCredentialsException;
import ms.identity.application.exception.InvalidRefreshTokenException;
import ms.identity.application.exception.InvalidVerificationTokenException;

@RestControllerAdvice
public class AuthenticationExceptionHandler {
    @ExceptionHandler({InvalidCredentialsException.class,InvalidRefreshTokenException.class,InvalidVerificationTokenException.class})
    public ResponseEntity<Map<String,String>> unauthorized(RuntimeException exception) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error","unauthorized","message","Authentication failed"));
    }
    @ExceptionHandler(AuthenticationNotAllowedException.class)
    public ResponseEntity<Map<String,String>> forbidden(AuthenticationNotAllowedException exception) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error","forbidden","message",exception.getMessage()));
    }
    @ExceptionHandler(EmailAlreadyRegisteredException.class)
    public ResponseEntity<Map<String,String>> conflict(EmailAlreadyRegisteredException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error","conflict","message",exception.getMessage()));
    }
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String,String>> badRequest(IllegalArgumentException exception) {
        return ResponseEntity.badRequest().body(Map.of("error","bad_request","message",exception.getMessage()));
    }
}
