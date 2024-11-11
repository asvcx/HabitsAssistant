package org.habitsapp.server;

import org.example.HabitService;
import org.example.UserService;
import org.habitsapp.model.Habit;
import org.habitsapp.model.User;
import org.habitsapp.server.repository.AccountRepo;
import org.habitsapp.server.security.JwtService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.security.NoSuchAlgorithmException;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AccountRepositoryTest {

    @Autowired
    private AccountRepo repository;

    @Autowired
    private JwtService jwt;

    @Autowired
    private UserService userService;

    @Autowired
    private HabitService habitService;

    private final User user = new User("Name", "name@mail.ru", "UserPass");
    private final Habit habit = new Habit("Title", "Description", 1);
    private final User existingUser = new User("ExistingName", "existing@mail.ru", "ExistingPass");
    private final Habit existingHabit = new Habit("ExistingTitle", "ExistingDescription", 1);

    private String token;

    @BeforeEach
    void setUp() throws NoSuchAlgorithmException {
        repository.createUser(existingUser);
        repository.createHabit(existingUser.getId(), existingHabit);
        token = userService.createToken(existingUser.getId(), existingUser.getName(),
                existingUser.getEmail(), existingUser.getAccessLevel().name());
    }

    @AfterEach
    void tearDown() throws NoSuchAlgorithmException {
        repository.deleteHabit(existingUser.getId(), existingHabit.getTitle());
        repository.deleteUser(existingUser.getId());
    }

    @Test
    @DisplayName("Should add a user to the collection and then remove them")
    void shouldAddThenRemoveUserToCollection() {
        // Given
        assertThat(repository.isUserExists(existingUser.getEmail())).isTrue();
        // When
        repository.deleteUser(existingUser.getId());
        // Then
        assertThat(repository.isUserExists(existingUser.getEmail())).isFalse();
    }

    @Test
    @DisplayName("Should add a user and habit, then remove")
    void shouldAddThenRemoveUserAndHabit() {
        // Given
        assertThat(repository.getHabitsOfUser(existingUser.getId())).isNotEmpty();
        // When
        //habitService.deleteHabit(existingUser.getId(), token, existingHabit.getTitle());
        repository.deleteUser(existingUser.getId());
        // Then
        assertThat(repository.getHabitsOfUser(existingUser.getId())).isEmpty();
        assertThat(repository.isUserExists(existingUser.getId())).isFalse();
    }

    @Test
    @DisplayName("Should update the habit's title")
    void shouldUpdateHabitTitle() {
        // When
        Habit changedHabit = existingHabit.clone();
        changedHabit.setTitle("NewTitle");
        // Then
        assertThat(habitService.editHabit(existingUser.getId(), existingHabit.getTitle(),
                changedHabit.getTitle(), changedHabit.getDescription(), changedHabit.getPeriod())).isTrue();
    }

    @Test
    @DisplayName("Should update the habit's description")
    void shouldUpdateHabitDescription() {
        // When
        Habit changedHabit = existingHabit.clone();
        changedHabit.setDescription("NewDescription");
        // Then
        assertThat(habitService.editHabit(existingUser.getId(), existingHabit.getTitle(),
                changedHabit.getTitle(), changedHabit.getDescription(), changedHabit.getPeriod())).isTrue();
    }

    @Test
    @DisplayName("Should update the habit's period")
    void shouldUpdateHabitPeriod() {
        // When
        Habit changedHabit = existingHabit.clone();
        changedHabit.setPeriod(7);
        // Then
        assertThat(habitService.editHabit(existingUser.getId(), existingHabit.getTitle(),
                changedHabit.getTitle(), changedHabit.getDescription(), changedHabit.getPeriod())).isTrue();
    }

    @Test
    @DisplayName("Should mark existing habit as completed")
    void shouldMarkHabitInCollection() {
        assertThat(habitService.markHabitAsCompleted(existingUser.getId(), existingHabit.getTitle())).isTrue();
        assertThat(habitService.markHabitAsCompleted(user.getId(), habit.getTitle())).isFalse();
    }

    @Test
    @DisplayName("Should authorize the user or return null if credentials are wrong")
    void shouldAuthorizeUserAndThenReturnUserOrNull() {
        String correctAuthToken = userService.authorizeUser(existingUser.getEmail(), "ExistingPass");
        String wrongAuthToken = userService.authorizeUser(existingUser.getEmail(), "WrongPass");
        assertThat(correctAuthToken.isEmpty()).isFalse();
        assertThat(wrongAuthToken.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("Should update the user's email")
    void shouldUpdateUserEmail() {
        // Given
        String oldEmail = existingUser.getEmail();
        String newEmail = "changed@mail.ru";

        // Check initial state
        assertThat(repository.isUserExists(existingUser.getEmail())).as("Check that actual email exists").isTrue();
        assertThat(repository.isUserExists(newEmail)).isFalse().as("Check that new email does not exists yet");

        // When: Try to change email
        boolean isChanged = userService.editUserData(existingUser.getId(), newEmail, existingUser.getName());
        assertThat(isChanged).as("Check result of email change").isTrue();
        // Then: Check email change was successful
        assertThat(repository.isUserExists(oldEmail)).as("Check that old email no longer exists").isFalse();
        assertThat(repository.isUserExists(newEmail)).as("Check that new email exists").isTrue();

        // When
        boolean isUnchanged = userService.editUserData(existingUser.getId(), oldEmail, existingUser.getName());
        assertThat(isUnchanged).as("Check result of reverting to old email").isTrue();
        // Then
        assertThat(repository.isUserExists(oldEmail)).as("Check that old email exists again").isTrue();
        assertThat(repository.isUserExists(newEmail)).as("Check that new email no longer exists").isFalse();
    }

    @Test
    @DisplayName("Should remove a user and then load them to collection")
    void shouldRemoveAndThenLoadUserToCollection() {
        // Given
        assertThat(repository.isUserExists(existingUser.getEmail())).isTrue();
        // When
        repository.deleteUser(existingUser.getId());
        // Then
        assertThat(repository.isUserExists(existingUser.getEmail())).isFalse();
        // When
        repository.createUser(existingUser);
        // Then
        assertThat(repository.isUserExists(existingUser.getEmail())).isTrue();
    }

}
