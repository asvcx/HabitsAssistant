package habitsapp.console;

import habitsapp.models.Habit;
import habitsapp.session.Session;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.Optional;
import java.util.Scanner;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class Input {

    private static final Scanner mainScanner = new Scanner(System.in);
    public static Scanner currentScanner = mainScanner;

    public static void setCurrentScanner(Scanner scanner) {
        currentScanner = scanner;
    }

    public static void resetCurrentScanner() {
        currentScanner = mainScanner;
    }

    public static boolean isValidDate(String dateString) {
        return dateString.matches("^\\d{1,2}-\\d{1,2}-\\d{2}$");

    }

    public static boolean isValidEmail(String email) {
        return email.matches("^[\\w-]+(\\.[\\w-]+)*@[\\w-]+(\\.[\\w-]{2,})+$");
    }

    public static int intInput(String inputMsg, int min, int max) {
        System.out.println(inputMsg);
        int result;
        while (true) {
            if (!currentScanner.hasNextInt()) {
                System.out.printf("Неверный ввод. Введите целое число в диапазоне %d - %d%n", min, max);
                currentScanner.nextLine();
                continue;
            }
            result = currentScanner.nextInt();
            currentScanner.nextLine();

            if (result < min || result > max) {
                System.out.printf("Неверный ввод. Введите целое число в диапазоне %d - %d%n", min, max);
            } else {
                break;
            }
        }
        return result;
    }

    public static String stringInput(String inputMsg, Predicate<String> condition, Consumer<String> action, String failMsg) {
        System.out.println(inputMsg);
        String input = currentScanner.nextLine();
        if (condition.test(input)) {
            action.accept(input);
            return input;
        } else {
            System.out.println(failMsg);
            return "";
        }
    }

    public static Instant dateTimeInput() {
        String dateInput = stringInput("Введите дату создания в виде дд-мм-гг.",
                Input::isValidDate,
                (_) -> {},
                "Неверный ввод. Неправильный формат даты."
        );
        if (dateInput.isEmpty()) {
            return null;
        }
        ZonedDateTime zonedDateTime;
        try {
            DateTimeFormatter utcFormatter = DateTimeFormatter.ofPattern("HH:mm, d-M-uu z");
            zonedDateTime = ZonedDateTime.parse("12:00, " + dateInput + " GMT", utcFormatter);
        } catch (DateTimeParseException e) {
            System.out.println("Некорректная дата");
            return null;
        }
        return zonedDateTime.toInstant();
    }

    public static Optional<Habit> selectHabit() {
        System.out.println("Введите название привычки");
        String habitTitle = currentScanner.nextLine();
        Optional<Habit> habit = Session.getCurrentHabits().stream()
                .filter(h -> h.getTitle().equals(habitTitle))
                .findFirst();
        habit.ifPresentOrElse(
                h -> System.out.printf("Вы выбрали привычку (%s)%n", h.getTitle()),
                () -> System.out.println("Привычка не найдена")
        );
        return habit;
    }

}
