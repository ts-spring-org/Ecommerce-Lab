package ms.identity.adapter.ingress.web.dtos;
import jakarta.validation.constraints.NotBlank;
public record VerifyEmailRequest(@NotBlank String token) {}
