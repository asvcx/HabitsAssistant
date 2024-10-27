package habitsapp.server.repository;

import habitsapp.server.ApplicationContext;
import habitsapp.models.EntityStatus;
import habitsapp.models.Habit;
import habitsapp.models.User;
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
     * Loads properties file for connect to database and instantiates userInput, database and repository
     */
    public void init() {
        /*
        Properties properties = new Properties();
        try {
            properties.load(Main.class.getClassLoader().getResourceAsStream("application.properties"));
        }
        catch (IOException e) {
            System.out.println("Exception: " + e.getMessage());
            return;
        }
        database = new DatabasePostgres(properties);
        repository = new Repository();
         */
    }

    /**
     * Loads data to Repository using Database class.
     */
    public void load() {
        init();
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
     * Saves data from Repository to database using Database class.
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
            database.saveHabits(user.getID(), habits);
            database.updateHabits(user.getID(), habits);
            database.removeHabits(user.getID(), habits);
        }
        // Remove users which was marked as deleted
        List<User> deletedUsers = repository.getUsersByStatus(EntityStatus.DELETED);
        database.removeUsers(deletedUsers);
    }

}
