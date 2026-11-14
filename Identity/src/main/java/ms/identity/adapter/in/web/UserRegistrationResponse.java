package ms.identity.adapter.in.web;

import java.time.Instant;
import java.util.UUID;

import ms.identity.domain.model.UserStatus;

public record UserRegistrationResponse(UUID id, String email, UserStatus status, Instant createdAt) {}