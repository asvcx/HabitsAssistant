package habitsapp.in;

import habitsapp.repository.DataLoader;
import habitsapp.repository.AccountRepository;
import habitsapp.models.Habit;
import habitsapp.models.User;
import habitsapp.session.Session;

import java.util.*;
import java.util.function.Supplier;

import static habitsapp.in.InputDataByConsole.*;
import static habitsapp.repository.DataLoader.accountRepository;

public class InputOrder {

    public static void userAuthorization() {
        System.out.println("Введите электронную почту");
        String email = currentScanner.nextLine();
        if (!accountRepository.isUserExists(email)) {
            System.out.printf("Пользователь с указанным email не найден (%s)%n", email);
            return;
        }
        System.out.println("Введите пароль");
        String password = currentScanner.nextLine();
        User tempUser = accountRepository.userAuth(email, password);
        if (tempUser != null) {
            Session.start(tempUser);
            System.out.printf("Вы вошли как %s%n", Session.getCurrentName());
        } else {
            System.out.println("Не удалось войти в аккаунт");
        }
    }

    public static void userRegistration() {
        Supplier<Optional<String>> step1 = () -> DataLoader.inputData.enterUserName();
        Supplier<Optional<String>> step2 = () -> DataLoader.inputData.enterUserEmail();
        Supplier<Optional<String>> step3 = () -> DataLoader.inputData.enterUserPassword();
        if (step1.get().isEmpty() || step2.get().isEmpty() || step3.get().isEmpty()) {
            return;
        }
        if (accountRepository.isUserExists(DataLoader.inputData.getTempUser().getEmail())) {
            System.out.println("Пользователь с указанным email уже существует");
            return;
        }
        if (accountRepository.registerUser(DataLoader.inputData.getTempUser())) {
            System.out.println("Регистрация прошла успешно");
        } else {
            System.out.println("Не удалось зарегистрироваться");
        }
    }

    public static List<String> getProfilesList() {
        return accountRepository.getUserNamesList(Session.getCurrentProfile());
    }

    public static void operateProfile(String actionWord, AccountRepository.ProfileAction action) {
        System.out.printf("Введите электронную почту пользователя, которого требуется %s.%n", actionWord);
        String emailToRemove = currentScanner.nextLine();
        boolean success = accountRepository.manageUserProfile(Session.getCurrentProfile(), emailToRemove, action);
        if (success) {
            System.out.printf("Действие выполнено успешно (%s пользователя %s) ().%n", actionWord, emailToRemove);
        } else {
            System.out.printf("Не удалось %s пользователя.%n", actionWord);
        }
    }

    public static void createHabit() {
        Habit tempHabit = new Habit();
        Supplier<Optional<String>> title = () -> DataLoader.inputData.enterHabitTitle();
        Supplier<Optional<String>> description = () -> DataLoader.inputData.enterHabitDescription();
        if (title.get().isEmpty() || description.get().isEmpty()) {
            return;
        }
        Optional<Integer> period = DataLoader.inputData.enterHabitPeriod();
        if (period.isEmpty()) {
            return;
        }
        tempHabit.setPeriod(period.get());

        if (accountRepository.loadHabit(Session.getCurrentEmail(), tempHabit)) {
            System.out.println("Привычка добавлена");
        } else {
            System.out.println("Не удалось добавить привычку");
        }
    }

    public static void markCompletion() {
        Optional<Habit> habitOpt = DataLoader.inputData.selectHabit();
        if (habitOpt.isEmpty()) {
            return;
        }
        Habit habit = habitOpt.get();
        boolean marked = accountRepository.markHabitAsCompleted(Session.getCurrentEmail(), habit);
        if(marked) {
            System.out.printf("Вы выполнили привычку %s%n",  habit.getTitle());
        } else {
            System.out.println("Не удалось записать выполнение привычки");
        }
    }

    public static void deleteHabit() {
        Optional<Habit> habitOpt = DataLoader.inputData.selectHabit();
        if (habitOpt.isEmpty()) {
            return;
        }
        Habit habit = habitOpt.get();
        accountRepository.deleteHabit(Session.getCurrentEmail(), habit);
        System.out.printf("Привычка (%s) удалена%n", habit.getTitle());
    }

    public static void resetUserPassword() {
        System.out.println("В настоящий момент не поддерживается");
    }

    public static void deleteUserProfile() {
        System.out.println("Введите пароль, чтобы удалить профиль.");
        String password = currentScanner.nextLine();
        boolean success = accountRepository.deleteOwnAccount(Session.getCurrentEmail(), password);
        if (success) {
            System.out.println("Профиль успешно удален.");
            Session.exitFromProfile();
        } else {
            System.out.println("Не удалось удалить профиль. Неверный пароль.");
        }
    }

}
