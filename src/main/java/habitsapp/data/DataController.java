package habitsapp.data;

import habitsapp.models.Habit;
import habitsapp.models.User;
import java.util.*;
import java.util.stream.IntStream;

public class DataController {
    public enum ProfileAction {
        BLOCK,
        UNBLOCK,
        DELETE
    }

    private static final HashMap<User, TreeSet<Habit>> habitsOfUser = new HashMap<>();
    private static final HashMap<String,User> userByEmail = new HashMap<>();

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
        User user = userByEmail.get(email.toLowerCase());
        if (user.isPasswordProper(password) && !user.isBlocked()) {
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
        User existingUser = userByEmail.get(email.toLowerCase());
        if (existingUser.getName().equals(changedUser.getName())
                && existingUser.getEmail().equals(changedUser.getEmail().toLowerCase())) {
            return false;
        }
        userByEmail.remove(existingUser.getEmail());
        existingUser.setName(changedUser.getName());
        existingUser.setEmail(changedUser.getEmail().toLowerCase());
        userByEmail.put(changedUser.getEmail().toLowerCase(), existingUser);
        return true;
    }

    public static boolean editUserPassword(String email, String oldPassword, String newPassword) {
        User user = userByEmail.get(email.toLowerCase());
        //LinkedHashSet;
        TreeSet<Habit> userHabits = habitsOfUser.get(user);
        if (user.isPasswordProper(oldPassword)) {
            habitsOfUser.remove(user);
            user.setPassword(newPassword);
            habitsOfUser.put(user, userHabits);
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
        if (user.isPasswordProper(userPassword)) {
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

    public static List<String> getProfilesList(User admin) {
        if (!userByEmail.containsKey(admin.getEmail())) {
            return new LinkedList<>();
        }
        User authenticUser = userByEmail.get(admin.getEmail());
        if (authenticUser.isAdmin() && authenticUser.isUserAuthentic(admin)) {
            Set<User> userSet = habitsOfUser.keySet();
            return IntStream.range(0, userSet.size())
                    .mapToObj(i -> (i + 1) + ". " + userSet.stream().toList().get(i))
                    .toList();
        }
        return new LinkedList<>();
    }

    public static boolean manageUserProfile(User admin, String emailToManage, ProfileAction profileAction) {
        if (!userByEmail.containsKey(admin.getEmail()) || !userByEmail.containsKey(emailToManage)) {
            return false;
        }
        User authenticUser = userByEmail.get(admin.getEmail());
        if (authenticUser.isAdmin() && authenticUser.isUserAuthentic(admin)) {
            User user = userByEmail.get(emailToManage);
            switch (profileAction) {
                case ProfileAction.BLOCK: {
                    if (!user.isBlocked()) {
                        user.block();
                        return true;
                    }
                    return false;
                }
                case ProfileAction.UNBLOCK: {
                    if (user.isBlocked()) {
                        user.unblock();
                        return true;
                    }
                    return false;
                }
                case ProfileAction.DELETE: {
                    habitsOfUser.remove(user);
                    userByEmail.remove(emailToManage);
                    return true;
                }
            }
        }
        return false;
    }


}
