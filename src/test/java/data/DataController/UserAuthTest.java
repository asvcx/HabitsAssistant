package data.DataController;

import habitsapp.data.DataController;
import habitsapp.models.User;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class UserAuthTest {

    User user = new User("userName6", "example6@mail.ru", "userPass6");

    @Test
    void shouldAuthorizeUser() {
        assertThat(DataController.userAuth(user.getEmail(), "userPass6")).isNull();
        DataController.addUser(user);
        assertThat(DataController.userAuth(user.getEmail(), "userPass6")).isNotNull();
    }

}
