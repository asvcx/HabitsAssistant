package habitsapp.console;

import habitsapp.data.DataController;
import habitsapp.models.Habit;
import habitsapp.models.User;

import java.io.Console;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

public class UIService {

    private static final Scanner scan = new Scanner(System.in);
    private static User currentProfile = null;
    private static Set<Habit> currentHabits = new TreeSet<Habit>();

    public static void startGuest() {
        String[] guestOptions = {"1. Вход", "2. Регистрация", "0. Закрыть программу"};
        while (currentProfile == null) {
            System.out.println("Выберите действие");
            Arrays.stream(guestOptions).forEach(System.out::println);

            switch (scan.nextLine()) {
                case "1": {
                    userEnter();
                    startUser();
                    break;
                }
                case "2": {
                    userRegistration();
                    break;
                }
                case "0": {
                    System.exit(0);
                    break;
                }
                default: {
                    System.out.println("Действие не выбрано");
                    break;
                }
            }
        }
    }

    private static void userEnter() {
        System.out.println("Введите электронную почту");
        String email = scan.nextLine();
        if (!DataController.userExists(email)) {
            System.out.printf("Пользователь с указанным email не найден (%s)%n", email);
            return;
        }
        System.out.println("Введите пароль");
        String password = scan.nextLine();
        User tempUser = DataController.userAuth(email, password);
        if (tempUser != null) {
            currentProfile = tempUser;
            currentHabits = DataController.getHabits(currentProfile.getEmail());
            System.out.printf("Вы вошли как %s%n", currentProfile.getName());
        } else {
            System.out.println("Неверный пароль");
        }
    }

    private static void userRegistration() {
        User tempUser = new User();
        System.out.println("Введите имя пользователя");
        tempUser.setName(scan.nextLine());
        if (tempUser.getName().length() < 4) {
            System.out.println("Имя должно содержать не менее 4 символов");
            return;
        }
        System.out.println("Введите электронную почту");
        tempUser.setEmail(scan.nextLine());
        if (!isValidEmail(tempUser.getEmail())) {
            System.out.println("Неверный формат почты");
            return;
        }
        if (DataController.userExists(tempUser.getEmail())) {
            System.out.println("Пользователь с указанным email уже существует");
            return;
        }
        Console console = System.console();
        String password;
        if (console != null) {
            char[] passwordArray = console.readPassword("Введите пароль: ");
            password = new String(passwordArray);
        } else {
            System.out.println("Введите пароль");
            password = scan.nextLine();
        }
        if (password.length() < 6) {
            System.out.println("Пароль должен содержать не менее 6 символов");
            return;
        }
        tempUser.setPassword(password);
        if (DataController.addUser(tempUser)) {
            System.out.println("Регистрация прошла успешно");
        } else {
            System.out.println("Не удалось зарегистрироваться");
        }
    }

    private static void startUser() {
        String[] userOptions = {"1. Привычки", "2. Профиль", "3. Выход из профиля", "0. Закрыть программу"};
        while (currentProfile != null) {
            System.out.println("Выберите действие");
            Arrays.stream(userOptions).forEach(System.out::println);

            switch (scan.nextLine()) {
                case "1": {
                    userHabits();
                    break;
                }
                case "2": {
                    userProfile();
                    break;
                }
                case "3": {
                    userExit();
                    break;
                }
                case "0": {
                    System.exit(0);
                    break;
                }
                default: {
                    System.out.println("Действие не выбрано");
                    break;
                }
            }
        }
        startGuest();
    }

    private static void userHabits() {
        String[] habitOptions = {
                "1. Вывести список привычек",
                "2. Показать историю",
                "3. Отметить выполнение",
                "4. Создать привычку",
                "5. Редактировать привычку",
                "6. Удалить привычку",
                "0. Назад"
        };
        while (true) {
            System.out.println("Выберите действие");
            Arrays.stream(habitOptions).forEach(System.out::println);
            switch (scan.nextLine()) {
                case "1": {
                    showHabits();
                    break;
                }
                case "2": {
                    showHistory();
                    break;
                }
                case "3": {
                    markCompletion();
                    break;
                }
                case "4": {
                    createHabit();
                    break;
                }
                case "5": {
                    editHabit();
                    break;
                }
                case "6": {
                    deleteHabit();
                    break;
                }
                case "0": {
                    return;
                }
                default: {
                    System.out.println("Действие не выбрано");
                    break;
                }
            }
            currentHabits = DataController.getHabits(currentProfile.getEmail());
        }
    }

