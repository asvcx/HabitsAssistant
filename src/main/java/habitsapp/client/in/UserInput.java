package habitsapp.client.in;

import habitsapp.models.Habit;
import habitsapp.models.dto.HabitDto;
import habitsapp.models.dto.UserDto;

import java.time.Instant;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

public abstract class UserInput {

    protected UserDto tempUser;
    protected HabitDto tempHabit;

    public UserInput() {
        this.tempUser = new UserDto();
        this.tempHabit = new HabitDto();
    }

    public UserDto getTempUser() {
        return tempUser;
    }

    public HabitDto getTempHabit() {
        return tempHabit.clone();
    }

    public static boolean isNameValid(String name) {
        return name.length() > 3;
    }

    public static boolean isDateValid(String dateString) {
        return dateString.matches("^\\d{1,2}-\\d{1,2}-\\d{2}$");
    }

    public static boolean isEmailValid(String email) {
        return email.matches("^[\\w-]+(\\.[\\w-]+)*@[\\w-]+(\\.[\\w-]{2,})+$");
    }

    public static boolean isPasswordValid(String password) {
        return password.length() > 5;
    }

    public void setTempUser(UserDto userDTO) {
        this.tempUser = userDTO;
    }

    public void setTempHabit(HabitDto habitDto) {
        this.tempHabit = habitDto;
    }

    public abstract Optional<Integer> intInput(String inputMsg, int min, int max);
    public abstract Optional<String> stringInput(String inputMsg, Predicate<String> condition, Consumer<String> action, String failMsg);
    public abstract Optional<Instant> dateTimeInput(String inputMsg);
    public abstract Optional<String> enterUserName();
    public abstract Optional<String> enterUserEmail();
    public abstract Optional<String> enterUserPassword();
    public abstract Optional<String> enterHabitTitle();
    public abstract Optional<String> enterHabitDescription();
    public abstract Optional<Integer> enterHabitPeriod();
    public abstract Optional<Habit> selectHabit();

}
