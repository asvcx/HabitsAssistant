package habitsapp.in;

import habitsapp.repository.AccountRepository;
import habitsapp.repository.DataLoader;
import habitsapp.models.Habit;
import habitsapp.models.User;
import habitsapp.session.Session;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.Scanner;

import static habitsapp.in.InputDataByConsole.*;
import static habitsapp.repository.DataLoader.accountRepository;
import static habitsapp.repository.DataLoader.inputData;
import static org.assertj.core.api.Assertions.assertThat;

public class InputTest {

    @Test
    void shouldInputStringWhenConditionSatisfied() {
        accountRepository = new AccountRepository();
        inputData = new InputDataByConsole();
        Scanner mockScanner = Mockito.mock(Scanner.class);
        Mockito.when(mockScanner.nextLine())
                .thenReturn("valid")
                .thenReturn("not valid");
        ((InputDataByConsole) DataLoader.inputData).setCurrentScanner(mockScanner);

        Optional<String> s1 = DataLoader.inputData.stringInput("",
                s -> s.startsWith("valid"),
                (_) -> {},
                "");
        Optional<String> s2 = DataLoader.inputData.stringInput("",
                s -> s.startsWith("valid"),
                (_) -> {},
                "");
        assertThat(s1.isPresent()).isEqualTo(true);
        assertThat(s2.isPresent()).isEqualTo(false);
        ((InputDataByConsole) DataLoader.inputData).resetCurrentScanner();
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
        accountRepository = new AccountRepository();
        inputData = new InputDataByConsole();
        Habit habit = new Habit("HabitTitle", "HabitDescription", 1);
        User user = new User("Name", "name@mail.ru", "UserPass");
        accountRepository.loadUser(user);
        Session.setCurrentProfile(user);
        accountRepository.loadHabit(user.getEmail(), habit);
        Session.update();

        Scanner mockScanner = Mockito.mock(Scanner.class);
        Mockito.when(mockScanner.nextLine())
                .thenReturn("HabitTitle");
        ((InputDataByConsole) DataLoader.inputData).setCurrentScanner(mockScanner);
        assertThat(DataLoader.inputData.selectHabit().isPresent()).isEqualTo(true);

        accountRepository.deleteHabit(user.getEmail(), habit);
        Session.exitFromProfile();
        accountRepository.deleteOwnAccount("admin@google.com", "AdminPass");
        ((InputDataByConsole) DataLoader.inputData).resetCurrentScanner();
    }

    @Test
    void shouldInputDateTimeAndReturnInstant() {
        accountRepository = new AccountRepository();
        inputData = new InputDataByConsole();
        Scanner mockScanner = Mockito.mock(Scanner.class);
        Mockito.when(mockScanner.nextLine())
                .thenReturn("14-07-97")
                .thenReturn("15-07-97")
                .thenReturn("29-05-99");
        ((InputDataByConsole) DataLoader.inputData).setCurrentScanner(mockScanner);
        Optional<Instant> t1 = DataLoader.inputData.dateTimeInput("Введите дату создания в виде дд-мм-гг.");
        Optional<Instant> t2 = DataLoader.inputData.dateTimeInput("Введите дату создания в виде дд-мм-гг.");
        Optional<Instant> t3 = DataLoader.inputData.dateTimeInput("Введите дату создания в виде дд-мм-гг.");

        assertThat (t1.isPresent() || t2.isPresent() || t3.isPresent()).isEqualTo(true);
        assertThat(Duration.between(t1.orElseThrow(), t2.orElseThrow()).toHours()).isBetween(23L, 25L);
        assertThat(Duration.between(t2.orElseThrow(), t3.orElseThrow()).toDays()).isBetween(365L, 365*2L);
        ((InputDataByConsole) DataLoader.inputData).resetCurrentScanner();
    }

}
