package data.DataController;

import habitsapp.data.DataController;
import habitsapp.models.User;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class UserEditingTest {

    User user = new User("userName7", "example7@mail.ru", "userPass7");

    @Test
    void shouldUpdateUserAttributes() {
        DataController.addUser(user);
        User newUser = user.clone();
        newUser.setEmail("newEmailExample@mail.ru");
        DataController.editUserData(user.getEmail(), newUser);
        assertThat(DataController.userExists(user.getEmail())).isEqualTo(false);
    }

}
