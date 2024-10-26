package habitsapp.data.repository;

import habitsapp.Main;
import habitsapp.data.database.DatabasePostgres;
import habitsapp.ui.in.UserInput;
import habitsapp.ui.in.UserInputByConsole;
import habitsapp.data.models.EntityStatus;
import habitsapp.data.models.Habit;
import habitsapp.data.models.User;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * The DataLoader class is responsible for loading and saving data such users and habits.
 */
public class DataLoader {

    public static UserInput userInput;
    public static DatabasePostgres database;
    public static Repository repository;

    /**
     * Loads properties file for connect to database and instantiates userInput, database and repository
     */
    public void init() {
        String dbUrl;
        String dbUserName;
        String dbPassword;
        Properties prop = new Properties();
        try {
            prop.load(Main.class.getClassLoader().getResourceAsStream("application.properties"));
            dbUrl = prop.getProperty("db.url");
            dbUserName = prop.getProperty("db.username");
            dbPassword = prop.getProperty("db.password");
        }
        catch (IOException e) {
            System.out.println("Exception: " + e.getMessage());
            e.printStackTrace();
            return;
        }
        // An instance of InputData for handling user input.
        userInput = new UserInputByConsole();
        // An instance of DatabasePostgres for managing database operations.
        database = new DatabasePostgres(dbUrl, dbUserName, dbPassword);
        // An instance of AccountRepository for managing user accounts and their associated habits.
        repository = new Repository();
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
        List<User> createdUsers = repository.getUsersList(EntityStatus.CREATED);
        database.saveUsers(createdUsers);
        for (User user : createdUsers) {
            List<Habit> habits = new LinkedList<>(repository.getHabitsSet(user.getEmail()));
            database.saveHabits(user.getID(), habits);
        }

        // Save users with changed profile attributes
        List<User> updatedUsers = repository.getUsersList(EntityStatus.UPDATED);
        for (User user : updatedUsers) {
            List<Habit> habits = new LinkedList<>(repository.getHabitsSet(user.getEmail()));
            database.updateHabits(user.getID(), habits);
        }
        database.updateUsers(updatedUsers);

        // Remove users which was marked as deleted and their habits
        List<User> deletedUsers = repository.getUsersList(EntityStatus.DELETED);
        for (User user : deletedUsers) {
            List<Habit> habits = new LinkedList<>(repository.getHabitsSet(user.getEmail()));
            database.removeHabits(user.getID(), habits);
        }
        database.removeUsers(deletedUsers);
    }

}