    private static void showHabits() {
        List<String> filteredHabits = filterHabits();
        if (filteredHabits.isEmpty()) {
            System.out.println("Привычки не найдены");
        } else {
            List<String> numberedHabits = IntStream.range(0, filteredHabits.size())
                    .mapToObj(i -> (i+1) + ". " + filteredHabits.get(i))
                    .toList();
            numberedHabits.forEach(System.out::println);
        }
    }

    private static boolean isValidDate(String dateString) {
        String regex = "^\\d{1,2}-\\d{1,2}-\\d{2}$";
        Pattern pattern = Pattern.compile(regex);
        return pattern.matcher(dateString).matches();
    }

    private static List<String> filterHabits() {
        System.out.println("Выберите фильтр");
        String[] filterOptions = {
                "1. По названию",
                "2. По частоте",
                "3. По дате создания",
                "4. По проценту выполнения",
                "5. По текущей серии выполнения",
                "6. Вывести все",
                "0. Назад"
        };
        while (true) {
            System.out.println("Выберите действие");
            Arrays.stream(filterOptions).forEach(System.out::println);
            switch (scan.nextLine()) {
                case "1": {
                    System.out.println("Введите текст, содержащийся в названии");
                    String titleSubStr = scan.nextLine();

                    return currentHabits.stream()
                            .filter(h -> h.getTitle().contains(titleSubStr))
                            .map(Habit::toString)
                            .toList();
                }
                case "2": {
                    System.out.println("Введите частоту выполнения привычки");
                    while (!scan.hasNextInt()) {
                        System.out.println("Неверный ввод. Введите целое число.");
                        scan.nextLine();
                    }
                    int period = scan.nextInt();
                    scan.nextLine();
                    return currentHabits.stream()
                            .filter(h -> h.getPeriod() == period)
                            .map(Habit::toString)
                            .toList();
                }
                case "3": {
                    System.out.println("Введите дату создания в виде дд-мм-гг");
                    String dateInput = scan.nextLine();

                    if (!isValidDate(dateInput)) {
                        System.out.println("Неправильный формат даты");
                        return new LinkedList<>();
                    }
                    ZonedDateTime zonedDateTime;
                    try {
                        DateTimeFormatter utcFormatter = DateTimeFormatter.ofPattern("HH:mm, d-M-uu z", Locale.US);
                        zonedDateTime = ZonedDateTime.parse("12:00, " + dateInput + " UTC", utcFormatter);
                    } catch (DateTimeParseException e) {
                        System.out.println("Некорректная дата");
                        return new LinkedList<>();
                    }
                    Instant dateTime = zonedDateTime.toInstant();
                    return currentHabits.stream()
                            .filter(h -> {
                                long hoursDifference = Duration.between(h.getStartedDate(), dateTime).toHours();
                                return Math.abs(hoursDifference) < 13;
                            })
                            .map(v -> v + "[дата создания : " + v.getStartedDate() + "]")
                            .toList();
                }
                case "4": {
                    System.out.println("Введите минимальный процент");
                    while (!scan.hasNextInt()) {
                        System.out.println("Неверный ввод. Введите целое число в диапазоне 0-100");
                        scan.nextLine();
                    }
                    int minPercent = scan.nextInt();
                    scan.nextLine();
                    System.out.println("Введите максимальный процент");
                    while (!scan.hasNextInt()) {
                        System.out.println("Неверный ввод. Введите целое число в диапазоне 0-100");
                        scan.nextLine();
                    }
                    int maxPercent = scan.nextInt();
                    scan.nextLine();
                    return currentHabits.stream()
                            .filter(h -> h.getCompletionPercent() >= minPercent && h.getCompletionPercent() <= maxPercent)
                            .map(v -> v + "[процент выполнения : " + v.getCompletionPercent() + " %]")
                            .toList();
                }
                case "5": {
                    System.out.println("Введите минимальную серию выполнения");
                    while (!scan.hasNextInt()) {
                        System.out.println("Неверный ввод. Введите целое число");
                        scan.nextLine();
                    }
                    int minStreak = scan.nextInt();
                    scan.nextLine();
                    System.out.println("Введите максимальную серию выполнения");
                    while (!scan.hasNextInt()) {
                        System.out.println("Неверный ввод. Введите целое число");
                        scan.nextLine();
                    }
                    int maxStreak = scan.nextInt();
                    scan.nextLine();
                    return currentHabits.stream()
                            .filter(h -> h.getCurrentStreak() >= minStreak && h.getCurrentStreak() <= maxStreak)
                            .map(v -> v + "[серия выполнения : " + v.getCurrentStreak() + "]")
                            .toList();
                }
                case "6": {
                    return currentHabits.stream()
                            .map(Habit::toString)
                            .toList();
                }
                case "0": {
                    return new LinkedList<>();
                }
                default: {
                    System.out.println("Действие не выбрано");
                    break;
                }
            }
        }
    }

