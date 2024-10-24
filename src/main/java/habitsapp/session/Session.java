package habitsapp.session;

import habitsapp.data.DataController;
import habitsapp.models.Habit;
import habitsapp.models.User;

import java.util.Set;
import java.util.TreeSet;

public class Session {

    private static User currentProfile = null;
    private static Set<Habit> currentHabits = new TreeSet<>();

    public static void start(User user) {
        if (user != null) {
            setCurrentProfile(user);
            setCurrentHabits(DataController.getHabits(getCurrentEmail()));
        }
    }

    public static void update() {
        setCurrentHabits(DataController.getHabits(getCurrentEmail()));
    }

    public static void setCurrentProfile(User user) {
        currentProfile = user;
    }

    public static void setCurrentHabits(Set<Habit> habits) {
        currentHabits = habits;
    }

    public static User getCurrentProfile() {
        return currentProfile;
    }

    public static String getCurrentEmail() {
        return currentProfile.getEmail();
    }

    public static String getCurrentName() {
        return currentProfile.getName();
    }

    public static Set<Habit> getCurrentHabits() {
        return currentHabits;
    }

    public static boolean isAuthorized() {
        return currentProfile != null;
    }

    public static boolean isAdmin() {
        return currentProfile != null && currentProfile.isAdmin();
    }

    public static void exitFromProfile() {
        currentProfile = null;
        currentHabits = null;
        System.out.println("Вы вышли из профиля.");
    }


}
