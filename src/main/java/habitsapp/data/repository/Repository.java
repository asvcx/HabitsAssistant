package habitsapp.data.repository;

import habitsapp.data.models.EntityStatus;
import habitsapp.data.models.Habit;
import habitsapp.data.models.User;
import java.util.*;
import java.util.stream.Collectors;

public class Repository {
    public enum ProfileAction {
        BLOCK,
        UNBLOCK,
        DELETE
    }

    private static final Map<User,TreeSet<Habit>> habitsOfUser = new HashMap<>();
    private static final Map<String,User> userByEmail = new HashMap<>();
    private static final Map<Long,User> userByID = new HashMap<>();

    public Repository() {

    }

    public boolean isUserExists(String email) {
        if (!userByEmail.containsKey(email.toLowerCase())) {
            return false;
        }
        User user = userByEmail.get(email.toLowerCase());
        return user.getAccountStatus() != EntityStatus.DELETED;
    }

    public boolean isUserExists(long id) {
        if (!userByID.containsKey(id)) {
            return false;
        }
        User user = userByID.get(id);
        return user.getAccountStatus() != EntityStatus.DELETED;
    }

    public boolean loadUser(User user) {
        if (!isUserExists(user.getEmail().toLowerCase())) {
            habitsOfUser.put(user, new TreeSet<>());
            userByEmail.put(user.getEmail().toLowerCase(), user);
            userByID.put(user.getID(), user);
            return true;
        }
        return false;
    }

    public boolean createUser(User user) {
        if (loadUser(user)) {
            user.setAccountStatus(EntityStatus.CREATED);
            return true;
        }
        return false;
    }

    public boolean loadHabit(String email, Habit habit) {
        if (!isUserExists(email.toLowerCase())) {
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

    public boolean createHabit(String email, Habit habit) {
        if (loadHabit(email, habit)) {
            habit.setStatus(EntityStatus.CREATED);
            return true;
        }
        return false;
    }

    public boolean setHabits(long userID, List<Habit> habits) {
        if (!isUserExists(userID)) {
            return false;
        }
        User user = userByID.get(userID);
        if (!habitsOfUser.containsKey(user)) {
            habitsOfUser.remove(user);
        }
        TreeSet<Habit> habitsSet = new TreeSet<>(habits);
        habitsOfUser.put(user, habitsSet);
        return true;
    }

    public Optional<User> userAuth(String email, String password) {
        if (!isUserExists(email.toLowerCase())) {
            return Optional.empty();
        }
        User user = userByEmail.get(email.toLowerCase());
        if (user.comparePassword(password)
                && !user.isBlocked()
                && user.getAccountStatus() != EntityStatus.DELETED) {
            return Optional.of(user.clone());
        }
        return Optional.empty();
    }

    public Set<Habit> getHabitsSet(String email) {
        if (!userByEmail.containsKey(email.toLowerCase())) {
            return new TreeSet<>();
        }
        User user = userByEmail.get(email.toLowerCase());
        if (habitsOfUser.containsKey(user)) {
            return habitsOfUser.get(user).stream()
                    .filter(h -> h.getStatus() != EntityStatus.DELETED)
                    .collect(Collectors.toSet());
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
        if (ceil == null
                || ceil.getStatus() == EntityStatus.DELETED
                || !ceil.equals(floor)) {
            return false;
        }
        ceil.markAsCompleted();
        ceil.setStatus(EntityStatus.UPDATED);
        return true;
    }

    public boolean editHabit(String email, Habit oldHabit, Habit newHabit) {
        User user = userByEmail.get(email.toLowerCase());
        TreeSet<Habit> userHabits = habitsOfUser.get(user);
        if (!userHabits.contains(oldHabit)) {
            return false;
        }
        userHabits.remove(oldHabit);
        userHabits.add(newHabit);
        newHabit.setStatus(EntityStatus.UPDATED);
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
        TreeSet<Habit> userHabits = habitsOfUser.get(user);
        userByEmail.remove(user.getEmail());
        habitsOfUser.remove(user);
        user.setName(changedUser.getName());
        user.setEmail(changedUser.getEmail().toLowerCase());
        userByEmail.put(changedUser.getEmail().toLowerCase(), user);
        habitsOfUser.put(user, userHabits);
        user.setAccountStatus(EntityStatus.UPDATED);
        return true;
    }

    public boolean editUserPassword(String email, String oldPassword, String newPassword) {
        User user = userByEmail.get(email.toLowerCase());
        TreeSet<Habit> userHabits = habitsOfUser.get(user);
        if (user.comparePassword(oldPassword)) {
            habitsOfUser.remove(user);
            user.setPassword(newPassword);
            habitsOfUser.put(user, userHabits);
            user.setAccountStatus(EntityStatus.UPDATED);
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
            user.setAccountStatus(EntityStatus.DELETED);
            return true;
        } else {
            return false;
        }
    }

    public boolean deleteHabit(String userEmail, String title) {
        if (!isUserExists(userEmail.toLowerCase())) {
            return false;
        }
        User user = userByEmail.get(userEmail.toLowerCase());
        Set<Habit> userHabits = habitsOfUser.get(user);
        Optional<Habit> habitOpt = userHabits.stream()
                .filter(h -> h.getTitle().equals(title))
                .findFirst();
        habitOpt.ifPresent(h -> h.setStatus(EntityStatus.DELETED));
        return habitOpt.isPresent();
    }

    public List<String> getUserNamesList(User admin) {
        if (!userByEmail.containsKey(admin.getEmail())) {
            return new LinkedList<>();
        }
        User authenticUser = userByEmail.get(admin.getEmail());
        if (authenticUser.isAdmin() && authenticUser.isUserAuthentic(admin)) {
            Set<User> userSet = habitsOfUser.keySet();
            return userSet.stream()
                    .filter(u -> u.getAccountStatus() != EntityStatus.DELETED)
                    .map(User::toString)
                    .toList();
        }
        return new LinkedList<>();
    }

    public List<User> getUsersList(EntityStatus accountStatus) {
        List<User> users = new LinkedList<>();
        for (User user: habitsOfUser.keySet()) {
            if (user.getAccountStatus().equals(accountStatus)) {
                users.add(user);
            }
        }
        return users;
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
                    if (!user.isBlocked() && user.getAccountStatus() != EntityStatus.DELETED) {
                        user.block();
                        user.setAccountStatus(EntityStatus.UPDATED);
                        return true;
                    }
                    return false;
                }
                case ProfileAction.UNBLOCK: {
                    if (user.isBlocked()) {
                        user.unblock();
                        user.setAccountStatus(EntityStatus.UPDATED);
                        return true;
                    }
                    return false;
                }
                case ProfileAction.DELETE: {
                    user.setAccountStatus(EntityStatus.DELETED);
                    return true;
                }
            }
        }
        return false;
    }


}
