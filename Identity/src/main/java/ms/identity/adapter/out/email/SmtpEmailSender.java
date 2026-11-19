package ms.identity.adapter.out.email;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import ms.identity.application.port.out.EmailSenderPort;

@Component
public class SmtpEmailSender implements EmailSenderPort {
    private final JavaMailSender mailSender;
    private final String from;
    public SmtpEmailSender(JavaMailSender mailSender,@Value("${MAIL_FROM:no-reply@example.com}") String from) {
        this.mailSender=mailSender; this.from=from;
    }
    @Override public void sendVerificationEmail(String recipient,String firstName,String verificationUrl) {
        try {
            MimeMessage message=mailSender.createMimeMessage();
            MimeMessageHelper helper=new MimeMessageHelper(message,true,"UTF-8");
            helper.setFrom(from); helper.setTo(recipient); helper.setSubject("Verify your email address");
            helper.setText("<!doctype html><html><body><h2>Hello "+escape(firstName)+"</h2>"
                    +"<p>Please verify your email address to activate your account.</p>"
                    +"<p><a href=\""+escape(verificationUrl)+"\">Verify email address</a></p>"
                    +"<p>This link expires in 24 hours. If you did not create this account, ignore this email.</p>"
                    +"</body></html>",true);
            mailSender.send(message);
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to send verification email",exception);
        }
    }
    private static String escape(String value) {
        return value.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;")
                .replace("\"","&quot;").replace("'","&#39;");
    }
}
