package data.DataController;

import habitsapp.data.DataController;
import habitsapp.models.User;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class UserRemovingTest {

    User user = new User("userName8", "example8@mail.ru", "userPass8");

    @Test
    void shouldRemoveUserFromCollection() {
        DataController.addUser(user);
        assertThat(DataController.userExists(user.getEmail())).isEqualTo(true);
        DataController.deleteUserProfile(user.getEmail(), "userPass8");
        assertThat(DataController.userExists(user.getEmail())).isEqualTo(false);
    }

}
