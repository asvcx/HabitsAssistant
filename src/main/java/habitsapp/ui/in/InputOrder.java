package habitsapp.ui.in;

import habitsapp.data.repository.DataLoader;
import habitsapp.data.repository.Repository;
import habitsapp.data.models.Habit;
import habitsapp.data.models.User;
import habitsapp.ui.session.Session;

import java.util.*;
import java.util.function.Supplier;

import static habitsapp.ui.in.UserInputByConsole.*;
import static habitsapp.data.repository.DataLoader.repository;

public class InputOrder {

    public static void userAuthorization() {
        System.out.println("Введите электронную почту");
        String email = currentScanner.nextLine();
        if (!repository.isUserExists(email)) {
            System.out.printf("Пользователь с указанным email не найден (%s)%n", email);
            return;
        }
        System.out.println("Введите пароль");
        String password = currentScanner.nextLine();
        Optional<User> tempUser = repository.userAuth(email, password);
        if (tempUser.isPresent()) {
            Session.start(tempUser.get());
            System.out.printf("Вы вошли как %s%n", Session.getCurrentName());
        } else {
            System.out.println("Не удалось войти в аккаунт");
        }
    }

    public static void userRegistration() {
        Supplier<Optional<String>> step1 = () -> DataLoader.userInput.enterUserName();
        Supplier<Optional<String>> step2 = () -> DataLoader.userInput.enterUserEmail();
        Supplier<Optional<String>> step3 = () -> DataLoader.userInput.enterUserPassword();
        if (step1.get().isEmpty() || step2.get().isEmpty() || step3.get().isEmpty()) {
            return;
        }
        if (repository.isUserExists(DataLoader.userInput.getTempUser().getEmail())) {
            System.out.println("Пользователь с указанным email уже существует");
            return;
        }
        if (repository.createUser(DataLoader.userInput.getTempUser())) {
            System.out.println("Регистрация прошла успешно");
        } else {
            System.out.println("Не удалось зарегистрироваться");
        }
    }

    public static List<String> getProfilesList() {
        return repository.getUserNamesList(Session.getCurrentProfile());
    }

    public static void operateProfile(String actionWord, Repository.ProfileAction action) {
        System.out.printf("Введите электронную почту пользователя, которого требуется %s.%n", actionWord);
        String emailToRemove = currentScanner.nextLine();
        boolean success = repository.manageUserProfile(Session.getCurrentProfile(), emailToRemove, action);
        if (success) {
            System.out.printf("Действие выполнено успешно (%s пользователя %s) ().%n", actionWord, emailToRemove);
        } else {
            System.out.printf("Не удалось %s пользователя.%n", actionWord);
        }
    }

    public static void createHabit() {
        Habit tempHabit = new Habit();
        Supplier<Optional<String>> title = () -> DataLoader.userInput.enterHabitTitle();
        Supplier<Optional<String>> description = () -> DataLoader.userInput.enterHabitDescription();
        if (title.get().isEmpty() || description.get().isEmpty()) {
            return;
        }
        Optional<Integer> period = DataLoader.userInput.enterHabitPeriod();
        if (period.isEmpty()) {
            return;
        }
        tempHabit.setPeriod(period.get());

        if (repository.createHabit(Session.getCurrentEmail(), tempHabit)) {
            System.out.println("Привычка добавлена");
        } else {
            System.out.println("Не удалось добавить привычку");
        }
    }

    public static void markCompletion() {
        Optional<Habit> habitOpt = DataLoader.userInput.selectHabit();
        if (habitOpt.isEmpty()) {
            return;
        }
        Habit habit = habitOpt.get();
        boolean marked = repository.markHabitAsCompleted(Session.getCurrentEmail(), habit);
        if(marked) {
            System.out.printf("Вы выполнили привычку %s%n",  habit.getTitle());
        } else {
            System.out.println("Не удалось записать выполнение привычки");
        }
    }

    public static boolean deleteHabit() {
        Optional<Habit> habitOpt = DataLoader.userInput.selectHabit();
        if (habitOpt.isEmpty()) {
            return false;
        }
        Habit habit = habitOpt.get();
        boolean result = repository.deleteHabit(Session.getCurrentEmail(), habit.getTitle());
        System.out.printf("Привычка (%s) удалена%n", habit.getTitle());
        return result;
    }

    public static void resetUserPassword() {
        System.out.println("В настоящий момент не поддерживается");
    }

    public static void deleteUserProfile() {
        System.out.println("Введите пароль, чтобы удалить профиль.");
        String password = currentScanner.nextLine();
        boolean success = repository.deleteOwnAccount(Session.getCurrentEmail(), password);
        if (success) {
            System.out.println("Профиль успешно удален.");
            Session.exitFromProfile();
        } else {
            System.out.println("Не удалось удалить профиль. Неверный пароль.");
        }
    }

}
