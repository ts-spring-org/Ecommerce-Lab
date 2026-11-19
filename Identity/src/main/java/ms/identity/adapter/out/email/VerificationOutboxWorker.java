package ms.identity.adapter.out.email;

import java.time.Clock;
import java.time.Instant;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ms.identity.application.port.out.EmailSenderPort;
import ms.identity.application.port.out.OutboxRepositoryPort;

@Component
@ConditionalOnProperty(name="identity.outbox.enabled", havingValue="true", matchIfMissing=true)
public class VerificationOutboxWorker {
    private final OutboxRepositoryPort outbox;
    private final EmailSenderPort emailSender;
    private final ObjectMapper mapper;
    private final Clock clock;

    public VerificationOutboxWorker(OutboxRepositoryPort outbox, EmailSenderPort emailSender,
            ObjectMapper mapper, Clock clock) {
        this.outbox=outbox; this.emailSender=emailSender; this.mapper=mapper; this.clock=clock;
    }

    @Scheduled(fixedDelayString="${identity.outbox.poll-interval:5000}")
    @Transactional
    public void publishPending() {
        Instant now=Instant.now(clock);
        for (var event : outbox.claimPending(now,25)) {
            try {
                if (!"USER_EMAIL_VERIFICATION_REQUESTED".equals(event.eventType()))
                    throw new IllegalArgumentException("Unsupported outbox event type: " + event.eventType());
                var payload=mapper.readTree(event.payload());
                emailSender.sendVerificationEmail(payload.path("email").asText(),
                        payload.path("firstName").asText(),payload.path("verificationUrl").asText());
                outbox.markPublished(event.id(),Instant.now(clock));
            } catch (Exception exception) {
                int attempts=event.attempts()+1;
                long delay=Math.min(3600L,1L << Math.min(attempts,12));
                outbox.scheduleRetry(event.id(),attempts,Instant.now(clock).plusSeconds(delay),exception.toString());
            }
        }
    }
}
