package ms.identity.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import ms.identity.application.command.RegisterUserCommand;
import ms.identity.application.exception.EmailAlreadyRegisteredException;
import ms.identity.application.port.out.PasswordHasherPort;
import ms.identity.application.port.out.UserRepositoryPort;
import ms.identity.application.results.RegisterUserResult;
import ms.identity.domain.model.EmailAddress;
import ms.identity.domain.model.User;
import ms.identity.domain.model.UserStatus;

import org.mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterUserServiceTest {

	@Mock
	private PasswordHasherPort passwordHasherPortMock;
	@Mock
	private UserRepositoryPort userRepositoryPortMock;

	private static final String ENCODED_PASSWORD = "encoded-password-hash";
	private static final Instant NOW = Instant.parse("2026-11-15T10:00:00Z");
	private static final RegisterUserCommand COMMAND = new RegisterUserCommand("user@example.com", "hash@password",
			"user", "lname");

	// @InjectMocks
	private RegisterUserService registerUserService;

	@BeforeEach
	void setup() {
		final Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);
		registerUserService = new RegisterUserService(passwordHasherPortMock, userRepositoryPortMock, clock);
	}

	@Test
	void shouldRegisterUser_whenEmailDoesNotExist() {
		when(userRepositoryPortMock.existsByEmail(any(EmailAddress.class))).thenReturn(false);
		when(passwordHasherPortMock.hash(COMMAND.rawPassword())).thenReturn(ENCODED_PASSWORD);

		RegisterUserResult result = registerUserService.register(COMMAND);

		assertNotNull(result);
		assertNotNull(result.id());
		assertEquals(COMMAND.email(), result.email());
		assertEquals(UserStatus.PENDING_VERIFICATION, result.status());
		assertEquals(NOW, result.createdAt());

		verify(userRepositoryPortMock).existsByEmail(EmailAddress.of(COMMAND.email()));
		verify(passwordHasherPortMock).hash(COMMAND.rawPassword());
		verify(userRepositoryPortMock).save(any(User.class));
	}

	@Test
	void shouldNotSaveUser_whenPasswordHashingFails() {
		when(userRepositoryPortMock.existsByEmail(EmailAddress.of(COMMAND.email()))).thenReturn(false);
		when(passwordHasherPortMock.hash(COMMAND.rawPassword()))
				.thenThrow(new IllegalArgumentException("Hashing failed."));
		assertThrows(IllegalArgumentException.class, () -> registerUserService.register(COMMAND));

		verify(userRepositoryPortMock, never()).save(any(User.class));
	}

	@Test
	void shouldPropagateException_whenSavingUserFails() {
		when(userRepositoryPortMock.existsByEmail(any(EmailAddress.class))).thenReturn(false);
		when(passwordHasherPortMock.hash(COMMAND.rawPassword())).thenReturn(ENCODED_PASSWORD);

		doThrow(new IllegalArgumentException("Persistence failed.")).when(userRepositoryPortMock).save(any(User.class));

		assertThrows(IllegalArgumentException.class, () -> registerUserService.register(COMMAND));

		verify(userRepositoryPortMock).save(any(User.class));
	}

	@Test
	void shouldThrowEmailAlreadyRegisteredException_whenEmailAlreadyExists() {
		when(userRepositoryPortMock.existsByEmail(EmailAddress.of(COMMAND.email()))).thenReturn(true);

		assertThrows(EmailAlreadyRegisteredException.class, () -> registerUserService.register(COMMAND));

		verify(passwordHasherPortMock, never()).hash(COMMAND.rawPassword());
		verify(userRepositoryPortMock).existsByEmail(EmailAddress.of(COMMAND.email()));
		verify(userRepositoryPortMock, never()).save(any(User.class));
	}

}