package ms.identity.adapter.out.email;

import static org.mockito.Mockito.*;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ms.identity.application.port.out.EmailSenderPort;
import ms.identity.application.port.out.OutboxRepositoryPort;
import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class VerificationOutboxWorkerTest {
    @Mock private OutboxRepositoryPort outbox;
    @Mock private EmailSenderPort emailSender;

    private final Instant now=Instant.parse("2026-11-20T10:00:00Z");
    private VerificationOutboxWorker worker;

    @BeforeEach
    void setUp() {
        worker=new VerificationOutboxWorker(outbox,emailSender,new ObjectMapper(),Clock.fixed(now,ZoneOffset.UTC));
    }

    @Test
    void sendsVerificationEmailAndMarksOutboxEventPublished() {
        UUID id=UUID.randomUUID();
        String payload="{\"email\":\"user@example.com\",\"firstName\":\"Test\",\"verificationUrl\":\"https://app.example/verify#token=abc\"}";
        when(outbox.claimPending(now,25)).thenReturn(List.of(
                new OutboxRepositoryPort.OutboxEvent(id,"USER_EMAIL_VERIFICATION_REQUESTED",payload,0)));

        worker.publishPending();

        verify(emailSender).sendVerificationEmail("user@example.com","Test","https://app.example/verify#token=abc");
        verify(outbox).markPublished(id,now);
        verify(outbox,never()).scheduleRetry(any(),anyInt(),any(),any());
    }

    @Test
    void schedulesExponentialRetryWhenEmailDeliveryFails() {
        UUID id=UUID.randomUUID();
        String payload="{\"email\":\"user@example.com\",\"firstName\":\"Test\",\"verificationUrl\":\"https://app.example/verify#token=abc\"}";
        when(outbox.claimPending(now,25)).thenReturn(List.of(
                new OutboxRepositoryPort.OutboxEvent(id,"USER_EMAIL_VERIFICATION_REQUESTED",payload,1)));
        doThrow(new IllegalStateException("SMTP unavailable"))
                .when(emailSender).sendVerificationEmail("user@example.com","Test","https://app.example/verify#token=abc");

        worker.publishPending();

        verify(outbox).scheduleRetry(eq(id),eq(2),eq(now.plusSeconds(4)),contains("SMTP unavailable"));
        verify(outbox,never()).markPublished(any(),any());
    }
}
