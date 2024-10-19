package habitsapp.in;

import habitsapp.models.Habit;
import habitsapp.models.User;

import java.time.Instant;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

public abstract class InputData {

    protected User tempUser;
    protected Habit tempHabit;

    public InputData() {
        this.tempUser = new User();
        this.tempHabit = new Habit();
    }

    public User getTempUser() {
        return tempUser.clone();
    }

    public Habit getTempHabit() {
        return tempHabit.clone();
    }

    public static boolean isValidName(String name) {
        return name.length() > 3;
    }

    public static boolean isValidDate(String dateString) {
        return dateString.matches("^\\d{1,2}-\\d{1,2}-\\d{2}$");
    }

    public static boolean isValidEmail(String email) {
        return email.matches("^[\\w-]+(\\.[\\w-]+)*@[\\w-]+(\\.[\\w-]{2,})+$");
    }

    public static boolean isValidPassword(String password) {
        return password.length() > 5;
    }

    public void setTempUser(User user) {
        this.tempUser = user;
    }

    public void setTempHabit(Habit habit) {
        this.tempHabit = habit;
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
