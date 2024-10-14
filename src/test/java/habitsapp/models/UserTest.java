package habitsapp.models;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class UserTest {

    private final User user = new User("Name", "name@mail.ru", "UserPass");

    @Test
    void shouldReturnTrueWhenUserIsAuthentic() {
        User clonedUser = user.clone();
        assertThat(user.isUserAuthentic(clonedUser)).isEqualTo(true);
        clonedUser.setPassword("NewPass");
        assertThat(user.isUserAuthentic(clonedUser)).isEqualTo(false);
    }

}
