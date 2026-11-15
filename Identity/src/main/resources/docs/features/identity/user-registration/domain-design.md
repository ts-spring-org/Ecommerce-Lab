# User Registration — Domain Design

## 1. Overview

The User Registration feature is responsible for creating a new user account in the Identity service.

The Domain Layer defines the core business concepts, invariants, and lifecycle rules independently of frameworks and infrastructure.

## 2. Domain Model

### User

`User` is the main domain entity.

It contains:

- `id`: Unique user identifier (UUID).
- `email`: User's normalized email address.
- `passwordHash`: Encoded password hash.
- `firstName`: User's first name.
- `lastName`: User's last name.
- `status`: Current account status.
- `createdAt`: Account creation timestamp.
- `updatedAt`: Last modification timestamp.

The entity controls its own lifecycle and protects its invariants.

### EmailAddress

`EmailAddress` is a Value Object responsible for email validation and normalization.

Rules:

- Must not be null or blank.
- Leading and trailing whitespace is removed.
- Email is normalized to lowercase.
- Maximum total length: 254 characters.
- Maximum local-part length: 64 characters.

### PasswordHash

`PasswordHash` is a Value Object representing an encoded password.

Rules:

- Must not be null.
- Must not be blank.
- Its string representation must never expose the encoded value.

The Domain Layer does not hash raw passwords. Password hashing belongs to an application-level abstraction implemented through an appropriate adapter.

### UserStatus

Supported account statuses:

- `PENDING_VERIFICATION`
- `ACTIVE`
- `BLOCKED`

## 3. Business Rules

1. A user must have a valid identifier.
2. A user must have a valid email address.
3. A user must have a non-empty password hash.
4. First name and last name must not be blank.
5. Names must not exceed 100 characters.
6. A newly registered user starts with `PENDING_VERIFICATION`.
7. A user can only be activated from `PENDING_VERIFICATION`.
8. A user can only be blocked from `ACTIVE`.
9. A user can only be unblocked from `BLOCKED`.
10. Lifecycle transitions must not move the `updatedAt` timestamp backwards.

## 4. Lifecycle Transitions

| Current Status | Operation | New Status |
|---|---|---|
| PENDING_VERIFICATION | activate | ACTIVE |
| ACTIVE | block | BLOCKED |
| BLOCKED | unblock | ACTIVE |

Invalid transitions must throw `InvalidUserStateException`.

## 5. Domain Exceptions

### InvalidUserStateException

Thrown when an operation attempts an invalid user lifecycle transition.

Invalid input to Value Objects or entity construction is rejected through the relevant validation exceptions.

## 6. Design Principles

- Framework-independent Domain Layer.
- No Spring dependencies.
- No JPA or persistence annotations.
- No infrastructure dependencies.
- Encapsulation of business invariants.
- Value Objects for validated domain concepts.
- Explicit lifecycle transitions.
- Entity equality based on user ID.

## 7. Domain Tests

The Domain Layer tests cover:

- Email validation and normalization.
- Password hash validation and redaction.
- User creation.
- Name validation.
- Initial account status.
- Valid lifecycle transitions.
- Invalid lifecycle transitions.
- Timestamp consistency.
- Entity equality and hash code.

## 8. Scope Boundaries

This Domain Layer does not handle:

- HTTP requests or responses.
- Password hashing implementation.
- Database persistence.
- Email verification delivery.
- Authentication tokens.
- Duplicate email checks against persisted users.

These responsibilities belong to the Application Layer or infrastructure adapters.

## 9. Status

**Domain Layer: Completed**

This document describes the agreed Domain Layer design for User Registration. Any future change to domain behavior must be reflected here and reviewed against the implementation and tests.
