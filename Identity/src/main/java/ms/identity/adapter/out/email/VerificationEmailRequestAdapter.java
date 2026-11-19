package ms.identity.adapter.out.email;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ms.identity.application.port.out.EmailVerificationRepositoryPort;
import ms.identity.application.port.out.OutboxRepositoryPort;
import ms.identity.application.port.out.RefreshTokenGeneratorPort;
import ms.identity.application.port.out.VerificationEmailRequestPort;
import ms.identity.domain.model.User;

@Component
public class VerificationEmailRequestAdapter implements VerificationEmailRequestPort {
    private final EmailVerificationRepositoryPort tokens;
    private final OutboxRepositoryPort outbox;
    private final RefreshTokenGeneratorPort generator;
    private final ObjectMapper mapper;
    private final Clock clock;
    private final String frontendBaseUrl;

    public VerificationEmailRequestAdapter(EmailVerificationRepositoryPort tokens,OutboxRepositoryPort outbox,
            RefreshTokenGeneratorPort generator,ObjectMapper mapper,Clock clock,
            @Value("${identity.email-verification.frontend-base-url}") String frontendBaseUrl) {
        this.tokens=tokens; this.outbox=outbox; this.generator=generator; this.mapper=mapper;
        this.clock=clock; this.frontendBaseUrl=frontendBaseUrl;
    }

    @Override @Transactional
    public void requestVerification(User user) {
        Instant now=Instant.now(clock);
        tokens.invalidateOutstanding(user.getId(),now);
        String raw=generator.generate();
        tokens.insert(UUID.randomUUID(),user.getId(),generator.hash(raw),now,now.plus(Duration.ofHours(24)));
        String url=frontendBaseUrl.replaceAll("/$","")+"/verify-email#token="+raw;
        try {
            String payload=mapper.writeValueAsString(Map.of("email",user.getEmail().value(),
                    "firstName",user.getFirstName(),"verificationUrl",url));
            outbox.enqueue(UUID.randomUUID(),"USER",user.getId(),"USER_EMAIL_VERIFICATION_REQUESTED",payload,now);
        } catch(Exception exception) {
            throw new IllegalStateException("Unable to create verification email outbox event",exception);
        }
    }
}
