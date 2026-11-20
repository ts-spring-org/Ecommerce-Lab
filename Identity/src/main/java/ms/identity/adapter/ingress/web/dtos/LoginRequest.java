package ms.identity.adapter.ingress.web.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import ms.identity.adapter.ingress.web.validation.MaxUtf8Bytes;

public record LoginRequest(@NotBlank @Email @Size(max=254) String email,
        @NotBlank @Size(min=8,max=72) @MaxUtf8Bytes(max=72) String password) {}
