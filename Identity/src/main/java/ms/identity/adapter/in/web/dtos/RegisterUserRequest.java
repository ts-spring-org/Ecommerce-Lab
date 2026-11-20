package ms.identity.adapter.in.web.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import ms.identity.adapter.in.web.validation.MaxUtf8Bytes;

public record RegisterUserRequest(
		@NotBlank @Email @Size(max = 254) String email,

		@NotBlank @Size(min = 8, max = 72) @MaxUtf8Bytes(max = 72) String password,

		@NotBlank @Size(max = 100) String firstName,

		@NotBlank @Size(max = 100) String lastName
) {}