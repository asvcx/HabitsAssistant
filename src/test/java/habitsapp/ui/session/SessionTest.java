package habitsapp.ui.session;

import habitsapp.data.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SessionTest {

    private final static User admin = new User("Admin", "admin@google.com", "AdminPass");

    @BeforeEach
    void setUp() {
        admin.setAccessLevel(User.AccessLevel.ADMIN);
        Session.setCurrentProfile(admin);
    }

    @Test
    @DisplayName("Should set current profile in Session and then exit")
    void shouldSetCurrentProfileAndThenExit() {
        // Given
        assertThat(Session.isAuthorized()).isTrue();
        // When
        Session.exitFromProfile();
        // Then
        assertThat(Session.isAuthorized()).isFalse();
    }

    @Test
    @DisplayName("Should check if current profile is admin")
    void shouldPassWhenAuthorizedAdmin() {
        // Given
        assertThat(Session.isAdmin()).isTrue();
        // When
        Session.exitFromProfile();
        // Then
        assertThat(Session.isAdmin()).isFalse();
    }

}
