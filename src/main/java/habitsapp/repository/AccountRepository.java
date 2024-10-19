package habitsapp.repository;

import habitsapp.models.Habit;
import habitsapp.models.User;

import java.time.Instant;
import java.util.*;
import java.util.stream.IntStream;

public class AccountRepository {
    public enum ProfileAction {
        BLOCK,
        UNBLOCK,
        DELETE
    }

    private static final Map<User,TreeSet<Habit>> habitsOfUser = new HashMap<>();
    private static final Map<String,User> userByEmail = new HashMap<>();
    //private static final List<User> updatedUsers = new LinkedList<>();
    //private static final List<User> createdUsers = new LinkedList<>();
    //private static final List<User> deletedUsers = new LinkedList<>();

    public AccountRepository() {

    }

    public boolean loadUser(User user) {
        if (!userByEmail.containsKey(user.getEmail().toLowerCase())) {
            habitsOfUser.put(user, new TreeSet<>());
            userByEmail.put(user.getEmail().toLowerCase(), user);
            return true;
        }
        return false;
    }

    public boolean registerUser(User user) {
        if (loadUser(user)) {
            user.setAccountStatus(User.AccountStatus.CREATED);
            return true;
        }
        return false;
    }

    public boolean loadHabit(String email, Habit habit) {
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

    public boolean isUserExists(String email) {
        return userByEmail.containsKey(email.toLowerCase());
    }

    public User userAuth(String email, String password) {
        if (!userByEmail.containsKey(email.toLowerCase())) {
            return null;
        }
        User user = userByEmail.get(email.toLowerCase());
        if (user.comparePassword(password) && !user.isBlocked()) {
            return userByEmail.get(email.toLowerCase()).clone();
        }
        return null;
    }

    public TreeSet<Habit> getHabitsList(String email) {
        if (!userByEmail.containsKey(email.toLowerCase())) {
            return new TreeSet<>();
        }
        User user = userByEmail.get(email.toLowerCase());
        if (habitsOfUser.containsKey(user)) {
            return new TreeSet<>(habitsOfUser.get(user));
        }
        return new TreeSet<>();
    }

    public boolean markHabitAsCompleted(String email, Habit habit) {
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
        if (ceil == null || !ceil.equals(floor)) {
            return false;
        }
        return ceil.markAsCompleted();
    }

    public boolean addCompletionDate(String email, String title, Instant date) {
        if (!userByEmail.containsKey(email.toLowerCase())) {
            return false;
        }
        User user = userByEmail.get(email.toLowerCase());
        TreeSet<Habit> habits = habitsOfUser.get(user);
        if (habits.isEmpty()) {
            return false;
        }
        Optional<Habit> habitOpt = habits.stream().filter(v -> v.getTitle().equals(title)).findFirst();
        if (habitOpt.isPresent()) {
            habitOpt.get().addCompletionDate(date);
            return true;
        }
        return false;
    }

    public boolean editHabit(String email, Habit oldHabit, Habit newHabit) {
        User user = userByEmail.get(email.toLowerCase());
        TreeSet<Habit> userHabits = habitsOfUser.get(user);
        if (!userHabits.contains(oldHabit)) {
            return false;
        }
        userHabits.remove(oldHabit);
        userHabits.add(newHabit);
        return true;
    }

    public boolean editUserData(String email, User changedUser, String password) {
        if (!userByEmail.containsKey(email.toLowerCase())) {
            return false;
        }
        User user = userByEmail.get(email.toLowerCase());
        if (!user.comparePassword(password)) {
            return false;
        }
        if (user.getName().equals(changedUser.getName())
                && user.getEmail().equals(changedUser.getEmail().toLowerCase())) {
            return false;
        }
        userByEmail.remove(user.getEmail());
        user.setName(changedUser.getName());
        user.setEmail(changedUser.getEmail().toLowerCase());
        userByEmail.put(changedUser.getEmail().toLowerCase(), user);
        user.setAccountStatus(User.AccountStatus.UPDATED);
        return true;
    }

    public boolean editUserPassword(String email, String oldPassword, String newPassword) {
        User user = userByEmail.get(email.toLowerCase());
        TreeSet<Habit> userHabits = habitsOfUser.get(user);
        if (user.comparePassword(oldPassword)) {
            habitsOfUser.remove(user);
            user.setPassword(newPassword);
            habitsOfUser.put(user, userHabits);
            user.setAccountStatus(User.AccountStatus.DELETED);
            return true;
        } else {
            return false;
        }
    }

    public boolean deleteOwnAccount(String email, String userPassword) {
        if (!userByEmail.containsKey(email.toLowerCase())) {
            return false;
        }
        User user = userByEmail.get(email.toLowerCase());
        if (user.comparePassword(userPassword)) {
            habitsOfUser.remove(user);
            userByEmail.remove(email.toLowerCase());
            user.setAccountStatus(User.AccountStatus.DELETED);
            return true;
        } else {
            return false;
        }
    }

    public void deleteHabit(String email, Habit habit) {
        User user = userByEmail.get(email.toLowerCase());
        Set<Habit> userHabits = habitsOfUser.get(user);
        userHabits.remove(habit);
    }

    public List<String> getUserNamesList(User admin) {
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

    public List<User> getUsersList(User.AccountStatus accountStatus) {
        List<User> users = new LinkedList<>();
        for (User user: habitsOfUser.keySet()) {
            if (user.getAccountStatus().equals(accountStatus)) {
                users.add(user);
            }
        }
        return users;
    }

    public List<User> getUserNamesList() {
        if (!habitsOfUser.isEmpty()) {
            return new LinkedList<>();
        }
        return new LinkedList<>(habitsOfUser.keySet());
    }

    public boolean manageUserProfile(User admin, String emailToManage, ProfileAction profileAction) {
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
                        user.setAccountStatus(User.AccountStatus.UPDATED);
                        return true;
                    }
                    return false;
                }
                case ProfileAction.UNBLOCK: {
                    if (user.isBlocked()) {
                        user.unblock();
                        user.setAccountStatus(User.AccountStatus.UPDATED);
                        return true;
                    }
                    return false;
                }
                case ProfileAction.DELETE: {
                    habitsOfUser.remove(user);
                    userByEmail.remove(emailToManage);
                    user.setAccountStatus(User.AccountStatus.DELETED);
                    return true;
                }
            }
        }
        return false;
    }


}
