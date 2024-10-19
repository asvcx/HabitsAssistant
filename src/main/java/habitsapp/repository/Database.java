package habitsapp.repository;

import habitsapp.models.Habit;
import habitsapp.models.User;

import java.util.List;
import java.util.Map;

public interface Database {
    List<User> loadUsers();
    Map<String,Habit> loadHabits();
    void saveUsers(List<User> users);
    void saveHabits(String userEmail, List<Habit> habits);
    void updateUsers(List<User> users);
    void updateHabits(String userEmail, List<Habit> habits);
    void removeUsers(List<User> users);
    void removeHabits(String userEmail, List<Habit> habits);
}
