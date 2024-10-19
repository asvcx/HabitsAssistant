package habitsapp.repository;

import habitsapp.in.InputDataByConsole;
import habitsapp.models.Habit;
import habitsapp.models.User;
import org.junit.jupiter.api.Test;

import static habitsapp.repository.DataLoader.accountRepository;
import static habitsapp.repository.DataLoader.inputData;
import static org.assertj.core.api.Assertions.assertThat;

public class AccountRepositoryTest {

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
        accountRepository = new AccountRepository();
        inputData = new InputDataByConsole();
        accountRepository.loadUser(user);
        assertThat(accountRepository.isUserExists(user.getEmail())).isEqualTo(true);
        accountRepository.deleteOwnAccount(user.getEmail(), "UserPass");
        assertThat(accountRepository.isUserExists(user.getEmail())).isEqualTo(false);
    }

    @Test
    void shouldAddThenRemoveUserAndHabit() {
        accountRepository = new AccountRepository();
        inputData = new InputDataByConsole();
        accountRepository.loadUser(user);
        assertThat(accountRepository.getHabitsList(user.getEmail())).isEmpty();
        accountRepository.loadHabit(user.getEmail(), habit);
        assertThat(accountRepository.getHabitsList(user.getEmail())).isNotEmpty();
        accountRepository.deleteHabit(user.getEmail(), habit);
        assertThat(accountRepository.getHabitsList(user.getEmail())).isEmpty();
        accountRepository.deleteOwnAccount(user.getEmail(), "UserPass");
        assertThat(accountRepository.isUserExists(user.getEmail())).isEqualTo(false);
    }

    @Test
    void shouldUpdateHabitTitle() {
        accountRepository = new AccountRepository();
        inputData = new InputDataByConsole();
        accountRepository.loadUser(existingUser);
        accountRepository.loadHabit(existingUser.getEmail(), existingHabit);
        Habit changedHabit = existingHabit.clone();
        changedHabit.setTitle("NewTitle");
        assertThat(accountRepository.editHabit(existingUser.getEmail(), existingHabit, changedHabit)).isEqualTo(true);
    }

    @Test
    void shouldUpdateHabitDescription() {
        accountRepository = new AccountRepository();
        inputData = new InputDataByConsole();
        accountRepository.loadUser(existingUser);
        accountRepository.loadHabit(existingUser.getEmail(), existingHabit);
        Habit changedHabit = existingHabit.clone();
        changedHabit.setDescription("NewDescription");
        assertThat(accountRepository.editHabit(existingUser.getEmail(), existingHabit, changedHabit)).isEqualTo(true);
    }

    @Test
    void shouldUpdateHabitPeriod() {
        accountRepository = new AccountRepository();
        inputData = new InputDataByConsole();
        accountRepository.loadUser(existingUser);
        accountRepository.loadHabit(existingUser.getEmail(), existingHabit);
        Habit changedHabit = existingHabit.clone();
        changedHabit.setPeriod(7);
        assertThat(accountRepository.editHabit(existingUser.getEmail(), existingHabit, changedHabit)).isEqualTo(true);
    }

    @Test
    void shouldMarkHabitInCollection() {
        accountRepository = new AccountRepository();
        inputData = new InputDataByConsole();
        accountRepository.loadUser(existingUser);
        accountRepository.loadHabit(existingUser.getEmail(), existingHabit);
        assertThat(accountRepository.markHabitAsCompleted(existingUser.getEmail(), existingHabit)).isEqualTo(true);
        assertThat(accountRepository.markHabitAsCompleted(user.getEmail(), habit)).isEqualTo(false);
    }

    @Test
    void shouldAuthorizeUserAndThenReturnUserOrNull() {
        accountRepository = new AccountRepository();
        inputData = new InputDataByConsole();
        accountRepository.loadUser(existingUser);
        accountRepository.loadHabit(existingUser.getEmail(), existingHabit);
        assertThat(accountRepository.userAuth(existingUser.getEmail(), "WrongPass")).isNull();
        assertThat(accountRepository.userAuth(existingUser.getEmail(), "ExistingPass")).isNotNull();
    }

    @Test
    void shouldUpdateUserEmail() {
        accountRepository = new AccountRepository();
        inputData = new InputDataByConsole();
        accountRepository.loadUser(existingUser);
        accountRepository.loadHabit(existingUser.getEmail(), existingHabit);
        User changedUser = existingUser.clone();
        changedUser.setEmail("newemail@mail.ru");
        assertThat(accountRepository.isUserExists(existingUser.getEmail())).isEqualTo(true);
        assertThat(accountRepository.isUserExists(changedUser.getEmail())).isEqualTo(false);

        accountRepository.editUserData(existingUser.getEmail(), changedUser, "ExistingPass");
        assertThat(accountRepository.isUserExists(existingUser.getEmail())).isEqualTo(false);
        assertThat(accountRepository.isUserExists(changedUser.getEmail())).isEqualTo(true);

        accountRepository.editUserData(changedUser.getEmail(), existingUser, "ExistingPass");
        assertThat(accountRepository.isUserExists(existingUser.getEmail())).isEqualTo(true);
        assertThat(accountRepository.isUserExists(changedUser.getEmail())).isEqualTo(false);
    }

    @Test
    void shouldRemoveAndThenLoadUserToCollection() {
        accountRepository = new AccountRepository();
        inputData = new InputDataByConsole();
        accountRepository.loadUser(existingUser);
        accountRepository.loadHabit(existingUser.getEmail(), existingHabit);
        assertThat(accountRepository.isUserExists(existingUser.getEmail())).isEqualTo(true);
        accountRepository.deleteOwnAccount(existingUser.getEmail(), "ExistingPass");
        assertThat(accountRepository.isUserExists(existingUser.getEmail())).isEqualTo(false);
        accountRepository.loadUser(existingUser);
        assertThat(accountRepository.isUserExists(existingUser.getEmail())).isEqualTo(true);
    }

}
