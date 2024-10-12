package data.DataController;

import habitsapp.data.DataController;
import habitsapp.models.User;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class UserAdditionTest {

    User user = new User("userName5", "example5@mail.ru", "userPass5");

    @Test
    void shouldAddUserToCollection() {
        assertThat(DataController.userExists(user.getEmail())).isEqualTo(false);
        DataController.addUser(user);
        assertThat(DataController.userExists(user.getEmail())).isEqualTo(true);
    }

}
