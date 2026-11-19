package ms.identity.application.port.out;

import java.util.List;
import java.util.UUID;

public interface AccessTokenProviderPort {
    String issue(UUID userId, List<String> authorities);
}
