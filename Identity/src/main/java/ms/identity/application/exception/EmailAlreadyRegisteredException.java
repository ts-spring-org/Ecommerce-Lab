package ms.identity.application.exception;

public class EmailAlreadyRegisteredException extends RuntimeException{
	private static final long serialVersionUID = 7670087291692444232L;
	
	public EmailAlreadyRegisteredException() {
		super("Email already exists.");
	}

}
