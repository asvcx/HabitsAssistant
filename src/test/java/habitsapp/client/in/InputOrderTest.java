package habitsapp.client.in;

import org.habitsapp.client.in.InputOrder;
import org.habitsapp.client.in.UserInput;
import org.habitsapp.client.in.UserInputByConsole;
import org.habitsapp.server.ApplicationContext;
import org.habitsapp.models.AccessLevel;
import org.habitsapp.server.repository.Repository;
import org.habitsapp.models.User;
import org.habitsapp.client.session.Session;
import org.habitsapp.server.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import java.util.Scanner;
import static org.assertj.core.api.Assertions.assertThat;

public class InputOrderTest {

    private Repository repository = ApplicationContext.getInstance().getRepository();
    private UserService userService = new UserService(repository);
    private UserInput userInput = new UserInputByConsole();
    private InputOrder inputOrder = new InputOrder();

    private final User admin = new User("Admin", "admin@google.com", "AdminPass");
    private final User user = new User("AffectedUser", "affected@mail.ru", "AffectedPass");

    @BeforeEach
    public void setUp() {
        repository = new Repository();
        admin.setAccessLevel(AccessLevel.ADMIN);
        Session.setToken("AdminToken");
        repository.loadUser(admin);
        repository.addToken("AdminToken", admin);
        Session.setProfile(admin);
    }

    @AfterEach
    public void tearDown() {
        ((UserInputByConsole) userInput).resetCurrentScanner();
        Session.logout();
    }

    @Test
    @DisplayName("User profile should be blocked and then unblocked by the admin")
    void shouldBlockAndUnblockUserProfileByAdmin() {
        // Given
        repository.loadUser(user);
        Session.setProfile(admin);

        Scanner mockScanner = Mockito.mock(Scanner.class);
        Mockito.when(mockScanner.nextLine()).thenReturn("affected@mail.ru");
        ((UserInputByConsole) userInput).setCurrentScanner(mockScanner);

        // When
        assertThat(user.isBlocked()).isFalse();
        inputOrder.operateProfile("заблокировать", Repository.ProfileAction.BLOCK);
        // Then
        assertThat(user.isBlocked()).isTrue();
        inputOrder.operateProfile("разблокировать", Repository.ProfileAction.UNBLOCK);
        assertThat(user.isBlocked()).isFalse();

        // When
        assertThat(repository.isUserExists(user.getEmail())).isTrue();
        inputOrder.operateProfile("удалить", Repository.ProfileAction.DELETE);
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
        inputOrder.userRegistration();
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
        inputOrder.userAuthorization();
        // Then
        assertThat(Session.getProfile().getEmail().equals(admin.getEmail())).isTrue();
    }

    @Test
    @DisplayName("Should return a list of users for admin's request and empty list for user's request")
    void shouldReturnProfileList() {
        // When
        Session.setProfile(admin);
        // Then
        assertThat(inputOrder.getProfilesList()).isNotEmpty();
        Session.logout();
        // When
        Session.setProfile(user);
        // Then
        assertThat(inputOrder.getProfilesList()).isEmpty();
    }

    @Test
    @DisplayName("A habit created by user should be added to repository")
    void shouldCreateHabit() {
        // Given
        Session.setProfile(admin);
        Scanner mockScanner = Mockito.mock(Scanner.class);

        Mockito.when(mockScanner.nextLine())
                .thenReturn("Медитация")
                .thenReturn("Для восстановления эмоционального баланса");
        Mockito.when(mockScanner.hasNextInt()).thenReturn(true);
        Mockito.when(mockScanner.nextInt()).thenReturn(7);
        ((UserInputByConsole) userInput).setCurrentScanner(mockScanner);

        // When
        int before = repository.getHabitsOfUser("admin@google.com").size();
        inputOrder.createHabit();
        int after = repository.getHabitsOfUser("admin@google.com").size();

        // Then
        assertThat(before == after - 1).isTrue();
    }

    @Test
    @DisplayName("When a user profile is deleted, it is marked as non-existent in the repository")
    void shouldDeleteOwnProfile() {
        // Given
        Scanner mockScanner = Mockito.mock(Scanner.class);
        Mockito.when(mockScanner.nextLine())
                .thenReturn("AdminPass");
        ((UserInputByConsole) userInput).setCurrentScanner(mockScanner);
        // When
        inputOrder.deleteOwnProfile();
        // Then
        assertThat(repository.isUserExists("admin@google.com")).isFalse();
    }

}
