package habitsapp.ui.out;

import habitsapp.data.repository.DataLoader;
import habitsapp.ui.in.InputOrder;
import habitsapp.data.repository.Repository;
import habitsapp.data.models.Habit;
import habitsapp.ui.session.Session;

import java.time.*;
import java.util.*;
import java.util.function.*;

import static habitsapp.ui.in.UserInputByConsole.*;
import static habitsapp.data.repository.DataLoader.repository;

public class MenuConsole implements Menu {

    private void displayMenu(Map<String, Runnable> options, Supplier<Boolean> continueCondition, Runnable loopAction) {
        while (continueCondition.get()) {
            System.out.println("Выберите действие:");
            options.forEach((msg, _) -> System.out.println(msg));
            String choice = currentScanner.nextLine();
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

    public void startGuestMenu() {
        Map<String, Runnable> options = new LinkedHashMap<>();
        options.put("1. Вход", () -> {
            InputOrder.userAuthorization();
            startUserMenu();
        });
        options.put("2. Регистрация", InputOrder::userRegistration);
        options.put("0. Закрыть программу", () -> System.exit(0));
        Supplier<Boolean> continueCondition = () -> !Session.isAuthorized();
        displayMenu(options, continueCondition, () -> {});
    }

    private void startUserMenu() {
        Map<String, Runnable> options = new LinkedHashMap<>();
        options.put("1. Привычки", this::startUserHabitsMenu);
        options.put("2. Профиль", this::startUserProfileMenu);
        if (Session.isAdmin()) {
            options.put("3. Администрирование", this::startAdminActionsMenu);
        }
        options.put((1 + options.size()) + ". Выход из профиля", Session::exitFromProfile);
        options.put("0. Закрыть программу", () -> System.exit(0));

        Supplier<Boolean> continueCondition = Session::isAuthorized;
        displayMenu(options, continueCondition, () -> {});
    }

    private void startUserHabitsMenu() {
        Map<String, Runnable> options = new LinkedHashMap<>();
        options.put("1. Вывести список привычек", this::startHabitsFilterMenu);
        options.put("2. Показать историю", () -> Info.showHistory(DataLoader.userInput.selectHabit()));
        options.put("3. Отметить выполнение", InputOrder::markCompletion);
        options.put("4. Создать привычку", InputOrder::createHabit);
        options.put("5. Редактировать привычку", this::startEditHabitMenu);
        options.put("6. Удалить привычку", InputOrder::deleteHabit);
        options.put("0. Назад", () -> {});

        Supplier<Boolean> continueCondition = Session::isAuthorized;
        displayMenu(options, continueCondition, Session::update);
    }

    private void startUserProfileMenu() {
        Map<String, Runnable> profileOptions = new LinkedHashMap<>();
        profileOptions.put("1. Редактирование профиля", this::startEditUserDataMenu);
        profileOptions.put("2. Сброс пароля", InputOrder::resetUserPassword);
        profileOptions.put("3. Удаление профиля", InputOrder::deleteUserProfile);
        profileOptions.put("0. Назад", () -> {});
        Supplier<Boolean> continueCondition = Session::isAuthorized;
        displayMenu(profileOptions, continueCondition, () -> {});
    }

    private void startHabitsFilterMenu() {
        System.out.println("Выберите фильтр");

        Map<String, Runnable> options = new LinkedHashMap<>();
        options.put("1. По названию", () -> DataLoader.userInput.stringInput(
                    "Введите текст, содержащийся в названии",
                    Objects::nonNull,
                    s -> Info.printHabits(HabitFilter.byName(s).toList()),
                    ""
        ));
        options.put("2. По частоте",  () -> {
            Optional<Integer> period = DataLoader.userInput.intInput("Введите частоту выполнения привычки", 1, 365);
            period.ifPresent(integer -> Info.printHabits(HabitFilter.byPeriod(integer).toList()));
        });
        options.put("3. По дате создания",  () -> {
            Optional<Instant> dateTime = DataLoader.userInput.dateTimeInput("Введите дату создания в виде дд-мм-гг.");
            dateTime.ifPresent(instant -> Info.printHabits(HabitFilter.byDateCreation(instant).toList()));

        });
        options.put("4. По проценту выполнения",  () -> {
            Optional<Integer> minPercent = DataLoader.userInput.intInput("Введите минимальный процент.", 0, 100);
            Optional<Integer> maxPercent = DataLoader.userInput.intInput("Введите максимальный процент.", 0, 100);
            if (minPercent.isPresent() && maxPercent.isPresent()) {
                Info.printHabits(HabitFilter.byCompletionPercent(minPercent.get(), maxPercent.get()).toList());
            }
        });
        options.put("5. По текущей серии выполнения",  () -> {
            Optional<Integer> minStreak = DataLoader.userInput.intInput("Введите минимальную серию выполнения.", 0, 9999999);
            Optional<Integer> maxStreak = DataLoader.userInput.intInput("Введите максимальную серию выполнения.", 0, 9999999);
            if (minStreak.isPresent() && maxStreak.isPresent()) {
                Info.printHabits(HabitFilter.byCurrentStreak(minStreak.get(), maxStreak.get()).toList());
            }
        });
        options.put("6. Вывести все",  () -> Info.printHabits(Session.getCurrentHabits().stream().map(Habit::toString).toList()));
        options.put("0. Назад", () -> {});

        Supplier<Boolean> continueCondition = Session::isAuthorized;
        displayMenu(options, continueCondition, () -> {});
    }

    public void startEditUserDataMenu() {
        DataLoader.userInput.setTempUser(Session.getCurrentProfile().clone());
        System.out.println("Что вы хотите изменить");
        Map<String, Runnable> options = new LinkedHashMap<>();

        options.put("1. Имя пользователя", () -> {
            Optional<String> newName = DataLoader.userInput.enterUserName();
            Optional<String> password = DataLoader.userInput.enterUserPassword();
            if (repository.editUserData(Session.getCurrentEmail(), DataLoader.userInput.getTempUser(), password.get())) {
                Session.getCurrentProfile().setName(newName.get());
                System.out.println("Данные успешно изменены");
            } else {
                System.out.println("Не удалось изменить данные");
            };
        });
        options.put("2. Электронную почту",  () -> {
            Optional<String> newEmail = DataLoader.userInput.enterUserEmail();
            Optional<String> password = DataLoader.userInput.enterUserPassword();
            if (repository.editUserData(Session.getCurrentEmail(), DataLoader.userInput.getTempUser(), password.get())) {
                Session.getCurrentProfile().setName(newEmail.get());
                System.out.println("Данные успешно изменены");
            } else {
                System.out.println("Не удалось изменить данные");
            };
        });
        options.put("3. Пароль",  () -> {
            System.out.println("Введите старый пароль");
            String oldPassword = currentScanner.nextLine();
            Optional<String> newPassword = DataLoader.userInput.stringInput("Введите новый пароль",
                    s -> s.length() > 5,
                    _ -> {},
                    "Пароль должен содержать не менее 6 символов"
            );
            if (repository.editUserPassword(Session.getCurrentEmail(), oldPassword, newPassword.get())) {
                Session.getCurrentProfile().setPassword(newPassword.get());
            } else {
                System.out.println("Не удалось изменить данные");
            };
        });
        options.put("0. Назад", () -> {});

        Supplier<Boolean> continueCondition = Session::isAuthorized;
        displayMenu(options, continueCondition, () -> {});
    }

    public void startEditHabitMenu() {
        Optional<Habit> habitOpt = DataLoader.userInput.selectHabit();
        if (habitOpt.isEmpty()) {
            return;
        }
        Habit habit = habitOpt.get();
        DataLoader.userInput.setTempHabit(habit.clone());
        System.out.println("Что вы хотите изменить");
        Map<String, Runnable> options = new LinkedHashMap<>();

        options.put("1. Название", () -> {
            DataLoader.userInput.enterHabitTitle();
            if (repository.editHabit(Session.getCurrentEmail(), habit, DataLoader.userInput.getTempHabit())) {
                System.out.printf("Привычка (%s) успешно обновлена%n", DataLoader.userInput.getTempHabit().getTitle());
            };
        });
        options.put("2. Описание",  () -> {
            DataLoader.userInput.enterHabitDescription();
            if (repository.editHabit(Session.getCurrentEmail(), habit, DataLoader.userInput.getTempHabit())) {
                System.out.printf("Привычка (%s) успешно обновлена%n", DataLoader.userInput.getTempHabit().getTitle());
            };
        });
        options.put("3. Частоту",  () -> {
            DataLoader.userInput.enterHabitPeriod();
            if (repository.editHabit(Session.getCurrentEmail(), habit, DataLoader.userInput.getTempHabit())) {
                System.out.printf("Привычка (%s) успешно обновлена%n", DataLoader.userInput.getTempHabit().getTitle());
            };
        });
        options.put("0. Назад", () -> {});

        Supplier<Boolean> continueCondition = Session::isAuthorized;
        displayMenu(options, continueCondition, () -> {});
    }

    private void startAdminActionsMenu() {
        System.out.println("Администрирование пользователей");
        Map<String, Runnable> options = new LinkedHashMap<>();
        options.put("1. Вывести список пользователей", () -> InputOrder.getProfilesList().forEach(System.out::println));
        options.put("2. Заблокировать пользователя",
                () -> InputOrder.operateProfile("заблокировать", Repository.ProfileAction.BLOCK));
        options.put("3. Разблокировать пользователя",
                () -> InputOrder.operateProfile("разблокировать", Repository.ProfileAction.UNBLOCK));
        options.put("4. Удалить пользователя",
                () -> InputOrder.operateProfile("удалить", Repository.ProfileAction.DELETE));
        options.put("0. Назад", () -> {});
        Supplier<Boolean> continueCondition = Session::isAuthorized;
        displayMenu(options, continueCondition, () -> {});
    }


}
