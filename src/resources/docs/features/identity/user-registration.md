# Feature 01 — User Registration

## 1. Overview

The User Registration feature is responsible for creating a new user within the Identity service.

The Domain Layer encapsulates the business rules and protects the consistency of the User entity independently of frameworks and infrastructure.

It follows Hexagonal Architecture:
- **Domain Layer**: Encapsulates business rules and protects the consistency of the User aggregate.
- **Application Layer**: Orchestrates the registration use case through inbound and outbound ports.
- **Persistence Adapter**: Will provide database-backed user persistence through the application port.

## 2. Domain Model

### User

**User** is the aggregate root responsible for managing the user lifecycle.

It contains:

```
UUID id
EmailAddress email
PasswordHash passwordHash
String firstName
String lastName
UserStatus status
Instant createdAt
Instant updatedAt
```

User creation is performed through the **User.register(...)** factory method.

A newly registered user starts with **PENDING_VERIFICATION** status.

### Value Objects

**EmailAddress**

- Rejects null and invalid email addresses.
- Trims whitespace.
- Normalizes email addresses to lowercase.
- Enforces a maximum length of 254 characters.
- Enforces a maximum local-part length of 64 characters.

**PasswordHash**

- Rejects null and blank encoded values.
- Represents an already encoded password hash.
- Redacts its value in `toString()`.

### UserStatus

Available states:

- `PENDING_VERIFICATION`
- `ACTIVE`
- `BLOCKED`

## 3. User Lifecycle

Allowed transitions:

| Current state | Operation | Next state |
|---|---|---|
| PENDING_VERIFICATION | activate | ACTIVE |
| ACTIVE | block | BLOCKED |
| BLOCKED | unblock | ACTIVE |

Invalid state transitions are rejected using `InvalidUserStateException`.

Lifecycle timestamps cannot move backwards relative to `updatedAt`.

## 4. Business Rules

- A user must have a valid email address.
- A user must have a valid password hash.
- First and last names cannot be blank.
- First and last names cannot exceed 100 characters.
- User creation initializes the status to `PENDING_VERIFICATION`.
- User identity is based on its UUID.
- Invalid lifecycle transitions are rejected.

## 5. Application Layer

The Application Layer implements the user registration use case without depending on Spring, JPA, or a specific password hashing implementation.

### 5.1 RegisterUserCommand

`RegisterUserCommand` carries the input required to register a user:

- `email`
- `rawPassword`
- `firstName`
- `lastName`

### 5.2 RegisterUserUseCase

The inbound port exposes the registration operation:

`register(RegisterUserCommand command)`

It returns a dedicated `RegisterUserResult`.

### 5.3 RegisterUserService

`RegisterUserService` implements `RegisterUserUseCase` and orchestrates the registration process.

Registration flow:

1. Convert the supplied email into an `EmailAddress`.
2. Check whether the email is already registered.
3. Hash the raw password through `PasswordHasherPort`.
4. Create a `PasswordHash` from the encoded password.
5. Obtain the current timestamp through the injected `Clock`.
6. Create the User aggregate using `User.register(...)`.
7. Persist the user through `UserRepositoryPort`.
8. Return a `RegisterUserResult`.

### 5.4 Outbound Ports

**UserRepositoryPort**

Defines the persistence operations required by the use case:

- `existsByEmail(EmailAddress email)`
- `save(User user)`

**PasswordHasherPort**

Defines the password hashing operation:

- `hash(String rawPassword)`

The Application Layer depends on these abstractions rather than infrastructure implementations.

### 5.5 RegisterUserResult

The use case returns a dedicated output model containing:

- `UUID id`
- `String email`
- `UserStatus status`
- `Instant createdAt`

This prevents the application boundary from exposing the Domain aggregate directly.

### 5.6 Email Uniqueness

Email uniqueness is protected through two complementary mechanisms:

- **Application pre-check:** `UserRepositoryPort.existsByEmail(...)` detects an already registered email before password hashing.
- **Database unique constraint:** Provides the final guarantee against concurrent registration attempts.

The persistence adapter is responsible for translating a database duplicate-key violation into `EmailAlreadyRegisteredException`, an Application Layer exception.

The exception does not expose the submitted email address.

### 5.7 Time Management

`RegisterUserService` receives a `Clock` through constructor injection.

This keeps time-dependent application behavior deterministic and testable.

## 6. Testing

### Domain Layer

Tests cover:

- User creation and initial state.
- Email validation and normalization.
- Password hash validation.
- User lifecycle transitions.
- Invalid state transitions.
- Timestamp consistency.

### Application Layer

Tests cover:

- Successful registration.
- Existing email rejection.
- Password hashing and persistence orchestration.
- Returning the registration result.

## 7. Implementation Status

| Layer | Status |
|---|---|
| Domain | Completed |
| Application | Completed |
| Persistence Adapter | Next |

**Status: Completed**

The Domain and Application Layers have been implemented and their reported tests pass.

The Persistence Adapter will be implemented next, with database mapping, email uniqueness enforcement, duplicate-key translation, and adapter tests.