package ms.identity.application.command;

public record RegisterUserCommand(String email, String rawPassword, String firstName, String lastName) {}
