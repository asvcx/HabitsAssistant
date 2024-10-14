package habitsapp.session;

import habitsapp.models.User;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SessionTest {

    private final User admin = new User("Admin", "admin@google.com", "AdminPass");

    @Test
    void shouldSetCurrentProfileAndThenExit() {
        Session.setCurrentProfile(admin);
        assertThat(Session.isAuthorized()).isEqualTo(true);
        Session.exitFromProfile();
        assertThat(Session.isAuthorized()).isEqualTo(false);
    }

    @Test
    void shouldPassWhenAuthorizedAdmin() {
        admin.setAccessLevel(User.AccessLevel.ADMIN);
        Session.setCurrentProfile(admin);
        assertThat(Session.isAdmin()).isEqualTo(true);
        Session.exitFromProfile();
        assertThat(Session.isAdmin()).isEqualTo(false);
    }


}
