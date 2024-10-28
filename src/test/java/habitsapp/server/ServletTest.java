package habitsapp.server;

import org.habitsapp.client.session.AuthorizationResult;
import org.habitsapp.client.session.Request;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ServletTest {
    @Test
    public void shouldLoginAndThenLogout() {
        Request request = new Request();
        AuthorizationResult result = null;
        // Login with wrong credentials
        result = request.login("WrongEmail", "WrongPassword");
        assertThat(result.getSuccess()).isFalse();
        // Login with correct credentials
        result = request.login("admin@mail.ru", "AdminPassword");
        assertThat(result.getSuccess()).isTrue();
        // Logout
        boolean isLoggedOut = request.logout(result.getToken());
        assertThat(result.getSuccess()).isTrue();
    }
}
