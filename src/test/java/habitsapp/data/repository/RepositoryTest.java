package habitsapp.data.repository;

import habitsapp.ui.in.UserInputByConsole;
import habitsapp.data.models.Habit;
import habitsapp.data.models.User;
import org.junit.jupiter.api.*;

import static habitsapp.data.repository.DataLoader.repository;
import static habitsapp.data.repository.DataLoader.userInput;
import static org.assertj.core.api.Assertions.assertThat;

public class RepositoryTest {

    private final User user = new User("Name", "name@mail.ru", "UserPass");
    private final Habit habit = new Habit("Title", "Description", 1);
    private final User existingUser = new User("ExistingName", "existing@mail.ru", "ExistingPass");
    private final Habit existingHabit = new Habit("ExistingTitle", "ExistingDescription", 1);

    @BeforeEach
    void setUp() {
        repository = new Repository();
        userInput = new UserInputByConsole();
        repository.loadUser(existingUser);
        repository.loadHabit(existingUser.getEmail(), existingHabit);
    }

    @Test
    @DisplayName("Should add a user to the collection and then remove them")
    void shouldAddThenRemoveUserToCollection() {
        // Given
        assertThat(repository.isUserExists(existingUser.getEmail())).isTrue();
        // When
        repository.deleteOwnAccount(existingUser.getEmail(), "ExistingPass");
        // Then
        assertThat(repository.isUserExists(existingUser.getEmail())).isFalse();
    }

    @Test
    @DisplayName("Should add a user and habit, then remove")
    void shouldAddThenRemoveUserAndHabit() {
        // Given
        assertThat(repository.getHabitsSet(existingUser.getEmail())).isNotEmpty();
        // When
        repository.deleteHabit(existingUser.getEmail(), existingHabit.getTitle());
        repository.deleteOwnAccount(existingUser.getEmail(), "ExistingPass");
        // Then
        assertThat(repository.getHabitsSet(existingUser.getEmail())).isEmpty();
        assertThat(repository.isUserExists(existingUser.getEmail())).isFalse();
    }

    @Test
    @DisplayName("Should update the habit's title")
    void shouldUpdateHabitTitle() {
        // When
        Habit changedHabit = existingHabit.clone();
        changedHabit.setTitle("NewTitle");
        // Then
        assertThat(repository.editHabit(existingUser.getEmail(), existingHabit, changedHabit)).isTrue();
    }

    @Test
    @DisplayName("Should update the habit's description")
    void shouldUpdateHabitDescription() {
        // When
        Habit changedHabit = existingHabit.clone();
        changedHabit.setDescription("NewDescription");
        // Then
        assertThat(repository.editHabit(existingUser.getEmail(), existingHabit, changedHabit)).isTrue();
    }

    @Test
    @DisplayName("Should update the habit's period")
    void shouldUpdateHabitPeriod() {
        // When
        Habit changedHabit = existingHabit.clone();
        changedHabit.setPeriod(7);
        // Then
        assertThat(repository.editHabit(existingUser.getEmail(), existingHabit, changedHabit)).isTrue();
    }

    @Test
    @DisplayName("Should mark existing habit as completed")
    void shouldMarkHabitInCollection() {
        assertThat(repository.markHabitAsCompleted(existingUser.getEmail(), existingHabit)).isTrue();
        assertThat(repository.markHabitAsCompleted(user.getEmail(), habit)).isFalse();
    }

    @Test
    @DisplayName("Should authorize the user or return null if credentials are wrong")
    void shouldAuthorizeUserAndThenReturnUserOrNull() {
        assertThat(repository.userAuth(existingUser.getEmail(), "WrongPass").isPresent()).isFalse();
        assertThat(repository.userAuth(existingUser.getEmail(), "ExistingPass").isPresent()).isTrue();
    }

    @Test
    @DisplayName("Should update the user's email")
    void shouldUpdateUserEmail() {
        // Given
        User changedUser = existingUser.clone();
        changedUser.setEmail("newemail@mail.ru");
        assertThat(repository.isUserExists(existingUser.getEmail())).isTrue();
        assertThat(repository.isUserExists(changedUser.getEmail())).isFalse();

        // When
        repository.editUserData(existingUser.getEmail(), changedUser, "ExistingPass");
        // Then
        assertThat(repository.isUserExists(existingUser.getEmail())).isFalse();
        assertThat(repository.isUserExists(changedUser.getEmail())).isTrue();

        // When
        repository.editUserData(changedUser.getEmail(), existingUser, "ExistingPass");
        // Then
        assertThat(repository.isUserExists(existingUser.getEmail())).isTrue();
        assertThat(repository.isUserExists(changedUser.getEmail())).isFalse();
    }

    @Test
    @DisplayName("Should remove a user and then load them to collection")
    void shouldRemoveAndThenLoadUserToCollection() {
        // Given
        assertThat(repository.isUserExists(existingUser.getEmail())).isTrue();
        // When
        repository.deleteOwnAccount(existingUser.getEmail(), "ExistingPass");
        // Then
        assertThat(repository.isUserExists(existingUser.getEmail())).isFalse();
        // When
        repository.loadUser(existingUser);
        // Then
        assertThat(repository.isUserExists(existingUser.getEmail())).isTrue();
    }

}
