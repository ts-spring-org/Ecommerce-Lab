package ms.identity.application.service;

import java.time.Clock;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ms.identity.application.port.in.ResendVerificationEmailUseCase;
import ms.identity.application.port.out.UserRepositoryPort;
import ms.identity.application.port.out.VerificationEmailRequestPort;
import ms.identity.domain.model.EmailAddress;
import ms.identity.domain.model.UserStatus;

@Service
public class ResendVerificationEmailService implements ResendVerificationEmailUseCase {
    private final UserRepositoryPort users;
    private final VerificationEmailRequestPort requests;
    public ResendVerificationEmailService(UserRepositoryPort users,VerificationEmailRequestPort requests) {
        this.users=users; this.requests=requests;
    }
    @Override @Transactional
    public void resend(String email) {
        users.findByEmail(EmailAddress.of(email))
                .filter(user -> user.getStatus()==UserStatus.PENDING_VERIFICATION)
                .ifPresent(requests::requestVerification);
        // Same response for unknown, active, blocked and pending addresses prevents account enumeration.
    }
}
