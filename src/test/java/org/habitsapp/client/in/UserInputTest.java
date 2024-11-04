package org.habitsapp.client.in;

import org.habitsapp.models.dto.UserDto;
import org.habitsapp.server.repository.Repository;
import org.habitsapp.models.Habit;
import org.habitsapp.models.User;
import org.habitsapp.client.session.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.Scanner;
import java.util.Set;

import static org.habitsapp.client.in.UserInputByConsole.*;
import static org.assertj.core.api.Assertions.assertThat;

public class UserInputTest {

    Repository repository = new Repository();
    private UserInput userInput = new UserInputByConsole();

    @BeforeEach
    void setUp() {
        repository = new Repository();
    }

    @Test
    @DisplayName("Should input string if condition is satisfied")
    void shouldInputStringWhenConditionSatisfied() {
        // Given a user is going to enter string
        Scanner mockScanner = Mockito.mock(Scanner.class);
        Mockito.when(mockScanner.nextLine())
                .thenReturn("valid")
                .thenReturn("not valid");
        ((UserInputByConsole) userInput).setCurrentScanner(mockScanner);
        // When a user inputs string with condition
        Optional<String> s1 = userInput.stringInput("",
                s -> s.startsWith("valid"),
                (_) -> {},
                "");
        Optional<String> s2 = userInput.stringInput("",
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
        assertThat(isDateValid("15-10-23")).isTrue();
        assertThat(isDateValid("09-07-85")).isTrue();
        assertThat(isDateValid("31-08-19")).isTrue();
        assertThat(isDateValid("9-3-24")).isTrue();

        assertThat(isDateValid("")).isFalse();
        assertThat(isDateValid("5.8.3")).isFalse();
        assertThat(isDateValid("21 12 22")).isFalse();
        assertThat(isDateValid("ab-cd-hf")).isFalse();
    }

    @Test
    @DisplayName("Should validate string with email")
    void shouldCheckValidEmail() {
        assertThat(isEmailValid("test@mail.ru")).isTrue();
        assertThat(isEmailValid("user.name@dommain.co.uk")).isTrue();
        assertThat(isEmailValid("jonh_doe@company.org")).isTrue();
        assertThat(isEmailValid("test123@example.io")).isTrue();
        assertThat(isEmailValid("email@subdomain.domain.com")).isTrue();

        assertThat(isEmailValid("@gmail.com")).isFalse();
        assertThat(isEmailValid("user@.org")).isFalse();
        assertThat(isEmailValid("user@domain")).isFalse();
        assertThat(isEmailValid("userdomain.com")).isFalse();
        assertThat(isEmailValid("email@.domain.com")).isFalse();
        assertThat(isEmailValid("email.@domain.com")).isFalse();
        assertThat(isEmailValid("email@domain..com")).isFalse();
    }

    @Test
    @DisplayName("Should select habit from user input")
    void shouldSelectHabit() {
        // Given a user is going to select an existing habit
        Habit habit = new Habit("HabitTitle", "HabitDescription", 1);
        User user = new User("Name", "name@mail.ru", "UserPass");
        Session.start(user, "UserToken");
        Session.setHabits(Set.of(habit));
        // Set up mock scanner for input habit title
        Scanner mockScanner = Mockito.mock(Scanner.class);
        Mockito.when(mockScanner.nextLine())
                .thenReturn("HabitTitle");
        ((UserInputByConsole) userInput).setCurrentScanner(mockScanner);

        // When habit tile entered
        Optional<Habit> habitOpt = userInput.selectHabit();

        // Then habit must be selected
        assertThat(habitOpt.isPresent()).isTrue();
    }

    @Test
    @DisplayName("Should get user input and convert it to Instant object")
    void shouldInputDateTimeAndReturnInstant() {
        // Set up mock scanner for input dates
        Scanner mockScanner = Mockito.mock(Scanner.class);
        Mockito.when(mockScanner.nextLine())
                .thenReturn("14-07-97")
                .thenReturn("15-07-97")
                .thenReturn("29-05-99");
        ((UserInputByConsole) userInput).setCurrentScanner(mockScanner);

        // When dates entered
        Optional<Instant> t1 = userInput.dateTimeInput("Введите дату создания в виде дд-мм-гг.");
        Optional<Instant> t2 = userInput.dateTimeInput("Введите дату создания в виде дд-мм-гг.");
        Optional<Instant> t3 = userInput.dateTimeInput("Введите дату создания в виде дд-мм-гг.");

        // Then they must be converted to Instant
        assertThat (t1.isPresent() || t2.isPresent() || t3.isPresent()).isTrue();
        assertThat(Duration.between(t1.orElseThrow(), t2.orElseThrow()).toHours()).isBetween(23L, 25L);
        assertThat(Duration.between(t2.orElseThrow(), t3.orElseThrow()).toDays()).isBetween(365L, 365*2L);
    }

}
