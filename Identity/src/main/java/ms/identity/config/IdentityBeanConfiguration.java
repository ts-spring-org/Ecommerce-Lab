package ms.identity.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import ms.identity.application.port.in.RegisterUserUseCase;
import ms.identity.application.port.out.PasswordHasherPort;
import ms.identity.application.port.out.UserRepositoryPort;
import ms.identity.application.port.out.VerificationEmailRequestPort;
import ms.identity.application.service.RegisterUserService;

@Configuration
@EnableScheduling
public class IdentityBeanConfiguration {
    @Bean
    public Clock applicationClock() { return Clock.systemUTC(); }

    @Bean
    public RegisterUserUseCase registerUserUseCase(PasswordHasherPort passwordHasherPort,
            UserRepositoryPort userRepositoryPort, Clock applicationClock,
            VerificationEmailRequestPort verificationEmailRequestPort) {
        return new RegisterUserService(passwordHasherPort, userRepositoryPort, applicationClock, verificationEmailRequestPort);
    }
}
