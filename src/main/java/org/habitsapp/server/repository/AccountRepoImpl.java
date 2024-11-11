package org.habitsapp.server.repository;

import org.habitsapp.model.Habit;
import org.habitsapp.model.User;
import org.springframework.stereotype.Repository;
import java.util.*;
import java.util.function.Consumer;

@Repository
public class AccountRepoImpl implements AccountRepo {
    private final Database database;

    public AccountRepoImpl(Database database) {
        this.database = database;
    }

    public List<User> getUsers() {
        return database.loadUsers();
    }

    public Optional<User> getUserByEmail(String email) {
        return database.loadUser(email.toLowerCase());
    }

    public Optional<User> getUserById(long id) {
        return database.loadUser(id);
    }

    public Optional<Map<String,Habit>> getHabitsOfUser(Long id) {
        Optional<User> userOpt = database.loadUser(id);
        if (userOpt.isEmpty()) {
            return Optional.empty();
        }
        User user = userOpt.get();
        Map<String,Habit> habits = database.loadHabits(user.getId());
        return Optional.of(habits);
    }

    public Optional<Habit> getHabitByTitle(Long id, String title) {
        Optional<Map<String,Habit>> habits = getHabitsOfUser(id);
        return habits.map(stringHabitMap -> stringHabitMap.get(title));
    }

    public boolean isUserExists(String email) {
        Optional<User> userOpt = database.loadUser(email.toLowerCase());
        return userOpt.isPresent();
    }

    public boolean isUserExists(long id) {
        Optional<User> userOpt = database.loadUser(id);
        return userOpt.isPresent();
    }

    public boolean createUser(User user) {
        if (!isUserExists(user.getEmail().toLowerCase())) {
            database.saveUser(user);
            return true;
        }
        return false;
    }

    public boolean deleteUser(Long id) {
        Optional<User> userOpt = database.loadUser(id);
        if (userOpt.isEmpty()) {
            return false;
        }
        User user = userOpt.get();
        Map<String,Habit> habits = getHabitsOfUser(id).orElseGet(LinkedHashMap::new);
        for (Habit habit : habits.values()) {
            database.removeHabit(user.getId() , habit.getTitle());
        }
        database.removeUser(user);
        return true;
    }

    public boolean updateUser(Long id, User changedUser) {
        Optional<User> userOpt = database.loadUser(id);
        if (userOpt.isEmpty()) {
            return false;
        }
        database.updateUser(changedUser);
        return true;
    }

    public boolean setUserBlockStatus(Long id, Consumer<User> userAction) {
        Optional<User> user = database.loadUser(id);
        if (user.isPresent() && userAction != null) {
            userAction.accept(user.get());
            database.updateUser(user.get());
            return true;
        }
        return false;
    }

    public boolean updateHabit(Long userId, String oldTitle, String title, String description, int period) {
        Map<String,Habit> userHabits = getHabitsOfUser(userId).orElseGet(LinkedHashMap::new);
        Optional<User> user = database.loadUser(userId);
        if (!userHabits.containsKey(oldTitle) || user.isEmpty()) {
            return false;
        }
        Habit habit = new Habit();
        userHabits.remove(oldTitle);
        habit.setTitle(title);
        habit.setDescription(description);
        habit.setPeriod(period);
        userHabits.put(habit.getTitle(), habit);
        database.updateHabit(user.get().getId(), habit);
        return true;
    }

    public boolean markHabit(Long userId, Habit habit) {
        Optional<User> user = database.loadUser(userId);
        if (user.isEmpty()) {
            return false;
        }
        database.updateHabit(user.get().getId(), habit);
        return true;
    }

    public boolean createHabit(Long userId, Habit habit) {
        Optional<User> user = database.loadUser(userId);
        if (user.isPresent()) {
            habit.setUserId(user.get().getId());
            database.saveHabit(user.get().getId(), habit);
            return true;
        }
        return false;
    }

    public boolean deleteHabit(Long userId, String title) {
        Optional<User> user = database.loadUser(userId);
        return user.filter(u -> database.removeHabit(u.getId(), title)).isPresent();
    }

    public boolean checkPassword(Long userId, String password) {
        Optional<User> user = database.loadUser(userId);
        return user.isPresent() && password.equals(user.get().getPassword());
    }

}
