package data.DataController;

import habitsapp.data.DataController;
import habitsapp.models.Habit;
import habitsapp.models.User;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class HabitEditingTest {

    Habit habit;
    User user;
    Habit newHabit;

    void addUserAndHabit() {
        habit = new Habit("habitTitle2", "habitDescription2", 1);
        newHabit = habit.clone();
        user = new User("userName2", "example2@mail.ru", "userPass2");
        DataController.addUser(user);
        DataController.addHabit(user.getEmail(), habit);
    }

    @Test
    void shouldUpdateHabitTitle() {
        addUserAndHabit();
        newHabit.setTitle("newTitle");
        assertThat(DataController.editHabit(user.getEmail(), habit, newHabit)).isEqualTo(true);
    }

    @Test
    void shouldUpdateHabitDescription() {
        addUserAndHabit();
        newHabit.setPeriod(7);
        assertThat(DataController.editHabit(user.getEmail(), habit, newHabit)).isEqualTo(true);
    }

    @Test
    void shouldUpdateHabitPeriod() {
        addUserAndHabit();
        newHabit.setPeriod(7);
        assertThat(DataController.editHabit(user.getEmail(), habit, newHabit)).isEqualTo(true);
    }

}
