package org.habitsapp.server.repository;

import org.habitsapp.model.Habit;
import org.habitsapp.model.User;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface Database {
    List<User> loadUsers();
    Optional<User> loadUser(long id);
    Optional<User> loadUser(String email);
    Map<String,Habit> loadHabits(long userId);
    boolean saveUser(User user);
    boolean saveHabit(long UserId, Habit habit);
    boolean updateUser(User user);
    boolean updateHabit(long userID, Habit habit);
    boolean removeUser(User user);
    boolean removeHabit(long userID, String title);
}
