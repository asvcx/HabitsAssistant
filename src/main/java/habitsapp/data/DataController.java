package habitsapp.data;

import habitsapp.models.Habit;
import habitsapp.models.User;
import java.util.*;

public class DataController {
    private static HashMap<User, TreeSet<Habit>> habitsOfUser = new HashMap<>();
    private static HashMap<String,User> userByEmail = new HashMap<>();

    public static boolean addUser(User user) {
        if (!userByEmail.containsKey(user.getEmail().toLowerCase())) {
            habitsOfUser.put(user, new TreeSet<>());
            userByEmail.put(user.getEmail().toLowerCase(), user);
            return true;
        }
        return false;
    }

    public static boolean addHabit(String email, Habit habit) {
        if (!userByEmail.containsKey(email.toLowerCase())) {
            return false;
        }
        User user = userByEmail.get(email.toLowerCase());
        if (!habitsOfUser.containsKey(user)) {
            return false;
        }
        TreeSet<Habit> habitsSet = habitsOfUser.get(user);
        habitsSet.add(habit);
        return true;
    }

    public static boolean userExists(String email) {
        return userByEmail.containsKey(email.toLowerCase());
    }

    public static User userAuth(String email, String password) {
        if (!userByEmail.containsKey(email.toLowerCase())) {
            return null;
        }
        if (userByEmail.get(email.toLowerCase()).isPasswordValid(password)) {
            return userByEmail.get(email.toLowerCase()).clone();
        }
        return null;
    }

    public static TreeSet<Habit> getHabits(String email) {
        if (!userByEmail.containsKey(email.toLowerCase())) {
            return null;
        }
        User user = userByEmail.get(email.toLowerCase());
        if (habitsOfUser.containsKey(user)) {
            return new TreeSet<>(habitsOfUser.get(user));
        }
        return null;
    }

    public static boolean markAsCompleted(String email, Habit habit) {
        if (!userByEmail.containsKey(email.toLowerCase())) {
            return false;
        }
        User user = userByEmail.get(email.toLowerCase());
        TreeSet<Habit> habits = habitsOfUser.get(user);
        if (habits.isEmpty()) {
            return false;
        }
        Habit ceil = habits.ceiling(habit);
        Habit floor = habits.floor(habit);
        if (!ceil.equals(floor)) {
            return false;
        }
        return ceil.markAsCompleted();
    }

    public static boolean editHabit(String email, Habit oldHabit, Habit newHabit) {
        User user = userByEmail.get(email.toLowerCase());
        TreeSet<Habit> userHabits = habitsOfUser.get(user);
        if (!userHabits.contains(oldHabit)) {
            return false;
        }
        userHabits.remove(oldHabit);
        userHabits.add(newHabit);
        return true;
    }

    public static boolean editUserData(String email, User changedUser) {
        if (!userByEmail.containsKey(email.toLowerCase())) {
            return false;
        }
        User user = userByEmail.get(email.toLowerCase());
        user.setName(changedUser.getName());
        user.setEmail(changedUser.getEmail());
        return true;
    }

    public static boolean editUserPassword(String email, String oldPassword, String newPassword) {
        User user = userByEmail.get(email.toLowerCase());
        if (user.isPasswordValid(oldPassword)) {
            user.setPassword(newPassword);
            return true;
        } else {
            return false;
        }
    }

    public static boolean deleteUserProfile(String email, String userPassword) {
        if (!userByEmail.containsKey(email.toLowerCase())) {
            return false;
        }
        User user = userByEmail.get(email.toLowerCase());
        if (user.isPasswordValid(userPassword)) {
            habitsOfUser.remove(user);
            userByEmail.remove(email.toLowerCase());
            return true;
        } else {
            return false;
        }
    }

    public static void deleteHabit(String email, Habit habit) {
        User user = userByEmail.get(email.toLowerCase());
        Set<Habit> userHabits = habitsOfUser.get(user);
        userHabits.remove(habit);
    }


}
