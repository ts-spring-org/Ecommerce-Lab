package ms.identity.application.port.out;

import java.util.List;
import java.util.UUID;

public interface AccessTokenProviderPort {
    String issue(final UUID userId, final List<String> authorities);
}
