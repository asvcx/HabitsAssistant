package habitsapp.console;

import habitsapp.models.Habit;
import habitsapp.session.Session;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static habitsapp.console.Input.*;

public class Menu {

    private static void displayMenu(Map<String, Runnable> options, Supplier<Boolean> continueCondition, Runnable loopAction) {
        while (continueCondition.get()) {
            System.out.println("Выберите действие:");
            options.forEach((msg, _) -> System.out.println(msg));
            String choice = scan.nextLine();
            if (choice.equals("0")) {
                return;
            }
            Optional<String> optionTitle = options.keySet().stream()
                    .filter(o -> o.startsWith(choice))
                    .findFirst();
            if (optionTitle.isPresent()) {
                options.get(optionTitle.get()).run();
            } else {
                System.out.println("Неверный выбор. Попробуйте снова.");
            }
            loopAction.run();
        }
    }

    private static void printHabits(List<String> habitsList) {
        if (habitsList.isEmpty()) {
            System.out.println("Привычки не найдены");
        } else {
            IntStream.range(0, habitsList.size())
                    .mapToObj(i -> (i + 1) + ". " + habitsList.get(i))
                    .forEach(System.out::println);
        }
    }

    public static void startGuestMenu() {
        Map<String, Runnable> options = new LinkedHashMap<>();
        options.put("1. Вход", () -> {
            DataRequest.userAuthorization();
            startUserMenu();
        });
        options.put("2. Регистрация", DataRequest::userRegistration);
        options.put("0. Закрыть программу", () -> System.exit(0));
        Supplier<Boolean> continueCondition = () -> !Session.isAuthorized();
        displayMenu(options, continueCondition, () -> {});
    }

    private static void startUserMenu() {
        Map<String, Runnable> options = new LinkedHashMap<>();
        options.put("1. Привычки", Menu::startUserHabitsMenu);
        options.put("2. Профиль", Menu::startUserProfileMenu);
        if (Session.isAdmin()) {
            options.put("3. Администрирование", Menu::startAdminActionsMenu);
        }
        options.put((1 + options.size()) + ". Выход из профиля", Session::exitFromProfile);
        options.put("0. Закрыть программу", () -> System.exit(0));

        Supplier<Boolean> continueCondition = Session::isAuthorized;
        displayMenu(options, continueCondition, () -> {});
    }

    private static void startUserHabitsMenu() {
        Map<String, Runnable> options = new LinkedHashMap<>();
        options.put("1. Вывести список привычек", Menu::startHabitsFilterMenu);
        options.put("2. Показать историю", Menu::showHistory);
        options.put("3. Отметить выполнение", DataRequest::markCompletion);
        options.put("4. Создать привычку", DataRequest::createHabit);
        options.put("5. Редактировать привычку", DataRequest::editHabit);
        options.put("6. Удалить привычку", DataRequest::deleteHabit);
        options.put("0. Назад", () -> {});

        Supplier<Boolean> continueCondition = Session::isAuthorized;
        displayMenu(options, continueCondition, () -> Session.setCurrentHabits(DataRequest.getHabits()));
    }

    private static void startUserProfileMenu() {
        Map<String, Runnable> profileOptions = new LinkedHashMap<>();
        profileOptions.put("1. Редактирование профиля", DataRequest::editUserData);
        profileOptions.put("2. Сброс пароля", DataRequest::resetUserPassword);
        profileOptions.put("3. Удаление профиля", DataRequest::deleteUserProfile);
        profileOptions.put("0. Назад", () -> {});
        Supplier<Boolean> continueCondition = Session::isAuthorized;
        displayMenu(profileOptions, continueCondition, () -> {});
    }

