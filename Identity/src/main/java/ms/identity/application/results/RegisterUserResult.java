package ms.identity.application.results;

import java.time.Instant;
import java.util.UUID;

import ms.identity.domain.model.UserStatus;

public record RegisterUserResult(UUID id, String email, UserStatus status, Instant createdAt) {}