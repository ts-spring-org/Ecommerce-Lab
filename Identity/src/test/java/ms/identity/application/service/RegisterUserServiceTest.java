package ms.identity.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import ms.identity.application.command.RegisterUserCommand;
import ms.identity.application.exception.EmailAlreadyRegisteredException;
import ms.identity.application.port.out.PasswordHasherPort;
import ms.identity.application.port.out.UserRepositoryPort;
import ms.identity.application.results.RegisterUserResult;
import ms.identity.domain.model.EmailAddress;
import ms.identity.domain.model.UserStatus;

import org.mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterUserServiceTest {

	@Mock
	private PasswordHasherPort passwordHasherPortMock;
	@Mock
	private UserRepositoryPort userRepositoryPortMock;

	private static final RegisterUserCommand COMMAND = new RegisterUserCommand("user@example.com", "hash@password",
			"user", "lname");

	@InjectMocks
	private RegisterUserService registerUserService;

	@Test
	void shouldRegisterNewUser_expect_success() {
		when(passwordHasherPortMock.hash(anyString())).thenReturn(UUID.randomUUID().toString());
		when(userRepositoryPortMock.existsByEmail(any(EmailAddress.class))).thenReturn(false);

		RegisterUserResult result = registerUserService.register(COMMAND);

		assertNotNull(result);
		assertThat(result.email()).isEqualTo(COMMAND.email());
		assertThat(result.status()).isEqualTo(UserStatus.PENDING_VERIFICATION);

		verify(userRepositoryPortMock).existsByEmail(any(EmailAddress.class));
		verify(passwordHasherPortMock).hash(COMMAND.rawPassword());
	}

	@Test
	void shouldFailsRegistringUserEmailExists_expect_fails() {
		when(userRepositoryPortMock.existsByEmail(any(EmailAddress.class))).thenReturn(true);

		assertThrows(EmailAlreadyRegisteredException.class, () -> registerUserService.register(COMMAND));

		verify(passwordHasherPortMock, never()).hash(COMMAND.rawPassword());
		verify(userRepositoryPortMock).existsByEmail(any(EmailAddress.class));
	}

}