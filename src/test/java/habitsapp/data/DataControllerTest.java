package habitsapp.data;

import habitsapp.models.Habit;
import habitsapp.models.User;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DataControllerTest {

    private final User user = new User("Name", "name@mail.ru", "UserPass");
    private final Habit habit = new Habit("Title", "Description", 1);
    private final User existingUser = new User("ExistingName", "existing@mail.ru", "ExistingPass");
    private final Habit existingHabit = new Habit("ExistingTitle", "ExistingDescription", 1);

    DataControllerTest() {
        DataController.addUser(existingUser);
        assertThat(DataController.userExists(existingUser.getEmail())).isEqualTo(true);
        DataController.addHabit(existingUser.getEmail(), existingHabit);
        assertThat(DataController.getHabits(existingUser.getEmail())).isNotEmpty();
    }

    @Test
    void shouldAddThenRemoveUserToCollection() {
        DataController.addUser(user);
        assertThat(DataController.userExists(user.getEmail())).isEqualTo(true);
        DataController.deleteUserProfile(user.getEmail(), "UserPass");
        assertThat(DataController.userExists(user.getEmail())).isEqualTo(false);
    }

    @Test
    void shouldAddThenRemoveUserAndHabit() {
        DataController.addUser(user);
        assertThat(DataController.getHabits(user.getEmail())).isEmpty();
        DataController.addHabit(user.getEmail(), habit);
        assertThat(DataController.getHabits(user.getEmail())).isNotEmpty();
        DataController.deleteHabit(user.getEmail(), habit);
        assertThat(DataController.getHabits(user.getEmail())).isEmpty();
        DataController.deleteUserProfile(user.getEmail(), "UserPass");
        assertThat(DataController.userExists(user.getEmail())).isEqualTo(false);
    }

    @Test
    void shouldUpdateHabitTitle() {
        Habit changedHabit = existingHabit.clone();
        changedHabit.setTitle("NewTitle");
        assertThat(DataController.editHabit(existingUser.getEmail(), existingHabit, changedHabit)).isEqualTo(true);
    }

    @Test
    void shouldUpdateHabitDescription() {
        Habit changedHabit = existingHabit.clone();
        changedHabit.setDescription("NewDescription");
        assertThat(DataController.editHabit(existingUser.getEmail(), existingHabit, changedHabit)).isEqualTo(true);
    }

    @Test
    void shouldUpdateHabitPeriod() {
        Habit changedHabit = existingHabit.clone();
        changedHabit.setPeriod(7);
        assertThat(DataController.editHabit(existingUser.getEmail(), existingHabit, changedHabit)).isEqualTo(true);
    }

    @Test
    void shouldMarkHabitInCollection() {
        assertThat(DataController.markAsCompleted(existingUser.getEmail(), existingHabit)).isEqualTo(true);
        assertThat(DataController.markAsCompleted(user.getEmail(), habit)).isEqualTo(false);
    }

    @Test
    void shouldAuthorizeUserAndThenReturnUserOrNull() {
        assertThat(DataController.userAuth(existingUser.getEmail(), "WrongPass")).isNull();
        assertThat(DataController.userAuth(existingUser.getEmail(), "ExistingPass")).isNotNull();
    }

    @Test
    void shouldUpdateUserEmail() {
        User changedUser = existingUser.clone();
        changedUser.setEmail("newemail@mail.ru");
        assertThat(DataController.userExists(existingUser.getEmail())).isEqualTo(true);
        assertThat(DataController.userExists(changedUser.getEmail())).isEqualTo(false);

        DataController.editUserData(existingUser.getEmail(), changedUser);
        assertThat(DataController.userExists(existingUser.getEmail())).isEqualTo(false);
        assertThat(DataController.userExists(changedUser.getEmail())).isEqualTo(true);

        DataController.editUserData(changedUser.getEmail(), existingUser);
        assertThat(DataController.userExists(existingUser.getEmail())).isEqualTo(true);
        assertThat(DataController.userExists(changedUser.getEmail())).isEqualTo(false);
    }

    @Test
    void shouldRemoveAndThenAddUserToCollection() {
        assertThat(DataController.userExists(existingUser.getEmail())).isEqualTo(true);
        DataController.deleteUserProfile(existingUser.getEmail(), "ExistingPass");
        assertThat(DataController.userExists(existingUser.getEmail())).isEqualTo(false);
        DataController.addUser(existingUser);
        assertThat(DataController.userExists(existingUser.getEmail())).isEqualTo(true);
    }

}