    private static void startHabitsFilterMenu() {
        System.out.println("Выберите фильтр");

        // Filters
        Function<String,Stream<String>> byName = (String titleSubStr) ->
                Session.getCurrentHabits().stream()
                .filter(h -> h.getTitle().contains(titleSubStr))
                .map(Habit::toString);
        Function<Integer,Stream<String>> byPeriod = (Integer period) ->
                Session.getCurrentHabits().stream()
                .filter(h -> h.getPeriod() == period)
                .map(Habit::toString);
        Function<Instant,Stream<String>> byDateCreation = (Instant dateTime) ->
                Session.getCurrentHabits().stream()
                .filter(h -> {
                    long hoursDifference = Duration.between(h.getStartedDate(), dateTime).toHours();
                    return Math.abs(hoursDifference) < 13;
                })
                .map(v -> v + "[дата создания : " + v.getStartedDate() + "]");
        BiFunction<Integer,Integer,Stream<String>> byCompletionPercent = (Integer minPercent, Integer maxPercent) ->
                Session.getCurrentHabits().stream()
                .filter(h -> h.getCompletionPercent() >= minPercent && h.getCompletionPercent() <= maxPercent)
                .map(v -> v + "[процент выполнения : " + v.getCompletionPercent() + " %]");
        BiFunction<Integer,Integer,Stream<String>> byCurrentStreak = (Integer minStreak, Integer maxStreak) ->
                Session.getCurrentHabits().stream()
                .filter(h -> h.getCurrentStreak() >= minStreak && h.getCurrentStreak() <= maxStreak)
                .map(v -> v + "[серия выполнения : " + v.getCurrentStreak() + "]");

        // Options
        Map<String, Runnable> options = new LinkedHashMap<>();
        options.put("1. По названию", () -> stringInput(
                    "Введите текст, содержащийся в названии",
                    Objects::nonNull,
                    s -> printHabits(byName.apply(s).toList()),
                    ""
        ));
        options.put("2. По частоте",  () -> {
            int period = intInput("Введите частоту выполнения привычки", 1, 365);
            printHabits(byPeriod.apply(period).toList());
        });
        options.put("3. По дате создания",  () -> {
            Instant dateTime = dateTimeInput();
            printHabits(byDateCreation.apply(dateTime).toList());
        });
        options.put("4. По проценту выполнения",  () -> {
            int minPercent = intInput("Введите минимальный процент.", 0, 100);
            int maxPercent = intInput("Введите максимальный процент.", 0, 100);
            printHabits(byCompletionPercent.apply(minPercent, maxPercent).toList());
        });
        options.put("5. По текущей серии выполнения",  () -> {
            int minStreak = intInput("Введите минимальную серию выполнения.", 0, 9999999);
            int maxStreak = intInput("Введите максимальную серию выполнения.", 0, 9999999);
            printHabits(byCurrentStreak.apply(minStreak, maxStreak).toList());
        });
        options.put("6. Вывести все",  () -> printHabits(Session.getCurrentHabits().stream().map(Habit::toString).toList()));
        options.put("0. Назад", () -> {});

        Supplier<Boolean> continueCondition = Session::isAuthorized;
        displayMenu(options, continueCondition, () -> {});
    }

    private static void startAdminActionsMenu() {
        System.out.println("Администрирование пользователей");
        Map<String, Runnable> options = new LinkedHashMap<>();
        options.put("1. Вывести список пользователей", () -> DataRequest.getProfilesList().forEach(System.out::println));
        options.put("2. Заблокировать пользователя", DataRequest::blockUserProfile);
        options.put("3. Разблокировать пользователя", DataRequest::unblockUserProfile);
        options.put("4. Удалить пользователя", DataRequest::removeUserProfile);
        options.put("0. Назад", () -> {});
        Supplier<Boolean> continueCondition = Session::isAuthorized;
        displayMenu(options, continueCondition, () -> {});
    }

    private static void showHistory() {
        Optional<Habit> habitOpt = selectHabit();
        if (habitOpt.isEmpty()) {
            return;
        }
        Habit habit = habitOpt.get();
        System.out.println("История выполнения привычки \"" + habit.getTitle() + "\":");
        TreeSet<Instant> completionDates = habit.getCompletionDate();
        if (completionDates.isEmpty()) {
            System.out.println("Привычка пока не была выполнена.");
            return;
        }
        System.out.println("Даты выполнения: ");
        for (Instant date : completionDates) {
            System.out.println("- " + date);
        }
        System.out.println("Пропущенные дни: ");
        Instant lastCompletion = habit.getStartedDate();
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
