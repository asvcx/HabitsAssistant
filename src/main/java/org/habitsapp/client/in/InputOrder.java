package org.habitsapp.client.in;

import org.habitsapp.client.session.Request;
import org.habitsapp.client.session.Session;
import org.habitsapp.models.results.AuthorizationResult;
import org.habitsapp.models.results.RegistrationResult;
import org.habitsapp.models.dto.HabitDto;
import org.habitsapp.models.Habit;
import org.habitsapp.models.dto.HabitMapper;
import org.habitsapp.models.dto.UserDto;
import org.habitsapp.server.repository.AccountRepository;

import java.util.*;
import java.util.function.Supplier;

import static org.habitsapp.client.in.UserInputByConsole.*;

public class InputOrder {

    private UserInput userInput = new UserInputByConsole();

    public void userAuthorization() {
        System.out.println("Введите электронную почту");
        String email = currentScanner.nextLine();
        System.out.println("Введите пароль");
        String password = currentScanner.nextLine();
        Request request = new Request();
        AuthorizationResult result = request.login(email, password);
        if (result.isSuccess()) {
            Session.start(result.getUserDto(), result.getToken());
            System.out.printf("Вы вошли как %s%n", Session.getName());
        } else {
            System.out.println("Не удалось войти в аккаунт: " + result.getMessage());
        }
    }

    public void userRegistration() {
        Supplier<Optional<String>> step1 = () -> userInput.enterUserName();
        Supplier<Optional<String>> step2 = () -> userInput.enterUserEmail();
        Supplier<Optional<String>> step3 = () -> userInput.enterUserPassword();
        if (step1.get().isEmpty() || step2.get().isEmpty() || step3.get().isEmpty()) {
            return;
        }
        Request request = new Request();
        UserDto user = userInput.getTempUser();
        RegistrationResult result = request.register(user.getName(), user.getEmail(), user.getPassword());
        if (result.isSuccess()) {
            System.out.println("Регистрация прошла успешно");
        } else {
            System.out.println("Не удалось зарегистрироваться: " + result.getMessage());
        }
    }

    public List<String> getProfilesList() {
        Request request = new Request();
        return request.getProfilesList(Session.getEmail(), Session.getToken());
    }

    public void operateProfile(String actionWord, AccountRepository.ProfileAction action) {
        System.out.printf("Введите электронную почту пользователя, которого требуется %s.%n", actionWord);
        String emailToRemove = currentScanner.nextLine();
        Request request = new Request();
        boolean success = request.manageUserProfile(Session.getEmail(), Session.getToken(), emailToRemove, action);
        if (success) {
            System.out.printf("Действие выполнено успешно (%s пользователя %s) ().%n", actionWord, emailToRemove);
        } else {
            System.out.printf("Не удалось %s пользователя.%n", actionWord);
        }
    }

    public void createHabit() {
        Optional<String> title = userInput.enterHabitTitle();
        Optional<String> description = userInput.enterHabitDescription();
        Optional<Integer> period = userInput.enterHabitPeriod();
        if (title.isEmpty() || description.isEmpty() || period.isEmpty()) {
            System.out.println("Недостаточно данных для создания привычки");
            return;
        }
        Request request = new Request();
        System.out.printf("title [%s]; description [%s]; period [%d]; userId [%d]", title.get(), description.get(), period.get(), Session.getID());
        HabitDto tempHabit = new HabitDto(title.get(), description.get(), period.get(), Session.getID());
        boolean result = request.createHabit(Session.getToken(), tempHabit);
        if (result) {
            System.out.println("Привычка добавлена");
        } else {
            System.out.println("Не удалось добавить привычку");
        }
    }

    public void markCompletion() {
        Optional<Habit> habitOpt = userInput.selectHabit();
        if (habitOpt.isEmpty()) {
            return;
        }
        Request request = new Request();
        HabitDto habitDto = HabitMapper.INSTANCE.habitToHabitDto(habitOpt.get());
        boolean marked = request.markHabit(Session.getToken(), habitDto);
        if(marked) {
            System.out.printf("Вы выполнили привычку %s%n",  habitDto.getTitle());
        } else {
            System.out.println("Не удалось записать выполнение привычки");
        }
    }

    public boolean deleteHabit() {
        Optional<Habit> habitOpt = userInput.selectHabit();
        if (habitOpt.isEmpty()) {
            return false;
        }
        HabitDto habitDto = HabitMapper.INSTANCE.habitToHabitDto(habitOpt.get());
        Request request = new Request();
        boolean result = request.deleteHabit(Session.getToken(), habitDto);
        System.out.printf("Привычка (%s) удалена%n", habitDto.getTitle());
        return result;
    }

    public void resetUserPassword() {
        System.out.println("В настоящий момент не поддерживается");
    }

    public void deleteOwnProfile() {
        System.out.println("Введите пароль, чтобы удалить профиль.");
        String password = currentScanner.nextLine();
        Request request = new Request();
        boolean success = request.deleteOwnProfile(Session.getToken(), password);
        if (success) {
            System.out.println("Профиль успешно удален.");
            Session.logout();
        } else {
            System.out.println("Не удалось удалить профиль. Неверный пароль.");
        }
    }

}
