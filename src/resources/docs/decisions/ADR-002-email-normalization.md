# ADR-002: Email Address as a Value Object

## Status

Accepted

## Context

The Identity Service requires a consistent representation of user email addresses.

Email addresses must be validated and normalized before they enter the domain model.

## Decision

Represent email addresses using an immutable `EmailAddress` value object.

The value object is responsible for:

- Validating email format.
- Normalizing surrounding whitespace.
- Applying the selected case-normalization policy.
- Enforcing maximum email length.
- Providing value-based equality.

## Consequences

Positive:
- Centralized validation.
- Consistent email representation.
- Stronger domain typing.
- Easier unit testing.

Trade-offs:
- The current validation policy supports a restricted ASCII email format.
- Case-insensitive normalization is a deliberate business policy.
- Actual email ownership must be verified through a separate workflow.

## Alternatives Considered

- Using raw String throughout the domain.
- Performing all validation in the REST Controller.

These alternatives were rejected because they distribute validation responsibilities and weaken domain guarantees.