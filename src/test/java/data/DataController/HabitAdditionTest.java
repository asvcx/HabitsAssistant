package data.DataController;

import habitsapp.data.DataController;
import habitsapp.models.Habit;
import habitsapp.models.User;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class HabitAdditionTest {

    Habit habit = new Habit("habitTitle", "habitDescription", 1);
    User user = new User("userName", "example@mail.ru", "userPass");

    @Test
    void shouldAddHabitToCollection() {
        DataController.addUser(user);
        assertThat(DataController.getHabits(user.getEmail())).isEmpty();
        DataController.addHabit(user.getEmail(), habit);
        assertThat(DataController.getHabits(user.getEmail())).isNotEmpty();
    }

}
