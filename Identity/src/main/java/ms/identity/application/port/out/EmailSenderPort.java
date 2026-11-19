package ms.identity.application.port.out;
public interface EmailSenderPort {
    void sendVerificationEmail(String recipient, String firstName, String verificationUrl);
}
