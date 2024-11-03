package org.habitsapp.server.repository;

import org.habitsapp.models.Habit;
import org.habitsapp.models.User;

import java.util.List;
import java.util.Map;

public interface Database {
    List<User> loadUsers();
    Map<Long,List<Habit>> loadHabits();
    void saveUser(User user);
    void saveHabit(long UserId, Habit habit);
    void updateUser(User user);
    void updateHabit(long userID, Habit habit);
    void removeUser(User user);
    void removeHabit(long userID, Habit habit);
}
