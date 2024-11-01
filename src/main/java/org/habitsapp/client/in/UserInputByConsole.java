package org.habitsapp.client.in;

import org.habitsapp.client.session.Session;
import org.habitsapp.models.Habit;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.Scanner;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class UserInputByConsole extends UserInput {

    private static final Scanner mainScanner = new Scanner(System.in);
    public static Scanner currentScanner = mainScanner;

    public void setCurrentScanner(Scanner scanner) {
        currentScanner = scanner;
    }
    public void resetCurrentScanner() {
        currentScanner = mainScanner;
    }

    public UserInputByConsole() {
        super();
    }

    public Optional<Integer> intInput(String inputMsg, int min, int max) {
        System.out.println(inputMsg);
        int result;
        if (!currentScanner.hasNextInt()) {
            System.out.printf("Неверный ввод. Введите целое число в диапазоне %d - %d%n", min, max);
            currentScanner.nextLine();
            return Optional.empty();
        }
        result = currentScanner.nextInt();
        currentScanner.nextLine();
        if (result < min || result > max) {
            System.out.printf("Неверный ввод. Введите целое число в диапазоне %d - %d%n", min, max);
            return Optional.empty();
        }
        return Optional.of(result);
    }

    public Optional<String> stringInput(String inputMsg, Predicate<String> condition, Consumer<String> action, String failMsg) {
        System.out.println(inputMsg);
        String input = currentScanner.nextLine();
        if (condition.test(input)) {
            action.accept(input);
            return Optional.of(input);
        } else {
            System.out.println(failMsg);
            return Optional.empty();
        }
    }

    public Optional<Instant> dateTimeInput(String inputMsg) {
        Optional<String> dateInput = stringInput(inputMsg,
                UserInputByConsole::isDateValid,
                (_) -> {},
                "Неверный ввод. Неправильный формат даты."
        );
        if (dateInput.isEmpty()) {
            return Optional.empty();
        }
        ZonedDateTime zonedDateTime;
        try {
            DateTimeFormatter utcFormatter = DateTimeFormatter.ofPattern("HH:mm, d-M-uu z");
            zonedDateTime = ZonedDateTime.parse("12:00, " + dateInput.get() + " GMT", utcFormatter);
        } catch (DateTimeParseException e) {
            System.out.println("Некорректная дата");
            return Optional.empty();
        }
        return Optional.ofNullable(zonedDateTime.toInstant());
    }

    public Optional<Habit> selectHabit() {
        System.out.println("Введите название привычки");
        String habitTitle = currentScanner.nextLine();
        Optional<Habit> habit = Session.getHabits().stream()
                .filter(h -> h.getTitle().equals(habitTitle))
                .findFirst();
        habit.ifPresentOrElse(
                h -> System.out.printf("Вы выбрали привычку (%s)%n", h.getTitle()),
                () -> System.out.println("Привычка не найдена")
        );
        return habit;
    }

    public Optional<String> enterUserName() {
        return stringInput(
                "Введите имя пользователя",
                UserInputByConsole::isNameValid,
                tempUser::setName,
                "Имя должно содержать не менее 4 символов"
        );
    }

    public Optional<String> enterUserEmail() {
        return stringInput(
                "Введите электронную почту",
                UserInputByConsole::isEmailValid,
                tempUser::setEmail,
                "Неверный формат почты"
        );
    }

    public Optional<String> enterUserPassword() {
        return stringInput(
                "Введите пароль",
                UserInputByConsole::isPasswordValid,
                tempUser::setPassword,
                "Неверный формат почты"
        );
    }

    public Optional<String> enterHabitTitle() {
        return stringInput("Введите название привычки",
                s -> !s.isEmpty(),
                tempHabit::setTitle,
                "Недопустимое название");
    }

    public Optional<String> enterHabitDescription() {
        return stringInput("Введите описание привычки",
                _ -> true,
                tempHabit::setDescription,
                "Недопустимое описание");
    }

    public Optional<Integer> enterHabitPeriod() {
        var period = intInput("Введите частоту в сутках", 1, 365);
        period.ifPresent(tempHabit::setPeriod);
        return period;
    }

}
