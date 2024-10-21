package habitsapp.ui.session;

import habitsapp.data.models.User;
import habitsapp.ui.session.Session;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SessionTest {

    private final static User admin = new User("Admin", "admin@google.com", "AdminPass");

    @Test
    void shouldSetCurrentProfileAndThenExit() {
        // Given
        Session.setCurrentProfile(admin);
        // When
        assertThat(Session.isAuthorized()).isEqualTo(true);
        Session.exitFromProfile();
        // Then
        assertThat(Session.isAuthorized()).isEqualTo(false);
    }

    @Test
    void shouldPassWhenAuthorizedAdmin() {
        // Given
        admin.setAccessLevel(User.AccessLevel.ADMIN);
        Session.setCurrentProfile(admin);
        // When
        assertThat(Session.isAdmin()).isEqualTo(true);
        Session.exitFromProfile();
        // Then
        assertThat(Session.isAdmin()).isEqualTo(false);
    }

}
