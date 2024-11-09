package org.habitsapp.server;

import org.habitsapp.model.Habit;
import org.habitsapp.model.User;
import org.habitsapp.server.repository.AccountRepoImpl;
import org.habitsapp.model.result.AuthorizationResult;
import org.habitsapp.server.security.JwtService;
import org.habitsapp.server.service.HabitServiceImpl;
import org.habitsapp.server.service.UserServiceImpl;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.security.NoSuchAlgorithmException;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AccountRepositoryTest {

    @Autowired
    private AccountRepoImpl repository;

    @Autowired
    private JwtService jwt;

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private HabitServiceImpl habitService;

    private final User user = new User("Name", "name@mail.ru", "UserPass");
    private final Habit habit = new Habit("Title", "Description", 1);
    private final User existingUser = new User("ExistingName", "existing@mail.ru", "ExistingPass");
    private final Habit existingHabit = new Habit("ExistingTitle", "ExistingDescription", 1);

    private String token;

    @BeforeEach
    void setUp() throws NoSuchAlgorithmException {
        repository.createUser(existingUser);
        repository.createHabit(existingUser.getId(), existingHabit);
        token = userService.createToken(existingUser);
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
        assertThat(habitService.editHabit(existingUser.getId(), token, existingHabit, changedHabit)).isTrue();
    }

    @Test
    @DisplayName("Should update the habit's description")
    void shouldUpdateHabitDescription() {
        // When
        Habit changedHabit = existingHabit.clone();
        changedHabit.setDescription("NewDescription");
        // Then
        assertThat(habitService.editHabit(existingUser.getId(), token, existingHabit, changedHabit)).isTrue();
    }

    @Test
    @DisplayName("Should update the habit's period")
    void shouldUpdateHabitPeriod() {
        // When
        Habit changedHabit = existingHabit.clone();
        changedHabit.setPeriod(7);
        // Then
        assertThat(habitService.editHabit(existingUser.getId(), token, existingHabit, changedHabit)).isTrue();
    }

    @Test
    @DisplayName("Should mark existing habit as completed")
    void shouldMarkHabitInCollection() {
        assertThat(habitService.markHabitAsCompleted(existingUser.getId(), token, existingHabit.getTitle())).isTrue();
        assertThat(habitService.markHabitAsCompleted(user.getId(), token, habit.getTitle())).isFalse();
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
        boolean isChanged = userService.editUserData(existingUser.getId(), token, newEmail, existingUser.getName());
        assertThat(isChanged).isTrue();
        // Then
        assertThat(repository.isUserExists(oldEmail)).isFalse();
        assertThat(repository.isUserExists(newEmail)).isTrue();

        // When
        boolean isUnchanged = userService.editUserData(existingUser.getId(), token, oldEmail, existingUser.getName());
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
        repository.deleteUser(existingUser.getId());
        // Then
        assertThat(repository.isUserExists(existingUser.getEmail())).isFalse();
        // When
        repository.createUser(existingUser);
        // Then
        assertThat(repository.isUserExists(existingUser.getEmail())).isTrue();
    }

}
