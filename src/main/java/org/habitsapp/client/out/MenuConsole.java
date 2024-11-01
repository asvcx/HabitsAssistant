package org.habitsapp.client.out;

import org.habitsapp.client.in.UserInput;
import org.habitsapp.client.session.Request;
import org.habitsapp.client.session.Session;
import org.habitsapp.client.in.UserInputByConsole;
import org.habitsapp.models.Habit;
import org.habitsapp.client.in.InputOrder;
import org.habitsapp.models.dto.HabitDto;
import org.habitsapp.models.dto.HabitMapper;
import org.habitsapp.server.repository.AccountRepository;

import java.time.*;
import java.util.*;
import java.util.function.*;

import static org.habitsapp.client.in.UserInputByConsole.*;

public class MenuConsole implements Menu {

    private UserInput userInput = new UserInputByConsole();
    private InputOrder inputOrder = new InputOrder();

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
            inputOrder.userAuthorization();
            startUserMenu();
        });
        options.put("2. Регистрация", inputOrder::userRegistration);
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
        options.put((1 + options.size()) + ". Выход из профиля", Session::logout);
        options.put("0. Закрыть программу", () -> System.exit(0));

        Supplier<Boolean> continueCondition = Session::isAuthorized;
        displayMenu(options, continueCondition, () -> {});
    }

    private void startUserHabitsMenu() {
        Map<String, Runnable> options = new LinkedHashMap<>();
        options.put("1. Вывести список привычек", this::startHabitsFilterMenu);
        options.put("2. Показать историю", () -> Info.showHistory(userInput.selectHabit()));
        options.put("3. Отметить выполнение", inputOrder::markCompletion);
        options.put("4. Создать привычку", inputOrder::createHabit);
        options.put("5. Редактировать привычку", this::startEditHabitMenu);
        options.put("6. Удалить привычку", inputOrder::deleteHabit);
        options.put("0. Назад", () -> {});

        Supplier<Boolean> continueCondition = Session::isAuthorized;
        displayMenu(options, continueCondition, Session::update);
    }

    private void startUserProfileMenu() {
        Map<String, Runnable> profileOptions = new LinkedHashMap<>();
        profileOptions.put("1. Редактирование профиля", this::startEditUserDataMenu);
        profileOptions.put("2. Сброс пароля", inputOrder::resetUserPassword);
        profileOptions.put("3. Удаление профиля", inputOrder::deleteOwnProfile);
        profileOptions.put("0. Назад", () -> {});
        Supplier<Boolean> continueCondition = Session::isAuthorized;
        displayMenu(profileOptions, continueCondition, () -> {});
    }

    private void startHabitsFilterMenu() {
        System.out.println("Выберите фильтр");

        Map<String, Runnable> options = new LinkedHashMap<>();
        options.put("1. По названию", () -> userInput.stringInput(
                    "Введите текст, содержащийся в названии",
                    Objects::nonNull,
                    s -> Info.printHabits(HabitFilter.byName(s).toList()),
                    ""
        ));
        options.put("2. По частоте",  () -> {
            Optional<Integer> period = userInput.intInput("Введите частоту выполнения привычки", 1, 365);
            period.ifPresent(integer -> Info.printHabits(HabitFilter.byPeriod(integer).toList()));
        });
        options.put("3. По дате создания",  () -> {
            Optional<Instant> dateTime = userInput.dateTimeInput("Введите дату создания в виде дд-мм-гг.");
            dateTime.ifPresent(instant -> Info.printHabits(HabitFilter.byDateCreation(instant).toList()));

        });
        options.put("4. По проценту выполнения",  () -> {
            Optional<Integer> minPercent = userInput.intInput("Введите минимальный процент.", 0, 100);
            Optional<Integer> maxPercent = userInput.intInput("Введите максимальный процент.", 0, 100);
            if (minPercent.isPresent() && maxPercent.isPresent()) {
                Info.printHabits(HabitFilter.byCompletionPercent(minPercent.get(), maxPercent.get()).toList());
            }
        });
        options.put("5. По текущей серии выполнения",  () -> {
            Optional<Integer> minStreak = userInput.intInput("Введите минимальную серию выполнения.", 0, 9999999);
            Optional<Integer> maxStreak = userInput.intInput("Введите максимальную серию выполнения.", 0, 9999999);
            if (minStreak.isPresent() && maxStreak.isPresent()) {
                Info.printHabits(HabitFilter.byCurrentStreak(minStreak.get(), maxStreak.get()).toList());
            }
        });
        options.put("6. Вывести все",  () -> Info.printHabits(Session.getHabits().stream().map(Habit::toString).toList()));
        options.put("0. Назад", () -> {});

        Supplier<Boolean> continueCondition = Session::isAuthorized;
        displayMenu(options, continueCondition, () -> {});
    }

    public void startEditUserDataMenu() {
        userInput.setTempUser(Session.getProfile().clone());
        System.out.println("Что вы хотите изменить");
        Map<String, Runnable> options = new LinkedHashMap<>();
        Request request = new Request();

        options.put("1. Имя пользователя", () -> {
            Optional<String> newName = userInput.enterUserName();
            Optional<String> password = userInput.enterUserPassword();
            if (newName.isPresent() &&
                    request.editUserData(Session.getEmail(), Session.getToken(), newName.get(), Session.getEmail())
            ) {
                Session.getProfile().setName(newName.get());
                System.out.println("Данные успешно изменены");
            } else {
                System.out.println("Не удалось изменить данные");
            };
        });
        options.put("2. Электронную почту",  () -> {
            Optional<String> newEmail = userInput.enterUserEmail();
            Optional<String> password = userInput.enterUserPassword();
            if (newEmail.isPresent() &&
                    request.editUserData(Session.getEmail(), Session.getToken(), Session.getName(), newEmail.get())
            ) {
                Session.getProfile().setName(newEmail.get());
                System.out.println("Данные успешно изменены");
            } else {
                System.out.println("Не удалось изменить данные");
            };
        });
        options.put("3. Пароль",  () -> {
            System.out.println("Введите старый пароль");
            String oldPassword = currentScanner.nextLine();
            Optional<String> newPassword = userInput.stringInput("Введите новый пароль",
                    s -> s.length() > 5,
                    _ -> {},
                    "Пароль должен содержать не менее 6 символов"
            );
            if (newPassword.isPresent()) {
                if (request.editUserPassword(Session.getEmail(), Session.getToken(), oldPassword, newPassword.get())) {
                    Session.getProfile().setPassword(newPassword.get());
                } else {
                    System.out.println("Не удалось изменить данные");
                }
            } else {
                System.out.println("Недопустимый пароль");
            }
        });
        options.put("0. Назад", () -> {});

        Supplier<Boolean> continueCondition = Session::isAuthorized;
        displayMenu(options, continueCondition, () -> {});
    }

    public void startEditHabitMenu() {
        Optional<Habit> habitOpt = userInput.selectHabit();
        if (habitOpt.isEmpty()) {
            return;
        }
        System.out.println("Что вы хотите изменить");
        Map<String, Runnable> options = new LinkedHashMap<>();
        Request request = new Request();
        HabitDto habitDto = HabitMapper.INSTANCE.habitToHabitDto(habitOpt.get());
        userInput.setTempHabit(habitDto.clone());
        options.put("1. Название", () -> {
            userInput.enterHabitTitle();
            if (request.editHabit(Session.getToken(), habitDto, userInput.getTempHabit())) {
                System.out.printf("Привычка (%s) успешно обновлена%n", userInput.getTempHabit().getTitle());
            };
        });
        options.put("2. Описание",  () -> {
            userInput.enterHabitDescription();
            if (request.editHabit(Session.getToken(), habitDto, userInput.getTempHabit())) {
                System.out.printf("Привычка (%s) успешно обновлена%n", userInput.getTempHabit().getTitle());
            };
        });
        options.put("3. Частоту",  () -> {
            userInput.enterHabitPeriod();
            if (request.editHabit(Session.getToken(), habitDto, userInput.getTempHabit())) {
                System.out.printf("Привычка (%s) успешно обновлена%n", userInput.getTempHabit().getTitle());
            };
        });
        options.put("0. Назад", () -> {});

        Supplier<Boolean> continueCondition = Session::isAuthorized;
        displayMenu(options, continueCondition, () -> {});
    }

    private void startAdminActionsMenu() {
        System.out.println("Администрирование пользователей");
        Map<String, Runnable> options = new LinkedHashMap<>();
        options.put("1. Вывести список пользователей", () -> inputOrder.getProfilesList().forEach(System.out::println));
        options.put("2. Заблокировать пользователя",
                () -> inputOrder.operateProfile("заблокировать", AccountRepository.ProfileAction.BLOCK));
        options.put("3. Разблокировать пользователя",
                () -> inputOrder.operateProfile("разблокировать", AccountRepository.ProfileAction.UNBLOCK));
        options.put("4. Удалить пользователя",
                () -> inputOrder.operateProfile("удалить", AccountRepository.ProfileAction.DELETE));
        options.put("0. Назад", () -> {});
        Supplier<Boolean> continueCondition = Session::isAuthorized;
        displayMenu(options, continueCondition, () -> {});
    }


}
