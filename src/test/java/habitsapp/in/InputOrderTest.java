package habitsapp.in;

import habitsapp.repository.AccountRepository;
import habitsapp.models.User;
import habitsapp.session.Session;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Scanner;

import static habitsapp.repository.DataLoader.inputData;
import static habitsapp.repository.DataLoader.accountRepository;
import static org.assertj.core.api.Assertions.assertThat;

public class InputOrderTest {

    private final User admin = new User("Admin", "admin@google.com", "AdminPass");
    private final User affectedUser = new User("AffectedUser", "affected@mail.ru", "AffectedPass");

    @Test
    void shouldAffectUserProfile() {
        // Given
        accountRepository = new AccountRepository();
        inputData = new InputDataByConsole();
        admin.setAccessLevel(User.AccessLevel.ADMIN);
        accountRepository.loadUser(admin);
        accountRepository.loadUser(affectedUser);
        Session.setCurrentProfile(admin);

        Scanner mockScanner = Mockito.mock(Scanner.class);
        Mockito.when(mockScanner.nextLine()).thenReturn("affected@mail.ru");
        ((InputDataByConsole) inputData).setCurrentScanner(mockScanner);

        // When
        assertThat(affectedUser.isBlocked()).isEqualTo(false);
        InputOrder.operateProfile("заблокировать", AccountRepository.ProfileAction.BLOCK);
        // Then
        assertThat(affectedUser.isBlocked()).isEqualTo(true);
        InputOrder.operateProfile("разблокировать", AccountRepository.ProfileAction.UNBLOCK);
        assertThat(affectedUser.isBlocked()).isEqualTo(false);

        // When
        assertThat(accountRepository.isUserExists(affectedUser.getEmail())).isTrue();
        InputOrder.operateProfile("удалить", AccountRepository.ProfileAction.DELETE);
        // Then
        assertThat(accountRepository.isUserExists(affectedUser.getEmail())).isFalse();

        // Cleanup
        ((InputDataByConsole) inputData).resetCurrentScanner();
    }

    @Test
    void shouldRegisterUser() {
        accountRepository = new AccountRepository();
        inputData = new InputDataByConsole();
        Scanner mockScanner = Mockito.mock(Scanner.class);
        Mockito.when(mockScanner.nextLine())
                .thenReturn("NewUser")
                .thenReturn("newuser@mail.ru")
                .thenReturn("NewPass");
        ((InputDataByConsole) inputData).setCurrentScanner(mockScanner);

        InputOrder.userRegistration();
        assertThat(accountRepository.isUserExists("newuser@mail.ru")).isEqualTo(true);
        accountRepository.deleteOwnAccount("newuser@mail.ru", "NewPass");

        ((InputDataByConsole) inputData).resetCurrentScanner();
    }

    @Test
    void shouldAuthorizeUser() {
        accountRepository = new AccountRepository();
        inputData = new InputDataByConsole();
        accountRepository.loadUser(admin);
        Scanner mockScanner = Mockito.mock(Scanner.class);
        Mockito.when(mockScanner.nextLine())
                .thenReturn("admin@google.com")
                .thenReturn("AdminPass");
        ((InputDataByConsole) inputData).setCurrentScanner(mockScanner);

        InputOrder.userAuthorization();
        assertThat(Session.getCurrentProfile().getEmail().equals(admin.getEmail())).isEqualTo(true);
        Session.exitFromProfile();

        accountRepository.deleteOwnAccount("admin@google.com", "AdminPass");
        ((InputDataByConsole) inputData).resetCurrentScanner();
    }

    @Test
    void shouldReturnProfileList() {
        accountRepository = new AccountRepository();
        inputData = new InputDataByConsole();
        accountRepository.loadUser(admin);
        Session.setCurrentProfile(admin);
        assertThat(!InputOrder.getProfilesList().isEmpty()).isEqualTo(false);
        accountRepository.deleteOwnAccount("admin@google.com", "AdminPass");
        Session.exitFromProfile();
    }

    @Test
    void shouldCreateHabit() {
        accountRepository = new AccountRepository();
        inputData = new InputDataByConsole();
        accountRepository.loadUser(admin);
        Session.setCurrentProfile(admin);
        Scanner mockScanner = Mockito.mock(Scanner.class);

        Mockito.when(mockScanner.nextLine())
                .thenReturn("Медитация")
                .thenReturn("Для восстановления эмоционального баланса");
        Mockito.when(mockScanner.hasNextInt()).thenReturn(true);
        Mockito.when(mockScanner.nextInt()).thenReturn(7);
        ((InputDataByConsole) inputData).setCurrentScanner(mockScanner);

        int before = accountRepository.getHabitsList("admin@google.com").size();
        InputOrder.createHabit();
        int after = accountRepository.getHabitsList("admin@google.com").size();
        assertThat(before == after - 1).isEqualTo(true);

        accountRepository.deleteOwnAccount("admin@google.com", "AdminPass");
        Session.exitFromProfile();
        ((InputDataByConsole) inputData).resetCurrentScanner();
    }

    @Test
    void shouldDeleteUserProfile() {
        accountRepository = new AccountRepository();
        inputData = new InputDataByConsole();
        accountRepository.loadUser(admin);
        Session.setCurrentProfile(admin);
        Scanner mockScanner = Mockito.mock(Scanner.class);
        Mockito.when(mockScanner.nextLine())
                .thenReturn("AdminPass");
        ((InputDataByConsole) inputData).setCurrentScanner(mockScanner);
        InputOrder.deleteUserProfile();
        assertThat(accountRepository.isUserExists("admin@google.com")).isEqualTo(false);
        Session.exitFromProfile();
        ((InputDataByConsole) inputData).resetCurrentScanner();
    }

}
