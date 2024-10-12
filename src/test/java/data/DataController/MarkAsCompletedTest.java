package data.DataController;

import habitsapp.data.DataController;
import habitsapp.models.Habit;
import habitsapp.models.User;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MarkAsCompletedTest {

    Habit habit = new Habit("habitTitle3", "habitDescription3", 1);
    User user = new User("userName3", "example3@mail.ru", "userPass3");

    @Test
    void shouldAddHabitToCollection() {
        DataController.addUser(user);
        DataController.addHabit(user.getEmail(), habit);
        assertThat(DataController.markAsCompleted(user.getEmail(), habit)).isEqualTo(true);
        assertThat(DataController.markAsCompleted(user.getEmail(), habit)).isEqualTo(false);
    }

}
