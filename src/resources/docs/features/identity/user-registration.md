# Feature 01 — User Registration

## 1. Overview

The User Registration feature is responsible for creating a new user within the Identity service.

The Domain Layer encapsulates the business rules and protects the consistency of the User entity independently of frameworks and infrastructure.

## 2. Domain Model

### User

`User` is the aggregate root responsible for managing the user lifecycle.

It contains:

- `UUID id`
- `EmailAddress email`
- `PasswordHash passwordHash`
- `String firstName`
- `String lastName`
- `UserStatus status`
- `Instant createdAt`
- `Instant updatedAt`

User creation is performed through the `User.register(...)` factory method.

A newly registered user starts with `PENDING_VERIFICATION` status.

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

## 5. Testing

The Domain Layer tests cover:

- User creation and initial state.
- Email validation and normalization.
- Password hash validation.
- User lifecycle transitions.
- Invalid state transitions.
- Timestamp consistency.

## 6. Implementation Status

**Status: Completed**

The Domain Layer implementation and its unit tests have been completed.

Application Layer design and implementation are documented separately as the feature progresses.