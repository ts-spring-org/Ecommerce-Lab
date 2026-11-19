package ms.identity.adapter.in.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import ms.identity.adapter.in.web.dtos.RegisterUserRequest;
import ms.identity.adapter.in.web.dtos.RegisterUserResponse;
import ms.identity.application.command.RegisterUserCommand;
import ms.identity.application.port.in.RegisterUserUseCase;
import ms.identity.application.results.RegisterUserResult;

@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = "http://localhost:5173")
public class UserRegistrationController {
	private final RegisterUserUseCase registerUserUseCase;

	public UserRegistrationController(final RegisterUserUseCase registerUserUseCase) {
		this.registerUserUseCase = registerUserUseCase;
	}

	@PostMapping("/register")
	public ResponseEntity<RegisterUserResponse> userRegistration(
			@Valid @RequestBody final RegisterUserRequest request) {
		final RegisterUserCommand command = new RegisterUserCommand(request.email(), request.password(),
				request.firstName(), request.lastName());
		final RegisterUserResult result = registerUserUseCase.register(command);

		final RegisterUserResponse response = new RegisterUserResponse(result.id(), result.email(), result.status(),
				result.createdAt());
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}
}
