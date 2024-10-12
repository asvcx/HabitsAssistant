package data.DataController;

import habitsapp.data.DataController;
import habitsapp.models.Habit;
import habitsapp.models.User;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TestHabitRemoving {

    Habit habit = new Habit("habitTitle4", "habitDescription4", 1);
    User user = new User("userName4", "example4@mail.ru", "userPass4");

    @Test
    void shouldRemoveHabitFromCollection() {
        DataController.addUser(user);
        DataController.addHabit(user.getEmail(), habit);
        assertThat(DataController.getHabits(user.getEmail())).isNotEmpty();
        DataController.deleteHabit(user.getEmail(), habit);
        assertThat(DataController.getHabits(user.getEmail())).isEmpty();
    }

}
