package ms.identity.adapter.in.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import ms.identity.adapter.in.web.exceptions.GlobalExceptionHandler;
import ms.identity.application.command.RegisterUserCommand;
import ms.identity.application.exception.EmailAlreadyRegisteredException;
import ms.identity.application.port.in.RegisterUserUseCase;
import ms.identity.application.results.RegisterUserResult;
import ms.identity.domain.model.UserStatus;

@ExtendWith(MockitoExtension.class)
public class UserRegistrationControllerTest {
	private MockMvc mockMvc;
	private static final UUID USER_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
	private static final Instant CREATED_AT = Instant.parse("2026-11-18T10:00:00Z");

	@Mock
	private RegisterUserUseCase registerUserUseCase;

	@InjectMocks
	private UserRegistrationController userRegistrationController;

	private void setup() {
		mockMvc = MockMvcBuilders.standaloneSetup(userRegistrationController)
				.setControllerAdvice(new GlobalExceptionHandler()).build();
	}

	@Test
	void shouldRegisterUserAndReturnCreated() throws Exception {
		setup();
		final RegisterUserResult result = new RegisterUserResult(USER_ID, "user@example.com",
				UserStatus.PENDING_VERIFICATION, CREATED_AT);

		when(registerUserUseCase.register(any(RegisterUserCommand.class))).thenReturn(result);

		final String request = """
				{
				    "email": "user@example.com",
				    "password": "StrongPassword123",
				    "firstName": "John",
				    "lastName": "Smith"
				}
				""";

		mockMvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON).content(request))
				.andExpect(status().isCreated()).andExpect(jsonPath("$.id").value(USER_ID.toString()))
				.andExpect(jsonPath("$.email").value("user@example.com"))
				.andExpect(jsonPath("$.status").value("PENDING_VERIFICATION"));

		verify(registerUserUseCase).register(any(RegisterUserCommand.class));
	}

	@Test
	void shouldReturnBadRequest_whenPasswordIsTooShort() throws Exception {
		setup();

		final String request = """
				{
				    "email": "user@example.com",
				    "password": "short",
				    "firstName": "John",
				    "lastName": "Smith"
				}
				""";

		mockMvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON).content(request))
				.andExpect(status().isBadRequest());
	}

	@Test
	void shouldReturnConflict_whenEmailAlreadyExists() throws Exception {
		setup();

		when(registerUserUseCase.register(any(RegisterUserCommand.class)))
				.thenThrow(new EmailAlreadyRegisteredException());

		final String request = """
				{
				    "email": "user@example.com",
				    "password": "StrongPassword123",
				    "firstName": "John",
				    "lastName": "Smith"
				}
				""";

		mockMvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON).content(request))
				.andExpect(status().isConflict());
	}
}
