package ms.identity.application.command;

public record LoginCommand(String email, String password, String clientType) {
}
