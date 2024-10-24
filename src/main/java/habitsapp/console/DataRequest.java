package habitsapp.console;

import habitsapp.data.DataController;
import habitsapp.models.Habit;
import habitsapp.models.User;
import habitsapp.session.Session;

import java.util.*;
import java.util.function.Supplier;

import static habitsapp.console.Input.*;

public class DataRequest {

    public static void userAuthorization() {
        System.out.println("Введите электронную почту");
        String email = currentScanner.nextLine();
        if (!DataController.userExists(email)) {
            System.out.printf("Пользователь с указанным email не найден (%s)%n", email);
            return;
        }
        System.out.println("Введите пароль");
        String password = currentScanner.nextLine();
        User tempUser = DataController.userAuth(email, password);
        if (tempUser != null) {
            Session.start(tempUser);
            System.out.printf("Вы вошли как %s%n", Session.getCurrentName());
        } else {
            System.out.println("Не удалось войти в аккаунт");
        }
    }

    public static void userRegistration() {
        User tempUser = new User();
        Supplier<String> step1 = () -> stringInput("Введите имя пользователя",
                s -> s.length() > 3,
                tempUser::setName,
                "Имя должно содержать не менее 4 символов"
        );
        Supplier<String> step2 = () -> stringInput("Введите электронную почту",
                Input::isValidEmail,
                tempUser::setEmail,
                "Неверный формат почты"
        );
        Supplier<String> step3 = () -> stringInput("Введите пароль",
                s -> s.length() > 5,
                tempUser::setPassword,
                "Пароль должен содержать не менее 6 символов"
        );
        if (step1.get().isEmpty() || step2.get().isEmpty() || step3.get().isEmpty()) {
            return;
        }
        if (DataController.userExists(tempUser.getEmail())) {
            System.out.println("Пользователь с указанным email уже существует");
            return;
        }
        if (DataController.addUser(tempUser)) {
            System.out.println("Регистрация прошла успешно");
        } else {
            System.out.println("Не удалось зарегистрироваться");
        }
    }

    public static List<String> getProfilesList() {
        return DataController.getProfilesList(Session.getCurrentProfile());
    }

    public static void operateProfile(String actionWord, DataController.ProfileAction action) {
        System.out.printf("Введите электронную почту пользователя, которого требуется %s.%n", actionWord);
        String emailToRemove = currentScanner.nextLine();
        boolean success = DataController.manageUserProfile(Session.getCurrentProfile(), emailToRemove, action);
        if (success) {
            System.out.printf("Действие выполнено успешно (%s пользователя %s) ().%n", actionWord, emailToRemove);
        } else {
            System.out.printf("Не удалось %s пользователя.%n", actionWord);
        }
    }

    public static void createHabit() {
        Habit tempHabit = new Habit();
        Supplier<String> title = () -> stringInput("Введите название привычки",
                s -> !s.isEmpty(),
                tempHabit::setTitle,
                "Недопустимое название");
        Supplier<String> description = () -> stringInput("Введите описание привычки",
                _ -> true,
                tempHabit::setDescription,
                "Недопустимое описание");
        if ("".equals(title.get()) || "".equals(description.get())) {
            return;
        }
        Supplier<Integer> period = () -> intInput("Введите частоту в сутках", 1, 365);
        tempHabit.setPeriod(period.get());

        if (DataController.addHabit(Session.getCurrentEmail(), tempHabit)) {
            System.out.println("Привычка добавлена");
        } else {
            System.out.println("Не удалось добавить привычку");
        }
    }

    public static void markCompletion() {
        Optional<Habit> habitOpt = selectHabit();
        if (habitOpt.isEmpty()) {
            return;
        }
        Habit habit = habitOpt.get();
        boolean marked = DataController.markAsCompleted(Session.getCurrentEmail(), habit);
        if(marked) {
            System.out.printf("Вы выполнили привычку %s%n",  habit.getTitle());
        } else {
            System.out.println("Не удалось записать выполнение привычки");
        }
    }

    public static void editHabit() {
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

        switch(currentScanner.nextLine()) {
            case "1" : {
                stringInput("Введите новое название",
                        s -> !s.isEmpty(),
                        tempHabit::setTitle,
                        "Недопустимое название");
                break;
            }
            case "2" : {
                System.out.println("Введите новое описание привычки");
                tempHabit.setDescription(currentScanner.nextLine());
                break;
            }
            case "3" : {
                int period = intInput("Введите частоту в сутках", 1, 365);
                tempHabit.setPeriod(period);
                tempHabit.setPeriod(currentScanner.nextInt());
                break;
            }
            default : {
                return;
            }
        }
        if (tempHabit.getTitle().isEmpty()) {
            return;
        }
        DataController.editHabit(Session.getCurrentEmail(), habit, tempHabit);
        System.out.printf("Привычка (%s) успешно обновлена%n", tempHabit.getTitle());
    }

    public static void deleteHabit() {
        Optional<Habit> habitOpt = selectHabit();
        if (habitOpt.isEmpty()) {
            return;
        }
        Habit habit = habitOpt.get();
        DataController.deleteHabit(Session.getCurrentEmail(), habit);
        System.out.printf("Привычка (%s) удалена%n", habit.getTitle());
    }

    public static void editUserData() {
        User tempUser = Session.getCurrentProfile().clone();
        String[] editOptions = {
                "1. Имя пользователя",
                "2. Электронная почта",
                "3. Пароль",
                "0. Назад"
        };
        String password = "";
        System.out.println("Что вы хотите изменить");
        Arrays.stream(editOptions).forEach(System.out::println);
        boolean success = false;
        switch(currentScanner.nextLine()) {
            case "1" : {
                stringInput("Введите новое имя",
                        s -> s.length() > 3,
                        tempUser::setName,
                        "Имя должно содержать не менее 4 символов");
                System.out.println("Введите пароль");
                password = currentScanner.nextLine();
                success = DataController.editUserData(Session.getCurrentEmail(), tempUser, password);
                break;
            }
            case "2" : {
                stringInput("Введите новую почту",
                        Input::isValidEmail,
                        tempUser::setEmail,
                        "Неверный формат почты");
                System.out.println("Введите пароль");
                password = currentScanner.nextLine();
                success = DataController.editUserData(Session.getCurrentEmail(), tempUser, password);
                break;
            }
            case "3" : {
                System.out.println("Введите старый пароль");
                String oldPassword = currentScanner.nextLine();
                String newPassword = stringInput("Введите новый пароль",
                        s -> s.length() > 5,
                        _ -> {},
                        "Пароль должен содержать не менее 6 символов");
                success = DataController.editUserPassword(Session.getCurrentEmail(), oldPassword, newPassword);
                break;
            }
            case "0" : {
                return;
            }
            default : {
                System.out.println("Действие не выбрано");
                break;
            }
        }
        if (success) {
            User user = DataController.userAuth(tempUser.getEmail(), password);
            Session.start(user);
            Session.update();
            System.out.println("Данные успешно изменены");
        } else {
            System.out.println("Не удалось изменить данные");
        }
    }

    public static void resetUserPassword() {
        System.out.println("В настоящий момент не поддерживается");
    }

    public static void deleteUserProfile() {
        System.out.println("Введите пароль, чтобы удалить профиль.");
        String password = currentScanner.nextLine();
        boolean success = DataController.deleteUserProfile(Session.getCurrentEmail(), password);
        if (success) {
            System.out.println("Профиль успешно удален.");
            Session.exitFromProfile();
        } else {
            System.out.println("Не удалось удалить профиль. Неверный пароль.");
        }
    }

}
