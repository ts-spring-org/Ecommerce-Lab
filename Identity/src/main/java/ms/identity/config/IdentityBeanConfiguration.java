package ms.identity.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import ms.identity.application.port.in.RegisterUserUseCase;
import ms.identity.application.port.out.PasswordHasherPort;
import ms.identity.application.port.out.UserRepositoryPort;
import ms.identity.application.service.RegisterUserService;

@Configuration
public class IdentityBeanConfiguration {

	@Bean
	public Clock applicationClock() {
		return Clock.systemUTC();
	}

	@Bean
	public RegisterUserUseCase registerUserUseCase(final PasswordHasherPort passwordHasherPort,
			final UserRepositoryPort userrRepositoryPort, final Clock applicationClock) {

		return new RegisterUserService(passwordHasherPort, userrRepositoryPort, applicationClock);
	}
}
