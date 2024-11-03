package org.habitsapp.server.repository;

import org.habitsapp.models.EntityStatus;
import org.habitsapp.models.Habit;
import org.habitsapp.models.User;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Repository;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Repository
@DependsOn("migration")
public class AccountRepository {
    private final Database database;
    public enum ProfileAction {
        BLOCK, UNBLOCK, DELETE
    }
    private static final Map<User,Map<String,Habit>> habitsOfUser = new HashMap<>();
    private static final Map<String,User> userByEmail = new HashMap<>();
    private static final Map<Long,User> userByID = new HashMap<>();
    private static final Map<String,User> userByToken = new HashMap<>();

    public AccountRepository(Database database) {
        this.database = database;
        // Load users from database
        List<User> users = database.loadUsers();
        for (User user : users) {
            loadUser(user);
        }
        // Load habits from database
        Map<Long,List<Habit>> habitsByID = database.loadHabits();
        for (long userID : habitsByID.keySet()) {
            List<Habit> userHabits = habitsByID.get(userID);
            setHabits(userID, userHabits);
        }
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

    public boolean isUserAuthorized(String token) {
        if (!userByToken.containsKey(token)) {
            return false;
        }
        User user = userByToken.get(token);
        return user.getAccountStatus() != EntityStatus.DELETED;
    }

    public boolean loadUser(User user) {
        if (!isUserExists(user.getEmail().toLowerCase())) {
            habitsOfUser.put(user, new LinkedHashMap<>());
            userByEmail.put(user.getEmail().toLowerCase(), user);
            userByID.put(user.getId(), user);
            return true;
        }
        return false;
    }

    public boolean createUser(User user) {
        if (loadUser(user)) {
            user.setAccountStatus(EntityStatus.CREATED);
            database.saveUser(user);
            return true;
        }
        return false;
    }

    public boolean replaceUser(String email, String token, User changedUser) {
        if (!isUserExists(email)) {
            return false;
        }
        Map<String,Habit> userHabits = getHabitsOfUser(email).orElseGet(LinkedHashMap::new);
        User user = userByEmail.get(email);
        String newEmail = changedUser.getEmail().toLowerCase();
        habitsOfUser.remove(user);
        userByEmail.remove(email);
        userByToken.remove(token);
        userByEmail.put(newEmail, changedUser);
        habitsOfUser.put(changedUser, userHabits);
        userByToken.put(token, user);
        user.setAccountStatus(EntityStatus.UPDATED);
        database.updateUser(user);
        return true;
    }

    public boolean stopSession(String token) {
        if (userByToken.containsKey(token)) {
            userByToken.remove(token);
            return true;
        }
        return false;
    }

    public boolean deleteUser(String email, String token) {
        Optional<User> userOpt = getUserByEmail(email);
        stopSession(token);
        if (userOpt.isEmpty()) {
            return false;
        }
        User user = userOpt.get();
        user.setAccountStatus(EntityStatus.DELETED);
        Map<String,Habit> habits = getHabitsOfUser(user.getEmail()).orElseGet(LinkedHashMap::new);
        for (Habit habit : habits.values()) {
            database.removeHabit(user.getId() , habit);
        };
        database.removeUser(user);
        return true;
    }

    public boolean updateUser(String email, Consumer<User> userAction) {
        Optional<User> user = getUserByEmail(email);
        if (user.isPresent() && userAction != null) {
            userAction.accept(user.get());
            user.get().setAccountStatus(EntityStatus.UPDATED);
            database.updateUser(user.get());
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
        Map<String,Habit> habits = habitsOfUser.get(user);
        if (habits.containsKey(habit.getTitle())) {
            return false;
        }
        habits.put(habit.getTitle(), habit);
        return true;
    }

    public boolean changeHabitProperties(String email, Habit oldHabit, Habit newHabit) {
        Map<String,Habit> userHabits = getHabitsOfUser(email).orElseGet(LinkedHashMap::new);
        Optional<User> user = getUserByEmail(email.toLowerCase());
        if (!userHabits.containsKey(oldHabit.getTitle()) || user.isEmpty()) {
            return false;
        }
        oldHabit.setStatus(EntityStatus.UPDATED);
        userHabits.remove(oldHabit.getTitle());
        oldHabit.setTitle(newHabit.getTitle());
        oldHabit.setDescription(newHabit.getDescription());
        oldHabit.setPeriod(newHabit.getPeriod());
        userHabits.put(oldHabit.getTitle(), oldHabit);
        database.updateHabit(user.get().getId(), oldHabit);
        return true;
    }

    public boolean createHabit(String email, Habit habit) {
        Optional<User> user = getUserByEmail(email);
        if (user.isPresent() && loadHabit(email, habit)) {
            habit.setStatus(EntityStatus.CREATED);
            habit.setUserId(user.get().getId());
            database.saveHabit(user.get().getId(), habit);
            return true;
        }
        return false;
    }

    public boolean setHabits(long userID, List<Habit> habitsList) {
        if (!isUserExists(userID)) {
            return false;
        }
        User user = userByID.get(userID);
        habitsOfUser.remove(user);
        Map<String,Habit> habits = habitsList.stream()
                .collect(Collectors.toMap(Habit::getTitle, habit -> habit));
        habitsOfUser.put(user, habits);
        return true;
    }

    public boolean deleteHabit(String email, String title) {
        Optional<User> user = getUserByEmail(email);
        if (user.isPresent()) {
            Map<String,Habit> userHabits = habitsOfUser.get(user.get());
            if (userHabits.containsKey(title)) {
                Habit habit = userHabits.get(title);
                database.removeHabit(user.get().getId(), habit);
                userHabits.remove(title);
                return true;
            }
        }
        return false;
    }

    public Optional<User> getUserByEmail(String email) {
        if (!isUserExists(email.toLowerCase())) {
            return Optional.empty();
        }
        return Optional.of(userByEmail.get(email.toLowerCase()));
    }

    public boolean isTokenExists(String token) {
        return userByToken.containsKey(token);
    }

    public Optional<User> getUserByToken(String token) {
        if (userByToken.containsKey(token)) {
            return Optional.of(userByToken.get(token));
        }
        return Optional.empty();
    }

    public void addToken(String token, User user) {
        userByToken.put(token, user);
    }

    public boolean removeToken(String token) {
        if (userByToken.containsKey(token)) {
            userByToken.remove(token);
            return true;
        }
        return false;
    }

    public boolean checkToken(String email, String token) {
        Optional<User> eUser = getUserByEmail(email.toLowerCase());
        Optional<User> tUser = getUserByToken(token);
        return eUser.isPresent() && tUser.isPresent() && email.equals(tUser.get().getEmail());
    }

    public boolean checkPassword(String email, String password) {
        Optional<User> user = getUserByEmail(email.toLowerCase());
        return user.isPresent() && password.equals(user.get().getPassword());
    }

    public Optional<Map<String,Habit>> getHabitsOfUser(String email) {
        Optional<User> user = getUserByEmail(email);
        if (user.isEmpty()) {
            return Optional.empty();
        }
        if (habitsOfUser.containsKey(user.get())) {
            return Optional.of(habitsOfUser.get(user.get()));
        }
        return Optional.empty();
    }

    public Optional<Habit> getHabitByTitle(String email, String title) {
        Optional<Map<String,Habit>> habits = getHabitsOfUser(email);
        return habits.map(stringHabitMap -> stringHabitMap.get(title));
    }

    public List<User> getUsers() {
        return new LinkedList<>(habitsOfUser.keySet());
    }

}
