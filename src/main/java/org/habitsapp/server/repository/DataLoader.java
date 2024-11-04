package org.habitsapp.server.repository;

import org.habitsapp.server.ApplicationContext;
import org.habitsapp.models.EntityStatus;
import org.habitsapp.models.Habit;
import org.habitsapp.models.User;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * The DataLoader class is responsible for loading and saving data such users and habits.
 */
public class DataLoader {

    private final Database database;
    private final Repository repository;

    public DataLoader(ApplicationContext context) {
        repository = context.getRepository();
        database = context.getDatabase();
    }

    /**
     * Load data to Repository using Database class.
     */
    public void load() {
        List<User> users = database.loadUsers();
        for (User user : users) {
            repository.loadUser(user);
        }
        Map<Long,List<Habit>> habitsByID = database.loadHabits();
        for (long userID : habitsByID.keySet()) {
            List<Habit> userHabits = habitsByID.get(userID);
            repository.setHabits(userID, userHabits);
        }
    }

    /**
     * Save data from Repository to database using Database class.
     */
    public void release() {
        // Save new users
        List<User> createdUsers = repository.getUsersByStatus(EntityStatus.CREATED);
        database.saveUsers(createdUsers);

        // Save users with changed profile attributes
        List<User> updatedUsers = repository.getUsersByStatus(EntityStatus.UPDATED);
        database.updateUsers(updatedUsers);

        // Perform saving, updating and deleting habits
        List<User> users = repository.getUsers();
        for (User user : users) {
            List<Habit> habits = new LinkedList<>(repository.getHabitsOfUser(user.getEmail()));
            database.saveHabits(user.getId(), habits);
            database.updateHabits(user.getId(), habits);
            database.removeHabits(user.getId(), habits);
        }
        // Remove users which was marked as deleted
        List<User> deletedUsers = repository.getUsersByStatus(EntityStatus.DELETED);
        database.removeUsers(deletedUsers);
    }

}
