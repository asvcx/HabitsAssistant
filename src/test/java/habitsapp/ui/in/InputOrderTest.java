package habitsapp.ui.in;

import habitsapp.data.repository.Repository;
import habitsapp.data.models.User;
import habitsapp.ui.session.Session;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Scanner;

import static habitsapp.data.repository.DataLoader.userInput;
import static habitsapp.data.repository.DataLoader.repository;
import static org.assertj.core.api.Assertions.assertThat;

public class InputOrderTest {

    private final User admin = new User("Admin", "admin@google.com", "AdminPass");
    private final User affectedUser = new User("AffectedUser", "affected@mail.ru", "AffectedPass");

    @Test
    @DisplayName("Профиль пользователя должен быть заблокирован, затем разблокирован администратором")
    void shouldAffectUserProfile() {
        // Given
        repository = new Repository();
        userInput = new UserInputByConsole();
        admin.setAccessLevel(User.AccessLevel.ADMIN);
        repository.loadUser(admin);
        repository.loadUser(affectedUser);
        Session.setCurrentProfile(admin);

        Scanner mockScanner = Mockito.mock(Scanner.class);
        Mockito.when(mockScanner.nextLine()).thenReturn("affected@mail.ru");
        ((UserInputByConsole) userInput).setCurrentScanner(mockScanner);

        // When
        assertThat(affectedUser.isBlocked()).isEqualTo(false);
        InputOrder.operateProfile("заблокировать", Repository.ProfileAction.BLOCK);
        // Then
        assertThat(affectedUser.isBlocked()).isEqualTo(true);
        InputOrder.operateProfile("разблокировать", Repository.ProfileAction.UNBLOCK);
        assertThat(affectedUser.isBlocked()).isEqualTo(false);

        // When
        assertThat(repository.isUserExists(affectedUser.getEmail())).isTrue();
        InputOrder.operateProfile("удалить", Repository.ProfileAction.DELETE);
        // Then
        assertThat(repository.isUserExists(affectedUser.getEmail())).isFalse();

        // Cleanup
        ((UserInputByConsole) userInput).resetCurrentScanner();
    }

    @Test
    @DisplayName("Пользователь должен быть добавлен в репозиторий при регистрации")
    void shouldRegisterUser() {
        repository = new Repository();
        userInput = new UserInputByConsole();
        Scanner mockScanner = Mockito.mock(Scanner.class);
        Mockito.when(mockScanner.nextLine())
                .thenReturn("NewUser")
                .thenReturn("newuser@mail.ru")
                .thenReturn("NewPass");
        ((UserInputByConsole) userInput).setCurrentScanner(mockScanner);

        InputOrder.userRegistration();
        assertThat(repository.isUserExists("newuser@mail.ru")).isEqualTo(true);
        repository.deleteOwnAccount("newuser@mail.ru", "NewPass");

        ((UserInputByConsole) userInput).resetCurrentScanner();
    }

    @Test
    @DisplayName("Пользователь должен авторизоваться при вводе корректных данных")
    void shouldAuthorizeUser() {
        repository = new Repository();
        userInput = new UserInputByConsole();
        repository.loadUser(admin);
        Scanner mockScanner = Mockito.mock(Scanner.class);
        Mockito.when(mockScanner.nextLine())
                .thenReturn("admin@google.com")
                .thenReturn("AdminPass");
        ((UserInputByConsole) userInput).setCurrentScanner(mockScanner);

        InputOrder.userAuthorization();
        assertThat(Session.getCurrentProfile().getEmail().equals(admin.getEmail())).isEqualTo(true);
        Session.exitFromProfile();

        repository.deleteOwnAccount("admin@google.com", "AdminPass");
        ((UserInputByConsole) userInput).resetCurrentScanner();
    }

    @Test
    void shouldReturnProfileList() {
        repository = new Repository();
        userInput = new UserInputByConsole();
        repository.loadUser(admin);
        Session.setCurrentProfile(admin);
        assertThat(InputOrder.getProfilesList().isEmpty()).isEqualTo(true);
        repository.deleteOwnAccount("admin@google.com", "AdminPass");
        Session.exitFromProfile();
    }

    @Test
    @DisplayName("Созданная пользователем привычка должна быть добавлена в репозиторий")
    void shouldCreateHabit() {
        // Given
        repository = new Repository();
        userInput = new UserInputByConsole();
        repository.loadUser(admin);
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
        assertThat(before == after - 1).isEqualTo(true);

        repository.deleteOwnAccount("admin@google.com", "AdminPass");
        Session.exitFromProfile();
        ((UserInputByConsole) userInput).resetCurrentScanner();
    }

    @Test
    @DisplayName("При удалении профиля пользователем, он считается несуществующим в репозитории")
    void shouldDeleteUserProfile() {
        repository = new Repository();
        userInput = new UserInputByConsole();
        repository.loadUser(admin);
        Session.setCurrentProfile(admin);
        Scanner mockScanner = Mockito.mock(Scanner.class);
        Mockito.when(mockScanner.nextLine())
                .thenReturn("AdminPass");
        ((UserInputByConsole) userInput).setCurrentScanner(mockScanner);
        InputOrder.deleteUserProfile();
        assertThat(repository.isUserExists("admin@google.com")).isEqualTo(false);
        Session.exitFromProfile();
        ((UserInputByConsole) userInput).resetCurrentScanner();
    }

}
