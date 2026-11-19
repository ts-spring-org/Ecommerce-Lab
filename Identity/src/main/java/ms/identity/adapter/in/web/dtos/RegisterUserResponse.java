package ms.identity.adapter.in.web.dtos;

import java.time.Instant;
import java.util.UUID;

import ms.identity.domain.model.UserStatus;

public record RegisterUserResponse(UUID id, String email, UserStatus status, Instant createdAt) {}
