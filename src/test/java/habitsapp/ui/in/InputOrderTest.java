package habitsapp.ui.in;

import habitsapp.data.repository.Repository;
import habitsapp.data.models.User;
import habitsapp.ui.session.Session;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Scanner;

import static habitsapp.data.repository.DataLoader.userInput;
import static habitsapp.data.repository.DataLoader.repository;
import static org.assertj.core.api.Assertions.assertThat;

public class InputOrderTest {

    private final User admin = new User("Admin", "admin@google.com", "AdminPass");
    private final User user = new User("AffectedUser", "affected@mail.ru", "AffectedPass");

    @BeforeEach
    public void setUp() {
        repository = new Repository();
        userInput = new UserInputByConsole();
        admin.setAccessLevel(User.AccessLevel.ADMIN);
        repository.loadUser(admin);
        Session.setCurrentProfile(admin);
    }

    @AfterEach
    public void tearDown() {
        Session.exitFromProfile();
        ((UserInputByConsole) userInput).resetCurrentScanner();
        Session.exitFromProfile();
    }

    @Test
    @DisplayName("User profile should be blocked and then unblocked by the admin")
    void shouldBlockAndUnblockUserProfileByAdmin() {
        // Given
        repository.loadUser(user);
        Session.setCurrentProfile(admin);

        Scanner mockScanner = Mockito.mock(Scanner.class);
        Mockito.when(mockScanner.nextLine()).thenReturn("affected@mail.ru");
        ((UserInputByConsole) userInput).setCurrentScanner(mockScanner);

        // When
        assertThat(user.isBlocked()).isFalse();
        InputOrder.operateProfile("заблокировать", Repository.ProfileAction.BLOCK);
        // Then
        assertThat(user.isBlocked()).isTrue();
        InputOrder.operateProfile("разблокировать", Repository.ProfileAction.UNBLOCK);
        assertThat(user.isBlocked()).isFalse();

        // When
        assertThat(repository.isUserExists(user.getEmail())).isTrue();
        InputOrder.operateProfile("удалить", Repository.ProfileAction.DELETE);
        // Then
        assertThat(repository.isUserExists(user.getEmail())).isFalse();
    }

    @Test
    @DisplayName("User should be added to repository during registration")
    void shouldRegisterUser() {
        // Given
        Scanner mockScanner = Mockito.mock(Scanner.class);
        Mockito.when(mockScanner.nextLine())
                .thenReturn("NewUser")
                .thenReturn("newuser@mail.ru")
                .thenReturn("NewPass");
        ((UserInputByConsole) userInput).setCurrentScanner(mockScanner);
        // When
        InputOrder.userRegistration();
        //  Then
        assertThat(repository.isUserExists("newuser@mail.ru")).isTrue();
    }

    @Test
    @DisplayName("User should be authorized with correct credentials")
    void shouldAuthorizeUser() {
        // Given
        Scanner mockScanner = Mockito.mock(Scanner.class);
        Mockito.when(mockScanner.nextLine())
                .thenReturn("admin@google.com")
                .thenReturn("AdminPass");
        ((UserInputByConsole) userInput).setCurrentScanner(mockScanner);
        // When
        InputOrder.userAuthorization();
        // Then
        assertThat(Session.getCurrentProfile().getEmail().equals(admin.getEmail())).isTrue();
    }

    @Test
    @DisplayName("Should return a list of users for admin's request and empty list for user's request")
    void shouldReturnProfileList() {
        // When
        Session.setCurrentProfile(admin);
        // Then
        assertThat(InputOrder.getProfilesList()).isNotEmpty();
        Session.exitFromProfile();
        // When
        Session.setCurrentProfile(user);
        // Then
        assertThat(InputOrder.getProfilesList()).isEmpty();
    }

    @Test
    @DisplayName("A habit created by user should be added to repository")
    void shouldCreateHabit() {
        // Given
        Session.setCurrentProfile(admin);
        Scanner mockScanner = Mockito.mock(Scanner.class);

        Mockito.when(mockScanner.nextLine())
                .thenReturn("Медитация")
                .thenReturn("Для восстановления эмоционального баланса");
        Mockito.when(mockScanner.hasNextInt()).thenReturn(true);
        Mockito.when(mockScanner.nextInt()).thenReturn(7);
        ((UserInputByConsole) userInput).setCurrentScanner(mockScanner);

        // When
        int before = repository.getHabitsSet("admin@google.com").size();
        InputOrder.createHabit();
        int after = repository.getHabitsSet("admin@google.com").size();

        // Then
        assertThat(before == after - 1).isTrue();
    }

    @Test
    @DisplayName("When a user profile is deleted, it is marked as non-existent in the repository")
    void shouldDeleteUserProfile() {
        // Given
        Scanner mockScanner = Mockito.mock(Scanner.class);
        Mockito.when(mockScanner.nextLine())
                .thenReturn("AdminPass");
        ((UserInputByConsole) userInput).setCurrentScanner(mockScanner);
        // When
        InputOrder.deleteUserProfile();
        // Then
        assertThat(repository.isUserExists("admin@google.com")).isFalse();
    }

}
