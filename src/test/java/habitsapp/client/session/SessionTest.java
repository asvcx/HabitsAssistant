package habitsapp.client.session;

import org.habitsapp.client.session.Session;
import org.habitsapp.models.AccessLevel;
import org.habitsapp.models.dto.UserDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SessionTest {

    @BeforeEach
    void setUp() {
        UserDto adminDTO = new UserDto("Admin", "admin@google.com", "AdminPass", AccessLevel.ADMIN);
        Session.setProfile(adminDTO);
    }

    @Test
    @DisplayName("Should set current profile in Session and then exit")
    void shouldSetProfileAndThenExit() {
        // Given
        assertThat(Session.isAuthorized()).isTrue();
        // When
        Session.logout();
        // Then
        assertThat(Session.isAuthorized()).isFalse();
    }

    @Test
    @DisplayName("Should check if current profile is admin")
    void shouldPassWhenAuthorizedAdmin() {
        // Given
        assertThat(Session.isAdmin()).isTrue();
        // When
        Session.logout();
        // Then
        assertThat(Session.isAdmin()).isFalse();
    }

}
