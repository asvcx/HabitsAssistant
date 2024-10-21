package habitsapp.data.repository;

import habitsapp.data.repository.Repository;
import habitsapp.ui.in.UserInputByConsole;
import habitsapp.data.models.Habit;
import habitsapp.data.models.User;
import org.junit.jupiter.api.Test;

import static habitsapp.data.repository.DataLoader.repository;
import static habitsapp.data.repository.DataLoader.userInput;
import static org.assertj.core.api.Assertions.assertThat;

public class RepositoryTest {

    private final User user = new User("Name", "name@mail.ru", "UserPass");
    private final Habit habit = new Habit("Title", "Description", 1);
    private final User existingUser = new User("ExistingName", "existing@mail.ru", "ExistingPass");
    private final Habit existingHabit = new Habit("ExistingTitle", "ExistingDescription", 1);

    /*
    AccountRepositoryTest() {
        accountRepository.loadUser(existingUser);
        assertThat(accountRepository.isUserExists(existingUser.getEmail())).isEqualTo(true);
        accountRepository.loadHabit(existingUser.getEmail(), existingHabit);
        assertThat(accountRepository.getHabitsList(existingUser.getEmail())).isNotEmpty();
    }

     */

    @Test
    void shouldAddThenRemoveUserToCollection() {
        repository = new Repository();
        userInput = new UserInputByConsole();
        repository.loadUser(user);
        assertThat(repository.isUserExists(user.getEmail())).isEqualTo(true);
        repository.deleteOwnAccount(user.getEmail(), "UserPass");
        assertThat(repository.isUserExists(user.getEmail())).isEqualTo(false);
    }

    @Test
    void shouldAddThenRemoveUserAndHabit() {
        repository = new Repository();
        userInput = new UserInputByConsole();
        repository.loadUser(user);
        assertThat(repository.getHabitsSet(user.getEmail())).isEmpty();
        repository.loadHabit(user.getEmail(), habit);
        assertThat(repository.getHabitsSet(user.getEmail())).isNotEmpty();
        repository.deleteHabit(user.getEmail(), habit.getTitle());
        assertThat(repository.getHabitsSet(user.getEmail())).isEmpty();
        repository.deleteOwnAccount(user.getEmail(), "UserPass");
        assertThat(repository.isUserExists(user.getEmail())).isEqualTo(false);
    }

    @Test
    void shouldUpdateHabitTitle() {
        repository = new Repository();
        userInput = new UserInputByConsole();
        repository.loadUser(existingUser);
        repository.loadHabit(existingUser.getEmail(), existingHabit);
        Habit changedHabit = existingHabit.clone();
        changedHabit.setTitle("NewTitle");
        assertThat(repository.editHabit(existingUser.getEmail(), existingHabit, changedHabit)).isEqualTo(true);
    }

    @Test
    void shouldUpdateHabitDescription() {
        repository = new Repository();
        userInput = new UserInputByConsole();
        repository.loadUser(existingUser);
        repository.loadHabit(existingUser.getEmail(), existingHabit);
        Habit changedHabit = existingHabit.clone();
        changedHabit.setDescription("NewDescription");
        assertThat(repository.editHabit(existingUser.getEmail(), existingHabit, changedHabit)).isEqualTo(true);
    }

    @Test
    void shouldUpdateHabitPeriod() {
        repository = new Repository();
        userInput = new UserInputByConsole();
        repository.loadUser(existingUser);
        repository.loadHabit(existingUser.getEmail(), existingHabit);
        Habit changedHabit = existingHabit.clone();
        changedHabit.setPeriod(7);
        assertThat(repository.editHabit(existingUser.getEmail(), existingHabit, changedHabit)).isEqualTo(true);
    }

    @Test
    void shouldMarkHabitInCollection() {
        repository = new Repository();
        userInput = new UserInputByConsole();
        repository.loadUser(existingUser);
        repository.loadHabit(existingUser.getEmail(), existingHabit);
        assertThat(repository.markHabitAsCompleted(existingUser.getEmail(), existingHabit)).isEqualTo(true);
        assertThat(repository.markHabitAsCompleted(user.getEmail(), habit)).isEqualTo(false);
    }

    @Test
    void shouldAuthorizeUserAndThenReturnUserOrNull() {
        repository = new Repository();
        userInput = new UserInputByConsole();
        repository.loadUser(existingUser);
        repository.loadHabit(existingUser.getEmail(), existingHabit);
        assertThat(repository.userAuth(existingUser.getEmail(), "WrongPass").isPresent()).isFalse();
        assertThat(repository.userAuth(existingUser.getEmail(), "ExistingPass").isPresent()).isTrue();
    }

    @Test
    void shouldUpdateUserEmail() {
        repository = new Repository();
        userInput = new UserInputByConsole();
        repository.loadUser(existingUser);
        repository.loadHabit(existingUser.getEmail(), existingHabit);
        User changedUser = existingUser.clone();
        changedUser.setEmail("newemail@mail.ru");
        assertThat(repository.isUserExists(existingUser.getEmail())).isEqualTo(true);
        assertThat(repository.isUserExists(changedUser.getEmail())).isEqualTo(false);

        repository.editUserData(existingUser.getEmail(), changedUser, "ExistingPass");
        assertThat(repository.isUserExists(existingUser.getEmail())).isEqualTo(false);
        assertThat(repository.isUserExists(changedUser.getEmail())).isEqualTo(true);

        repository.editUserData(changedUser.getEmail(), existingUser, "ExistingPass");
        assertThat(repository.isUserExists(existingUser.getEmail())).isEqualTo(true);
        assertThat(repository.isUserExists(changedUser.getEmail())).isEqualTo(false);
    }

    @Test
    void shouldRemoveAndThenLoadUserToCollection() {
        repository = new Repository();
        userInput = new UserInputByConsole();
        repository.loadUser(existingUser);
        repository.loadHabit(existingUser.getEmail(), existingHabit);
        assertThat(repository.isUserExists(existingUser.getEmail())).isEqualTo(true);
        repository.deleteOwnAccount(existingUser.getEmail(), "ExistingPass");
        assertThat(repository.isUserExists(existingUser.getEmail())).isEqualTo(false);
        repository.loadUser(existingUser);
        assertThat(repository.isUserExists(existingUser.getEmail())).isEqualTo(true);
    }

}
