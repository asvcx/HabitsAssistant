package habitsapp.out;

import habitsapp.models.Habit;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.TreeSet;
import java.util.stream.IntStream;

public class Info {

    public static void printHabits(List<String> habitsList) {
        if (habitsList.isEmpty()) {
            System.out.println("Привычки не найдены");
        } else {
            IntStream.range(0, habitsList.size())
                    .mapToObj(i -> (i + 1) + ". " + habitsList.get(i))
                    .forEach(System.out::println);
        }
    }

    public static void showHistory(Optional<Habit> habitOpt) {
        if (habitOpt.isEmpty()) {
            return;
        }
        Habit habit = habitOpt.get();
        System.out.println("История выполнения привычки \"" + habit.getTitle() + "\":");
        TreeSet<Instant> completionDates = habit.getCompletionDates();
        if (completionDates.isEmpty()) {
            System.out.println("Привычка пока не была выполнена.");
            return;
        }
        System.out.println("Даты выполнения: ");
        for (Instant date : completionDates) {
            System.out.println("- " + date);
        }
        System.out.println("Пропущенные дни: ");
        Instant lastCompletion = habit.getStartDate();
        for (Instant completion : completionDates) {
            long daysBetween = ChronoUnit.DAYS.between(lastCompletion, completion);
            for (long i = 1; i < daysBetween; i++) {
                Instant missedDate = lastCompletion.plus(i, ChronoUnit.DAYS);
                System.out.println("- " + missedDate);
            }
            lastCompletion = completion.plus(habit.getPeriod(), ChronoUnit.DAYS);
        }
        Instant now = Instant.now();
        long daysBetween = ChronoUnit.DAYS.between(lastCompletion, now);
        for (long i = 1; i <= daysBetween; i++) {
            Instant missedDate = lastCompletion.plus(i, ChronoUnit.DAYS);
            System.out.println("- " + missedDate);
        }
    }

}
