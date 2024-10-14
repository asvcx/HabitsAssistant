package habitsapp.console;

import habitsapp.data.DataController;
import habitsapp.models.User;
import habitsapp.session.Session;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Scanner;

import static habitsapp.console.Input.*;
import static org.assertj.core.api.Assertions.assertThat;

public class DataRequestTest {

    private final User admin = new User("Admin", "admin@google.com", "AdminPass");
    private final User affectedUser = new User("AffectedUser", "affected@mail.ru", "AffectedPass");

    @Test
    void shouldAffectUserProfile() {
        admin.setAccessLevel(User.AccessLevel.ADMIN);
        DataController.addUser(admin);
        DataController.addUser(affectedUser);
        Session.setCurrentProfile(admin);

        Scanner mockScanner = Mockito.mock(Scanner.class);
        Mockito.when(mockScanner.nextLine()).thenReturn("affected@mail.ru");
        setCurrentScanner(mockScanner);

        assertThat(affectedUser.isBlocked()).isEqualTo(false);
        DataRequest.operateProfile("заблокировать", DataController.ProfileAction.BLOCK);
        assertThat(affectedUser.isBlocked()).isEqualTo(true);
        DataRequest.operateProfile("разблокировать", DataController.ProfileAction.UNBLOCK);
        assertThat(affectedUser.isBlocked()).isEqualTo(false);
        DataRequest.operateProfile("удалить", DataController.ProfileAction.DELETE);
        assertThat(DataController.userExists(affectedUser.getEmail())).isEqualTo(false);

        resetCurrentScanner();
    }

    @Test
    void shouldRegisterUser() {
        Scanner mockScanner = Mockito.mock(Scanner.class);
        Mockito.when(mockScanner.nextLine())
                .thenReturn("NewUser")
                .thenReturn("newuser@mail.ru")
                .thenReturn("NewPass");
        setCurrentScanner(mockScanner);

        DataRequest.userRegistration();
        assertThat(DataController.userExists("newuser@mail.ru")).isEqualTo(true);
        DataController.deleteUserProfile("newuser@mail.ru", "NewPass");

        resetCurrentScanner();
    }

    @Test
    void shouldAuthorizeUser() {
        DataController.addUser(admin);
        Scanner mockScanner = Mockito.mock(Scanner.class);
        Mockito.when(mockScanner.nextLine())
                .thenReturn("admin@google.com")
                .thenReturn("AdminPass");
        setCurrentScanner(mockScanner);

        DataRequest.userAuthorization();
        assertThat(Session.getCurrentProfile().getEmail().equals(admin.getEmail())).isEqualTo(true);
        Session.exitFromProfile();

        DataController.deleteUserProfile("admin@google.com", "AdminPass");
        resetCurrentScanner();
    }

    @Test
    void shouldReturnProfileList() {
        DataController.addUser(admin);
        Session.setCurrentProfile(admin);
        assertThat(!DataRequest.getProfilesList().isEmpty()).isEqualTo(false);
        DataController.deleteUserProfile("admin@google.com", "AdminPass");
        Session.exitFromProfile();
    }

    @Test
    void shouldCreateHabit() {
        DataController.addUser(admin);
        Session.setCurrentProfile(admin);
        Scanner mockScanner = Mockito.mock(Scanner.class);

        Mockito.when(mockScanner.nextLine())
                .thenReturn("Медитация")
                .thenReturn("Для восстановления эмоционального баланса");
        Mockito.when(mockScanner.hasNextInt()).thenReturn(true);
        Mockito.when(mockScanner.nextInt()).thenReturn(7);
        setCurrentScanner(mockScanner);

        int before = DataController.getHabits("admin@google.com").size();
        DataRequest.createHabit();
        int after = DataController.getHabits("admin@google.com").size();
        assertThat(before == after - 1).isEqualTo(true);

        DataController.deleteUserProfile("admin@google.com", "AdminPass");
        Session.exitFromProfile();
        resetCurrentScanner();
    }

    @Test
    void shouldDeleteUserProfile() {
        DataController.addUser(admin);
        Session.setCurrentProfile(admin);
        Scanner mockScanner = Mockito.mock(Scanner.class);
        Mockito.when(mockScanner.nextLine())
                .thenReturn("AdminPass");
        setCurrentScanner(mockScanner);
        DataRequest.deleteUserProfile();
        assertThat(DataController.userExists("admin@google.com")).isEqualTo(false);
        Session.exitFromProfile();
        resetCurrentScanner();
    }

}
