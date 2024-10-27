package habitsapp.server;

import habitsapp.client.in.UserInput;
import habitsapp.client.in.UserInputByConsole;
import habitsapp.models.Habit;
import habitsapp.models.User;
import habitsapp.server.repository.Repository;
import habitsapp.client.session.AuthorizationResult;
import habitsapp.server.service.HabitService;
import habitsapp.server.service.UserService;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.assertThat;

public class RepositoryTest {

    Repository repository = new Repository();
    private UserInput userInput = new UserInputByConsole();
    private UserService userService = new UserService(repository);
    private HabitService habitService = new HabitService(repository);

    private final User user = new User("Name", "name@mail.ru", "UserPass");
    private final Habit habit = new Habit("Title", "Description", 1);
    private final User existingUser = new User("ExistingName", "existing@mail.ru", "ExistingPass");
    private final Habit existingHabit = new Habit("ExistingTitle", "ExistingDescription", 1);

    @BeforeEach
    void setUp() {
        repository = new Repository();
        repository.loadUser(existingUser);
        repository.loadHabit(existingUser.getEmail(), existingHabit);
        repository.addToken("ExistingToken", existingUser);
    }

    @Test
    @DisplayName("Should add a user to the collection and then remove them")
    void shouldAddThenRemoveUserToCollection() {
        // Given
        assertThat(repository.isUserExists(existingUser.getEmail())).isTrue();
        // When
        repository.deleteUser(existingUser.getEmail(), "ExistingPass");
        // Then
        assertThat(repository.isUserExists(existingUser.getEmail())).isFalse();
    }

    @Test
    @DisplayName("Should add a user and habit, then remove")
    void shouldAddThenRemoveUserAndHabit() {
        // Given
        assertThat(repository.getHabitsOfUser(existingUser.getEmail())).isNotEmpty();
        // When
        //habitService.deleteHabit(existingUser.getEmail(),  existingHabit.getTitle());
        repository.deleteUser(existingUser.getEmail(), "ExistingPass");
        // Then
        assertThat(repository.getHabitsOfUser(existingUser.getEmail())).isEmpty();
        assertThat(repository.isUserExists(existingUser.getEmail())).isFalse();
    }

    @Test
    @DisplayName("Should update the habit's title")
    void shouldUpdateHabitTitle() {
        // When
        Habit changedHabit = existingHabit.clone();
        changedHabit.setTitle("NewTitle");
        // Then
        assertThat(habitService.editHabit(existingUser.getEmail(), "ExistingToken", existingHabit, changedHabit)).isTrue();
    }

    @Test
    @DisplayName("Should update the habit's description")
    void shouldUpdateHabitDescription() {
        // When
        Habit changedHabit = existingHabit.clone();
        changedHabit.setDescription("NewDescription");
        // Then
        assertThat(habitService.editHabit(existingUser.getEmail(), "ExistingToken", existingHabit, changedHabit)).isTrue();
    }

    @Test
    @DisplayName("Should update the habit's period")
    void shouldUpdateHabitPeriod() {
        // When
        Habit changedHabit = existingHabit.clone();
        changedHabit.setPeriod(7);
        // Then
        assertThat(habitService.editHabit(existingUser.getEmail(), "ExistingToken", existingHabit, changedHabit)).isTrue();
    }

    @Test
    @DisplayName("Should mark existing habit as completed")
    void shouldMarkHabitInCollection() {
        assertThat(habitService.markHabitAsCompleted(existingUser.getEmail(), "ExistingToken", existingHabit.getTitle())).isTrue();
        assertThat(habitService.markHabitAsCompleted(user.getEmail(), "ExistingToken", habit.getTitle())).isFalse();
    }

    @Test
    @DisplayName("Should authorize the user or return null if credentials are wrong")
    void shouldAuthorizeUserAndThenReturnUserOrNull() {
        AuthorizationResult correctAuthResult = userService.authorizeUser(existingUser.getEmail(), "ExistingPass");
        AuthorizationResult wrongAuthResult = userService.authorizeUser(existingUser.getEmail(), "WrongPass");
        assertThat(correctAuthResult.getSuccess()).isTrue();
        assertThat(wrongAuthResult.getSuccess()).isFalse();
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
        boolean result = userService.editUserData(existingUser.getEmail(), "ExistingToken", "Email", "newemail@mail.ru");
        assertThat(result).isTrue();
        // Then
        assertThat(repository.isUserExists(existingUser.getEmail())).isFalse();
        assertThat(repository.isUserExists(changedUser.getEmail())).isTrue();

        // When
        userService.editUserData(changedUser.getEmail(), "ExistingToken", "Email", "existing@mail.ru");
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
        repository.deleteUser(existingUser.getEmail(), "ExistingPass");
        // Then
        assertThat(repository.isUserExists(existingUser.getEmail())).isFalse();
        // When
        repository.loadUser(existingUser);
        // Then
        assertThat(repository.isUserExists(existingUser.getEmail())).isTrue();
    }

}