    private static void createHabit() {
        Habit tempHabit = new Habit();
        System.out.println("Введите название привычки");
        tempHabit.setTitle(scan.nextLine());
        if (tempHabit.getTitle().isEmpty()) {
            System.out.println("Недопустимое название");
            return;
        }
        System.out.println("Введите описание привычки");
        tempHabit.setDescription(scan.nextLine());
        System.out.println("Введите частоту в сутках");
        if (!scan.hasNextInt()) {
            System.out.println("Недопустимая частота");
            return;
        }
        tempHabit.setPeriod(scan.nextInt());
        scan.nextLine();
        if (DataController.addHabit(currentProfile.getEmail(), tempHabit)) {
            System.out.println("Привычка добавлена");
        } else {
            System.out.println("Не удалось добавить привычку");
        }
    }

    private static Optional<Habit> selectHabit() {
        System.out.println("Введите название привычки");
        String habitTitle = scan.nextLine();
        Optional<Habit> habit = currentHabits.stream()
                .filter(h -> h.getTitle().equals(habitTitle))
                .findFirst();
        habit.ifPresentOrElse(
                h -> System.out.printf("Вы выбрали привычку (%s)%n", h.getTitle()),
                () -> System.out.println("Привычка не найдена")
        );
        return habit;
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

    private static void markCompletion() {
        Optional<Habit> habitOpt = selectHabit();
        if (habitOpt.isEmpty()) {
            return;
        }
        Habit habit = habitOpt.get();
        boolean marked = DataController.markAsCompleted(currentProfile.getEmail(), habit);
        if(marked) {
            System.out.printf("Вы выполнили привычку %s%n",  habit.getTitle());
        } else {
            System.out.println("Не удалось записать выполнение привычки");
        }
    }

    private static void editHabit() {
        Optional<Habit> habitOpt = selectHabit();
        if (habitOpt.isEmpty()) {
            return;
        }
        Habit habit = habitOpt.get();
        Habit tempHabit = habit.clone();
        String[] editOptions = {
                "1. Название",
                "2. Описание",
                "3. Частоту",
                "0. Назад"
        };
        System.out.println("Что вы хотите изменить");
        Arrays.stream(editOptions).forEach(System.out::println);

        switch(scan.nextLine()) {
            case "1" : {
                System.out.println("Введите новое название");
                tempHabit.setTitle(scan.nextLine());
                if (tempHabit.getTitle().isEmpty()) {
                    System.out.println("Недопустимое название");
                    return;
                }
                break;
            }
            case "2" : {
                System.out.println("Введите новое описание привычки");
                tempHabit.setDescription(scan.nextLine());
                break;
            }
            case "3" : {
                System.out.println("Введите новую частоту в сутках (история будет сброшена)");
                if (!scan.hasNextInt()) {
                    System.out.println("Недопустимая частота");
                    return;
                }
                tempHabit.setPeriod(scan.nextInt());
                scan.nextLine();
                break;
            }
            default : {
                return;
            }
        }
        DataController.editHabit(currentProfile.getEmail(), habit, tempHabit);
        System.out.printf("Привычка (%s) успешно обновлена%n", tempHabit.getTitle());
    }

    private static void deleteHabit() {
        Optional<Habit> habitOpt = selectHabit();
        if (habitOpt.isEmpty()) {
            return;
        }
        Habit habit = habitOpt.get();
        DataController.deleteHabit(currentProfile.getEmail(), habit);
        System.out.printf("Привычка (%s) удалена%n", habit.getTitle());
    }

    private static void userProfile() {
        String[] profileOptions = {
                "1. Редактирование профиля",
                "2. Сброс пароля",
                "3. Удаление профиля",
                "0. Назад"
        };
        while (currentProfile != null) {
            System.out.println("Выберите действие");
            Arrays.stream(profileOptions).forEach(System.out::println);
            switch (scan.nextLine()) {
                case "1": {
                    editUserData();
                    break;
                }
                case "2": {
                    resetUserPassword();
                    break;
                }
                case "3": {
                    deleteUserProfile();
                    break;
                }
                case "0": {
                    return;
                }
                default: {
                    System.out.println("Действие не выбрано");
                    break;
                }
            }
        }
    }

    private static boolean isValidEmail(String email) {
        return email.matches("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$");
    }

    private static void editUserData() {
        User tempUser = currentProfile.clone();
        String[] editOptions = {
                "1. Имя пользователя",
                "2. Электронная почта",
                "3. Пароль",
                "0. Назад"
        };
        System.out.println("Что вы хотите изменить");
        Arrays.stream(editOptions).forEach(System.out::println);
        boolean success = false;
        switch(scan.nextLine()) {
            case "1" : {
                System.out.println("Введите новое имя");
                tempUser.setName(scan.nextLine());
                if (tempUser.getName().isEmpty()) {
                    System.out.println("Недопустимое имя");
                    return;
                }
                success = DataController.editUserData(currentProfile.getEmail(), tempUser);
                break;
            }
            case "2" : {
                System.out.println("Введите новую почту");
                tempUser.setEmail(scan.nextLine());
                if (!isValidEmail(tempUser.getEmail())) {
                    System.out.println("Неверный формат почты");
                    return;
                }
                success = DataController.editUserData(currentProfile.getEmail(), tempUser);
                break;
            }
            case "3" : {
                System.out.println("Введите старый пароль");
                String oldPassword = scan.nextLine();
                System.out.println("Введите новый пароль");
                String newPassword = scan.nextLine();
                if (newPassword.length() < 6) {
                    System.out.println("Пароль должен содержать не менее 6 символов");
                    return;
                }
                success = DataController.editUserPassword(currentProfile.getEmail(), oldPassword, newPassword);
                break;
            }
            case "0" : {

                break;
            }
            default : {
                System.out.println("Действие не выбрано");
                break;
            }
        }

        if (success) {
            System.out.println("Данные успешно изменены");
        } else {
            System.out.println("Не удалось изменить данные");
        }
    }

    private static void resetUserPassword() {

    }

    private static void deleteUserProfile() {
        System.out.println("Введите пароль, чтобы удалить профиль.");
        String password = scan.nextLine();
        boolean success = DataController.deleteUserProfile(currentProfile.getEmail(), password);
        if (success) {
            System.out.println("Профиль успешно удален.");
            userExit();
        } else {
            System.out.println("Не удалось удалить профиль. Неверный пароль.");
        }
    }

    private static void userExit() {
        currentProfile = null;
        currentHabits = null;
        System.out.println("Вы вышли из профиля.");
    }

}
