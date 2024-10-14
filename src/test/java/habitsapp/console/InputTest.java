package habitsapp.console;

import habitsapp.data.DataController;
import habitsapp.models.Habit;
import habitsapp.models.User;
import habitsapp.session.Session;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Duration;
import java.time.Instant;
import java.util.Scanner;

import static habitsapp.console.Input.*;
import static org.assertj.core.api.Assertions.assertThat;

public class InputTest {

    @Test
    void shouldInputStringWhenConditionSatisfied() {
        Scanner mockScanner = Mockito.mock(Scanner.class);
        Mockito.when(mockScanner.nextLine())
                .thenReturn("valid")
                .thenReturn("not valid");
        setCurrentScanner(mockScanner);

        String s1 = stringInput("",
                s -> s.startsWith("valid"),
                (_) -> {},
                "");
        String s2 = stringInput("",
                s -> s.startsWith("valid"),
                (_) -> {},
                "");
        assertThat(s1.isEmpty()).isEqualTo(false);
        assertThat(s2.isEmpty()).isEqualTo(true);
        resetCurrentScanner();
    }

    @Test
    void shouldCheckValidDate() {
        assertThat(isValidDate("15-10-23")).isEqualTo(true);
        assertThat(isValidDate("09-07-85")).isEqualTo(true);
        assertThat(isValidDate("31-08-19")).isEqualTo(true);
        assertThat(isValidDate("9-3-24")).isEqualTo(true);

        assertThat(isValidDate("")).isEqualTo(false);
        assertThat(isValidDate("5.8.3")).isEqualTo(false);
        assertThat(isValidDate("21 12 22")).isEqualTo(false);
        assertThat(isValidDate("ab-cd-hf")).isEqualTo(false);
    }

    @Test
    void shouldCheckValidEmail() {
        assertThat(isValidEmail("test@mail.ru")).isEqualTo(true);
        assertThat(isValidEmail("user.name@dommain.co.uk")).isEqualTo(true);
        assertThat(isValidEmail("jonh_doe@company.org")).isEqualTo(true);
        assertThat(isValidEmail("test123@example.io")).isEqualTo(true);
        assertThat(isValidEmail("email@subdomain.domain.com")).isEqualTo(true);

        assertThat(isValidEmail("@gmail.com")).isEqualTo(false);
        assertThat(isValidEmail("user@.org")).isEqualTo(false);
        assertThat(isValidEmail("user@domain")).isEqualTo(false);
        assertThat(isValidEmail("userdomain.com")).isEqualTo(false);
        assertThat(isValidEmail("email@.domain.com")).isEqualTo(false);
        assertThat(isValidEmail("email.@domain.com")).isEqualTo(false);
        assertThat(isValidEmail("email@domain..com")).isEqualTo(false);
    }

    @Test
    void shouldSelectHabit() {
        Habit habit = new Habit("HabitTitle", "HabitDescription", 1);
        User user = new User("Name", "name@mail.ru", "UserPass");
        DataController.addUser(user);
        Session.setCurrentProfile(user);
        DataController.addHabit(user.getEmail(), habit);
        Session.update();

        Scanner mockScanner = Mockito.mock(Scanner.class);
        Mockito.when(mockScanner.nextLine())
                .thenReturn("HabitTitle");
        setCurrentScanner(mockScanner);
        assertThat(selectHabit().isPresent()).isEqualTo(true);

        DataController.deleteHabit(user.getEmail(), habit);
        Session.exitFromProfile();
        DataController.deleteUserProfile("admin@google.com", "AdminPass");
        resetCurrentScanner();
    }

    @Test
    void shouldInputDateTimeAndReturnInstant() {
        Scanner mockScanner = Mockito.mock(Scanner.class);
        Mockito.when(mockScanner.nextLine())
                .thenReturn("14-7-97")
                .thenReturn("15-7-97")
                .thenReturn("31-05-99");
        setCurrentScanner(mockScanner);
        Instant t1 = dateTimeInput();
        Instant t2 = dateTimeInput();
        Instant t3 = dateTimeInput();

        assertThat(Duration.between(t1, t2).toHours()).isBetween(23L, 25L);
        assertThat(Duration.between(t2, t3).toDays()).isBetween(365L, 365*2L);
        resetCurrentScanner();
    }

}
