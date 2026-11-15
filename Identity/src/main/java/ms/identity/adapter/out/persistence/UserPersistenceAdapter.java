package ms.identity.adapter.out.persistence;

import ms.identity.application.port.out.UserRepositoryPort;
import ms.identity.domain.model.EmailAddress;
import ms.identity.domain.model.User;

public class UserPersistenceAdapter implements UserRepositoryPort {

	@Override
	public boolean existsByEmail(EmailAddress email) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void save(User user) {
		// TODO Auto-generated method stub
		
	}

}
