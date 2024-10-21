package habitsapp.ui.in;

import habitsapp.data.repository.Repository;
import habitsapp.data.repository.DataLoader;
import habitsapp.data.models.Habit;
import habitsapp.data.models.User;
import habitsapp.ui.session.Session;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.Scanner;

import static habitsapp.ui.in.UserInputByConsole.*;
import static habitsapp.data.repository.DataLoader.repository;
import static habitsapp.data.repository.DataLoader.userInput;
import static org.assertj.core.api.Assertions.assertThat;

public class UserInputTest {

    @Test
    void shouldInputStringWhenConditionSatisfied() {
        repository = new Repository();
        userInput = new UserInputByConsole();
        Scanner mockScanner = Mockito.mock(Scanner.class);
        Mockito.when(mockScanner.nextLine())
                .thenReturn("valid")
                .thenReturn("not valid");
        ((UserInputByConsole) DataLoader.userInput).setCurrentScanner(mockScanner);

        Optional<String> s1 = DataLoader.userInput.stringInput("",
                s -> s.startsWith("valid"),
                (_) -> {},
                "");
        Optional<String> s2 = DataLoader.userInput.stringInput("",
                s -> s.startsWith("valid"),
                (_) -> {},
                "");
        assertThat(s1.isPresent()).isEqualTo(true);
        assertThat(s2.isPresent()).isEqualTo(false);
        ((UserInputByConsole) DataLoader.userInput).resetCurrentScanner();
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
        repository = new Repository();
        userInput = new UserInputByConsole();
        Habit habit = new Habit("HabitTitle", "HabitDescription", 1);
        User user = new User("Name", "name@mail.ru", "UserPass");
        repository.loadUser(user);
        Session.setCurrentProfile(user);
        repository.loadHabit(user.getEmail(), habit);
        Session.update();

        Scanner mockScanner = Mockito.mock(Scanner.class);
        Mockito.when(mockScanner.nextLine())
                .thenReturn("HabitTitle");
        ((UserInputByConsole) DataLoader.userInput).setCurrentScanner(mockScanner);
        assertThat(DataLoader.userInput.selectHabit().isPresent()).isEqualTo(true);

        repository.deleteHabit(user.getEmail(), habit.getTitle());
        Session.exitFromProfile();
        repository.deleteOwnAccount("admin@google.com", "AdminPass");
        ((UserInputByConsole) DataLoader.userInput).resetCurrentScanner();
    }

    @Test
    void shouldInputDateTimeAndReturnInstant() {
        repository = new Repository();
        userInput = new UserInputByConsole();
        Scanner mockScanner = Mockito.mock(Scanner.class);
        Mockito.when(mockScanner.nextLine())
                .thenReturn("14-07-97")
                .thenReturn("15-07-97")
                .thenReturn("29-05-99");
        ((UserInputByConsole) DataLoader.userInput).setCurrentScanner(mockScanner);
        Optional<Instant> t1 = DataLoader.userInput.dateTimeInput("Введите дату создания в виде дд-мм-гг.");
        Optional<Instant> t2 = DataLoader.userInput.dateTimeInput("Введите дату создания в виде дд-мм-гг.");
        Optional<Instant> t3 = DataLoader.userInput.dateTimeInput("Введите дату создания в виде дд-мм-гг.");

        assertThat (t1.isPresent() || t2.isPresent() || t3.isPresent()).isEqualTo(true);
        assertThat(Duration.between(t1.orElseThrow(), t2.orElseThrow()).toHours()).isBetween(23L, 25L);
        assertThat(Duration.between(t2.orElseThrow(), t3.orElseThrow()).toDays()).isBetween(365L, 365*2L);
        ((UserInputByConsole) DataLoader.userInput).resetCurrentScanner();
    }

}
