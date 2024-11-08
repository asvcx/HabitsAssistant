package org.habitsapp.server;

import org.habitsapp.model.Habit;
import org.habitsapp.model.User;
import org.habitsapp.server.migration.DatabaseConfig;
import org.habitsapp.server.repository.AccountRepoImpl;
import org.habitsapp.model.result.AuthorizationResult;
import org.habitsapp.server.repository.DatabasePostgres;
import org.habitsapp.server.security.JwtService;
import org.habitsapp.server.service.HabitServiceImpl;
import org.habitsapp.server.service.UserServiceImpl;
import org.junit.jupiter.api.*;
import java.security.NoSuchAlgorithmException;

import static org.assertj.core.api.Assertions.assertThat;

public class AccountRepositoryTest {

    AccountRepoImpl repository;
    private UserServiceImpl userService;
    private HabitServiceImpl habitService;

    private final User user = new User("Name", "name@mail.ru", "UserPass");
    private final Habit habit = new Habit("Title", "Description", 1);
    private final User existingUser = new User("ExistingName", "existing@mail.ru", "ExistingPass");
    private final Habit existingHabit = new Habit("ExistingTitle", "ExistingDescription", 1);

    private String token;

    @BeforeEach
    void setUp() throws NoSuchAlgorithmException {
        repository = new AccountRepoImpl(new DatabasePostgres(new DatabaseConfig()));
        repository.createUser(existingUser);
        repository.createHabit(existingUser.getEmail(), existingHabit);
        JwtService jwt = new JwtService();
        userService = new UserServiceImpl(repository, jwt);
        habitService = new HabitServiceImpl(repository, jwt);
        token = userService.createToken(existingUser);
    }

    @AfterEach
    void tearDown() throws NoSuchAlgorithmException {
        repository = new AccountRepoImpl(new DatabasePostgres(new DatabaseConfig()));
        repository.deleteHabit(existingUser.getEmail(), existingHabit.getTitle());
        repository.deleteUser(existingUser.getEmail(), token);
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
        assertThat(correctAuthResult.success()).isTrue();
        assertThat(wrongAuthResult.success()).isFalse();
    }

    @Test
    @DisplayName("Should update the user's email")
    void shouldUpdateUserEmail() {
        // Given
        String oldEmail = existingUser.getEmail();
        String newEmail = "changed@mail.ru";
        assertThat(repository.isUserExists(existingUser.getEmail())).isTrue();
        assertThat(repository.isUserExists(newEmail)).isFalse();

        // When
        boolean isChanged = userService.editUserData(oldEmail, token, newEmail, existingUser.getName());
        assertThat(isChanged).isTrue();
        // Then
        assertThat(repository.isUserExists(oldEmail)).isFalse();
        assertThat(repository.isUserExists(newEmail)).isTrue();

        // When
        boolean isUnchanged = userService.editUserData(newEmail, token, oldEmail, existingUser.getName());
        assertThat(isUnchanged).isTrue();
        // Then
        assertThat(repository.isUserExists(oldEmail)).isTrue();
        assertThat(repository.isUserExists(newEmail)).isFalse();
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
        repository.createUser(existingUser);
        // Then
        assertThat(repository.isUserExists(existingUser.getEmail())).isTrue();
    }

}
