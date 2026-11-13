package ms.identity.domain.model;

public record PasswordHash(String password) {
	
	public PasswordHash {
		if(password == null || password.isBlank()) {
			throw new IllegalArgumentException("Password cannot be null or blank");
		}
		
		if(password.length < 8) {
			throw new IllegalArgumentException("Password should be minimum 8 characters");
		}
	}

}
