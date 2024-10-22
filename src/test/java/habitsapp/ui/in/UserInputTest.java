package habitsapp.ui.in;

import habitsapp.data.repository.Repository;
import habitsapp.data.repository.DataLoader;
import habitsapp.data.models.Habit;
import habitsapp.data.models.User;
import habitsapp.ui.session.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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

    @BeforeEach
    void setUp() {
        repository = new Repository();
        userInput = new UserInputByConsole();
    }

    @Test
    @DisplayName("Should input string if condition is satisfied")
    void shouldInputStringWhenConditionSatisfied() {
        // Given a user is going to enter string
        Scanner mockScanner = Mockito.mock(Scanner.class);
        Mockito.when(mockScanner.nextLine())
                .thenReturn("valid")
                .thenReturn("not valid");
        ((UserInputByConsole) DataLoader.userInput).setCurrentScanner(mockScanner);
        // When a user inputs string with condition
        Optional<String> s1 = DataLoader.userInput.stringInput("",
                s -> s.startsWith("valid"),
                (_) -> {},
                "");
        Optional<String> s2 = DataLoader.userInput.stringInput("",
                s -> s.startsWith("valid"),
                (_) -> {},
                "");
        // Then string returns if condition satisfied
        assertThat(s1.isPresent()).isTrue();
        assertThat(s2.isPresent()).isFalse();
    }

    @Test
    @DisplayName("Should validate string with date")
    void shouldCheckValidDate() {
        assertThat(isValidDate("15-10-23")).isTrue();
        assertThat(isValidDate("09-07-85")).isTrue();
        assertThat(isValidDate("31-08-19")).isTrue();
        assertThat(isValidDate("9-3-24")).isTrue();

        assertThat(isValidDate("")).isFalse();
        assertThat(isValidDate("5.8.3")).isFalse();
        assertThat(isValidDate("21 12 22")).isFalse();
        assertThat(isValidDate("ab-cd-hf")).isFalse();
    }

    @Test
    @DisplayName("Should validate string with email")
    void shouldCheckValidEmail() {
        assertThat(isValidEmail("test@mail.ru")).isTrue();
        assertThat(isValidEmail("user.name@dommain.co.uk")).isTrue();
        assertThat(isValidEmail("jonh_doe@company.org")).isTrue();
        assertThat(isValidEmail("test123@example.io")).isTrue();
        assertThat(isValidEmail("email@subdomain.domain.com")).isTrue();

        assertThat(isValidEmail("@gmail.com")).isFalse();
        assertThat(isValidEmail("user@.org")).isFalse();
        assertThat(isValidEmail("user@domain")).isFalse();
        assertThat(isValidEmail("userdomain.com")).isFalse();
        assertThat(isValidEmail("email@.domain.com")).isFalse();
        assertThat(isValidEmail("email.@domain.com")).isFalse();
        assertThat(isValidEmail("email@domain..com")).isFalse();
    }

    @Test
    @DisplayName("Should select habit from user input")
    void shouldSelectHabit() {
        // Given a user is going to select an existing habit
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

        // When
        Optional<Habit> habitOpt = DataLoader.userInput.selectHabit();

        // Then
        assertThat(habitOpt.isPresent()).isTrue();
    }

    @Test
    @DisplayName("Should get user input and convert it to Instant object")
    void shouldInputDateTimeAndReturnInstant() {
        // Given
        Scanner mockScanner = Mockito.mock(Scanner.class);
        Mockito.when(mockScanner.nextLine())
                .thenReturn("14-07-97")
                .thenReturn("15-07-97")
                .thenReturn("29-05-99");
        ((UserInputByConsole) DataLoader.userInput).setCurrentScanner(mockScanner);

        // When
        Optional<Instant> t1 = DataLoader.userInput.dateTimeInput("Введите дату создания в виде дд-мм-гг.");
        Optional<Instant> t2 = DataLoader.userInput.dateTimeInput("Введите дату создания в виде дд-мм-гг.");
        Optional<Instant> t3 = DataLoader.userInput.dateTimeInput("Введите дату создания в виде дд-мм-гг.");

        // Then
        assertThat (t1.isPresent() || t2.isPresent() || t3.isPresent()).isTrue();
        assertThat(Duration.between(t1.orElseThrow(), t2.orElseThrow()).toHours()).isBetween(23L, 25L);
        assertThat(Duration.between(t2.orElseThrow(), t3.orElseThrow()).toDays()).isBetween(365L, 365*2L);
    }

}
