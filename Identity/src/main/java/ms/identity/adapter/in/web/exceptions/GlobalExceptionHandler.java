package ms.identity.adapter.in.web.exceptions;

import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.MethodArgumentNotValidException;

import ms.identity.adapter.in.web.dtos.ApiErrorResponse;
import ms.identity.application.exception.EmailAlreadyRegisteredException;
import ms.identity.domain.exception.InvalidUserStateException;

@ControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(EmailAlreadyRegisteredException.class)
	public ResponseEntity<ApiErrorResponse> handleEmailAlreadyRegistred(EmailAlreadyRegisteredException ex) {
		return buildResponse(HttpStatus.CONFLICT, ex.getMessage(), Map.of());
	}

	@ExceptionHandler(InvalidUserStateException.class)
	public ResponseEntity<ApiErrorResponse> handleInvalidUserStateException(InvalidUserStateException ex) {
		return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), Map.of());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
		final Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream().collect(Collectors
				.toMap(error -> error.getField(), error -> error.getDefaultMessage(), (first, second) -> first));

		return buildResponse(HttpStatus.BAD_REQUEST, "Request validation failed.", errors);
	}

	private ResponseEntity<ApiErrorResponse> buildResponse(final HttpStatus status, final String message,
			final Map<String, String> details) {
		final ApiErrorResponse errorResponse = new ApiErrorResponse(Instant.now(), status.value(),
				status.getReasonPhrase(), message, details);
		return ResponseEntity.status(status).body(errorResponse);
	}
}
