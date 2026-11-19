package ms.identity.application.service;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ms.identity.application.command.RegisterUserCommand;
import ms.identity.application.exception.EmailAlreadyRegisteredException;
import ms.identity.application.port.in.RegisterUserUseCase;
import ms.identity.application.port.out.PasswordHasherPort;
import ms.identity.application.port.out.UserRepositoryPort;
import ms.identity.application.port.out.VerificationEmailRequestPort;
import ms.identity.application.results.RegisterUserResult;
import ms.identity.domain.model.EmailAddress;
import ms.identity.domain.model.PasswordHash;
import ms.identity.domain.model.User;

@Service
public class RegisterUserService implements RegisterUserUseCase {
    private final PasswordHasherPort passwordHasher;
    private final UserRepositoryPort users;
    private final Clock clock;
    private final VerificationEmailRequestPort verificationRequests;

    public RegisterUserService(PasswordHasherPort passwordHasher,UserRepositoryPort users,Clock clock) {
        this(passwordHasher,users,clock,user -> {});
    }

    @Autowired
    public RegisterUserService(PasswordHasherPort passwordHasher,UserRepositoryPort users,Clock clock,
            VerificationEmailRequestPort verificationRequests) {
        this.passwordHasher=passwordHasher; this.users=users; this.clock=clock;
        this.verificationRequests=verificationRequests;
    }

    @Override @Transactional
    public RegisterUserResult register(RegisterUserCommand command) {
        EmailAddress email=EmailAddress.of(command.email());
        if(users.existsByEmail(email)) throw new EmailAlreadyRegisteredException();
        PasswordHash passwordHash=new PasswordHash(passwordHasher.hash(command.rawPassword()));
        Instant now=Instant.now(clock);
        User user=User.register(UUID.randomUUID(),email,passwordHash,command.firstName(),command.lastName(),now);
        users.save(user);
        verificationRequests.requestVerification(user);
        return new RegisterUserResult(user.getId(),user.getEmail().value(),user.getStatus(),now);
    }
}
