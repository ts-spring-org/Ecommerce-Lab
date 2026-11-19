package ms.identity.adapter.in.web.dtos;
import jakarta.validation.constraints.NotBlank;
public record VerifyEmailRequest(@NotBlank String token) {}
