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
        return new LinkedList<>();
    }

    public Optional<User> getUserByEmail(String email) {
        return database.loadUser(email.toLowerCase());
    }

    public Optional<User> getUserById(long id) {
        return database.loadUser(id);
    }

    public Optional<Map<String,Habit>> getHabitsOfUser(String email) {
        Optional<User> userOpt = database.loadUser(email.toLowerCase());
        if (userOpt.isEmpty()) {
            return Optional.empty();
        }
        User user = userOpt.get();
        Map<String,Habit> habits = database.loadHabits(user.getId());
        return Optional.of(habits);
    }

    public Optional<Habit> getHabitByTitle(String email, String title) {
        Optional<Map<String,Habit>> habits = getHabitsOfUser(email);
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

    public boolean updateUser(String email, String token, User changedUser) {
        Optional<User> userOpt = database.loadUser(email.toLowerCase());
        if (userOpt.isEmpty()) {
            return false;
        }
        User user = userOpt.get();
        database.updateUser(user);
        return true;
    }

    public boolean deleteUser(String email, String token) {
        Optional<User> userOpt = database.loadUser(email.toLowerCase());
        if (userOpt.isEmpty()) {
            return false;
        }
        User user = userOpt.get();
        Map<String,Habit> habits = getHabitsOfUser(user.getEmail()).orElseGet(LinkedHashMap::new);
        for (Habit habit : habits.values()) {
            database.removeHabit(user.getId() , habit.getTitle());
        }
        database.removeUser(user);
        return true;
    }

    public boolean updateUser(String email, Consumer<User> userAction) {
        Optional<User> user = database.loadUser(email.toLowerCase());
        if (user.isPresent() && userAction != null) {
            userAction.accept(user.get());
            database.updateUser(user.get());
            return true;
        }
        return false;
    }

    public boolean updateHabit(String email, Habit oldHabit, Habit newHabit) {
        Map<String,Habit> userHabits = getHabitsOfUser(email).orElseGet(LinkedHashMap::new);
        Optional<User> user = database.loadUser(email.toLowerCase());
        if (!userHabits.containsKey(oldHabit.getTitle()) || user.isEmpty()) {
            return false;
        }
        userHabits.remove(oldHabit.getTitle());
        oldHabit.setTitle(newHabit.getTitle());
        oldHabit.setDescription(newHabit.getDescription());
        oldHabit.setPeriod(newHabit.getPeriod());
        userHabits.put(oldHabit.getTitle(), oldHabit);
        database.updateHabit(user.get().getId(), oldHabit);
        return true;
    }

    public boolean markHabit(String email, Habit habit) {
        Optional<User> user = database.loadUser(email.toLowerCase());
        if (user.isEmpty()) {
            return false;
        }
        database.updateHabit(user.get().getId(), habit);
        return true;
    }

    public boolean createHabit(String email, Habit habit) {
        Optional<User> user = database.loadUser(email.toLowerCase());
        if (user.isPresent()) {
            habit.setUserId(user.get().getId());
            database.saveHabit(user.get().getId(), habit);
            return true;
        }
        return false;
    }

    public boolean deleteHabit(String email, String title) {
        Optional<User> user = database.loadUser(email.toLowerCase());
        return user.filter(u -> database.removeHabit(u.getId(), title)).isPresent();
    }

    public boolean checkPassword(String email, String password) {
        Optional<User> user = database.loadUser(email.toLowerCase());
        return user.isPresent() && password.equals(user.get().getPassword());
    }

}
