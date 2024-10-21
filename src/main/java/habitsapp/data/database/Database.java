package habitsapp.data.database;

import habitsapp.data.models.Habit;
import habitsapp.data.models.User;

import java.util.List;
import java.util.Map;

public interface Database {
    List<User> loadUsers();
    Map<Long,List<Habit>> loadHabits();
    void saveUsers(List<User> users);
    void saveHabits(long userID, List<Habit> habits);
    void updateUsers(List<User> users);
    void updateHabits(long userID, List<Habit> habits);
    void removeUsers(List<User> users);
    void removeHabits(long userID, List<Habit> habits);
}
